package ru.unisuite.propertyservice;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@WebServlet({"/version", "/info"})
public class VersionServlet extends HttpServlet {

    private static final String GIT_PROPERTIES_FILENAME = "git.properties";
    private static final String GIT_PREFIX = "git.";

    private static final String FORMAT =
            "{" +
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Properties prop = new Properties();
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(GIT_PROPERTIES_FILENAME)) {
            prop.load(input);
        }

        String buildDate = prop.getProperty(GIT_PREFIX + "build.time");
        String branch = prop.getProperty(GIT_PREFIX + "branch");
        String commitDate = prop.getProperty(GIT_PREFIX + "commit.time");
        String commitId = prop.getProperty(GIT_PREFIX + "commit.id");
        String shortMessage = prop.getProperty(GIT_PREFIX + "commit.message.short");
        String dirty = prop.getProperty(GIT_PREFIX + "dirty");

        resp.getWriter().print(String.format(FORMAT, buildDate, branch, commitDate, commitId, shortMessage, dirty));
    }
}
