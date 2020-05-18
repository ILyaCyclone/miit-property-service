package ru.unisuite.propertyservice;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/*")
public class PropertyServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(PropertyServlet.class.getName());

    private static final String CONTENT_TYPE_TEXT = "text/plain;charset=UTF-8";
    private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
    private static final String PROPERTY_QUERY = "select to_char(wpms_env_wp.get_property(?)) from dual";
    private static final String ENV_PROPERTY_QUERY = "select p_environment_.get_ve_us_text(?) from dual";
    private static final String DELIMITER = ",";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = getAbsoluteUrl(request);

        String propertyKey = getPropertyKey(request);
        if (propertyKey == null || propertyKey.trim().length() < 1) {
            responseNoPropertyKey(request, response);
            return;
        }

        boolean environmentProperty = url.contains("/env/");

        boolean json = url.contains("/json/") || request.getParameter("json") != null;
        boolean multiple = url.contains(",");

        String propertyValue = null;

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            DataSource dataSource = DataSourceManager.lookup();
            con = dataSource.getConnection();

            String query = environmentProperty ? ENV_PROPERTY_QUERY : PROPERTY_QUERY;

            if (!multiple) {
                stmt = con.prepareStatement(query);
                stmt.setString(1, propertyKey);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    String result = rs.getString(1);
                    if (rs.wasNull()) {
                        responsePropertyNotFound(request, response);
                        return;

                    }

                    if (!json) {
                        propertyValue = result;
                    } else {
                        propertyValue = "{\"" + propertyKey + "\": \"" + escapeForJson(result) + "\"}";
                    }
                }
            } else {
                // multiple
                String[] propertyKeys = propertyKey.split(",");
                StringBuilder multipleQuery = new StringBuilder(query);
                for (int i = 1; i < propertyKeys.length; i++) {
                    multipleQuery.append(" union all ").append(query);
                }
                stmt = con.prepareStatement(multipleQuery.toString());
                for (int i = 0; i < propertyKeys.length; i++) {
                    stmt.setString(i + 1, propertyKeys[i]);
                }

                rs = stmt.executeQuery();

                StringBuilder sb = new StringBuilder();
                int i = 0;
                while (rs.next()) {
                    if (i > 0) {
                        sb.append(DELIMITER);
                    }
                    String result = rs.getString(1);

                    if (!json) {
                        if (result == null) {
                            result = "";
                        }
                        result = escapeForCsv(result);
                        sb.append(result);
                    } else {
                        // csv
                        sb.append('"' + propertyKeys[i] + "\": ");
                        if (result != null) {
                            sb.append('"' + escapeForJson(result) + '"');
                        } else {
                            sb.append("null");
                        }
                    }
                    i++;
                }
                if (json) {
                    sb.insert(0, '{').append('}');
                }
                propertyValue = sb.toString();
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not get properties for url '" + url + '\'', e);
            // TODO reply with appropriate format
            throw new RuntimeException("Could not get properties for url '" + url + '\'', e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                }
            }

            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                }
            }
        }

        response.setContentType(json ? CONTENT_TYPE_JSON : CONTENT_TYPE_TEXT);
        PrintWriter out = response.getWriter();
        out.print(propertyValue);
        out.close();
    }

    private String escapeForCsv(String s) {
        if (s == null) return null;
        return s.replaceAll("\\" + DELIMITER, "\\\\" + DELIMITER); // aa,bb --> aa\,bb
    }

    private String escapeForJson(String s) {
        if (s == null) return null;
        return s.replaceAll("\\\"", "\\\\\"")
                .replaceAll("\n", "\\\\n");
    }

    private void responsePropertyNotFound(HttpServletRequest request,
                                          HttpServletResponse response) throws IOException {
        String propertyKey = getPropertyKey(request);
        response.setContentType(CONTENT_TYPE_TEXT);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        PrintWriter out = response.getWriter();
        out.println("Property not found for key '" + propertyKey + '\'');
        out.close();
    }


    private void responseNoPropertyKey(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType(CONTENT_TYPE_TEXT);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String url = getAbsoluteUrl(request);
        PrintWriter out = response.getWriter();
        out.println("No property key specified in url '" + url + '\'');
        out.println();
        out.println("Please, specify needed property key in format:");
        out.println("- /<property_key> to get value by <property_key>");
        out.println("- /env/<env_property_key> prepend with /env for environment properties");
        out.println("- /<property_key_1>,<property_key_2> for multiple properties");
        out.println("- /json/<property_key_1>,<property_key_2> or /<property_key_1>,<property_key_2>?json for json format");
        out.close();
    }


    private String getAbsoluteUrl(HttpServletRequest request) {
        return request.getRequestURL().toString();
    }

    private String getPropertyKey(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
