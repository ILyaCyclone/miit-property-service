package ru.unisuite.propertyservice;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/health")
public class HealthServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(HealthServlet.class.getName());

    //@formatter:off
    private static final String FORMAT =
            "{" +
              "\"status\": \"%s\"," +
              "\"components\": {" +
                "\"db\": {" +
                  "\"status\": \"%s\"" +
                "}" +
              "}" +
            "}";
    //@formatter:on

    private enum Status {
        UP, DOWN, CHECK_FAILED
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Status applicationStatus = Status.UP;

        Status databaseStatus;
        try {
            databaseStatus = checkDatabaseIsUp() ? Status.UP : Status.DOWN;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not check database status", e);
            databaseStatus = Status.CHECK_FAILED;
        }

        resp.getWriter().print(String.format(FORMAT, applicationStatus, databaseStatus));
    }

    /**
     * assuming it is Oracle DB
     */
    private boolean checkDatabaseIsUp() throws SQLException {
        DataSource dataSource = DataSourceManager.lookup();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 'Hello' from DUAL");
             ResultSet rs = stmt.executeQuery()
        ) {
            rs.next();
            return rs.getString(1).equals("Hello");
        }
    }
}
