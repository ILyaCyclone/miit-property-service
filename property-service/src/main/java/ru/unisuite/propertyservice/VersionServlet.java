package ru.unisuite.propertyservice;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet({"/version", "/info"})
public class VersionServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(VersionServlet.class.getName());

    private static final String GIT_PROPERTIES_FILENAME = "git.properties";
    private static final String GIT_PREFIX = "git.";

    //@formatter:off
    private static final String FORMAT =
            "{" +
              "\"application\": \"property-service\"," +
              "\"build\": {" +
                "\"time\": \"%s\"" +
              "}," +
              "\"git\": {" +
                "\"branch\": \"%s\"," +
                "\"commit\": {" +
                  "\"time\": \"%s\"," +
                  "\"id\": \"%s\"," +
                  "\"message\": \"%s\"" +
                "}," +
                "\"dirty\": \"%s\"" +
              "}" +
            "}";
    //@formatter:on

    private static String formattedResponse;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(GIT_PROPERTIES_FILENAME)) {
            Properties prop = new Properties();
            prop.load(input);

            //@formatter:off
            String buildDate    = prop.getProperty(GIT_PREFIX + "build.time");
            String branch       = prop.getProperty(GIT_PREFIX + "branch");
            String commitDate   = prop.getProperty(GIT_PREFIX + "commit.time");
            String commitId     = prop.getProperty(GIT_PREFIX + "commit.id");
            String shortMessage = prop.getProperty(GIT_PREFIX + "commit.message.short");
            String dirty        = prop.getProperty(GIT_PREFIX + "dirty");
            //@formatter:on
            formattedResponse = String.format(FORMAT, buildDate, branch, commitDate, commitId, shortMessage, dirty);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not prepare version information", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().print(formattedResponse);
    }
}
