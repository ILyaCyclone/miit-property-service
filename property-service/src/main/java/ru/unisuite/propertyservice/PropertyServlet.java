package ru.unisuite.propertyservice;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.sql.DataSource;

@WebServlet("/*")
public class PropertyServlet extends HttpServlet {
    //@formatter:off
    private static final String CONTENT_TYPE_TEXT = "text/plain;charset=UTF-8"
        , CONTENT_TYPE_JSON = "application/json"
        , DATA_SOURCE = "jdbc/ds_basic"
        , DELIMITER = ","
        , PROPERTY_QUERY = "select to_char(wpms_env_wp.get_property(?)) as property_value from dual"
        , ENV_PROPERTY_QUERY = "select p_environment_.get_ve_us_text(?) as property_value from dual";
    //@formatter:on

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

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
            DataSource dataSource = DataSourceManager.lookup(DATA_SOURCE);
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
                    result = escape(result);
                    if (!json) {
                        propertyValue = result;
                    } else {
                        propertyValue = "{\"" + propertyKey + "\" : \"" + result + "\"}";
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
                    String result = rs.getString("property_value");
                    if (result == null) {
                        result = "";
                    }
                    result = escape(result);

                    if (!json) {
                        result = escapeDelimiter(result);
                        sb.append(result);
                    } else {
                        sb.append('"' + propertyKeys[i] + "\" : \"" + result + '"');
                    }
                    i++;
                }
                if (json) {
                    sb.insert(0, '{').append('}');
                }
                propertyValue = sb.toString();
            }

        } catch (Exception e) {
            throw new RuntimeException("PropertyService failed for url '" + url + '\'', e);
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
                } catch (Exception ex) {
                }
            }

            if (con != null) {
                try {
                    con.close();
                } catch (Exception ex) {
                }
            }
        }

        response.setContentType(json ? CONTENT_TYPE_JSON : CONTENT_TYPE_TEXT);
        PrintWriter out = response.getWriter();
        out.print(propertyValue);
        out.close();
    }


    private String escape(String s) {
        return s.replaceAll("\"", "\\\\\"")
                .replaceAll("\n", "")
                .replaceAll("\r", "")
                .replaceAll("\t", "");
    }

    private String escapeDelimiter(String s) {
        return s.replaceAll("\\"+DELIMITER, "\\\\" + DELIMITER); // aa,bb --> aa\,bb
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
