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

@WebServlet("/health")
public class HealthServlet extends HttpServlet {

    private static final String FORMAT =
            "{" +
              "\"status\": \"%s\"," +
              "\"components\": {" +
                "\"db\": {" +
                  "\"status\": \"%s\"" +
                "}" +
              "}" +
            "}";

    private static final String UP = "UP";
    private static final String DOWN = "DOWN";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean applicationStatusIsUp = true;
        boolean databaseStatusIsUp = checkDatabaseIsUp();

        resp.getWriter().print(String.format(FORMAT, (applicationStatusIsUp?UP:DOWN), (databaseStatusIsUp?UP:DOWN)));
    }

    /**
     * assuming it is Oracle DB
     */
    private boolean checkDatabaseIsUp() {
        DataSource dataSource = DataSourceManager.lookup();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 'Hello' from DUAL");
             ResultSet rs = stmt.executeQuery()
        ) {
            rs.next();
            return rs.getString(1).equals("Hello");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
