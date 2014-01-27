package net.kkolyan.jhole2;

import net.kkolyan.jhole2.log.H2ApplicationLogger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @author nplekhanov
 */
public class Console2 {

    private static int port;
    private static H2ApplicationLogger applicationLogger;

    public static void launch(Executor executor, int port, H2ApplicationLogger applicationLogger) throws IOException {
        Console2.port = port;
        Console2.applicationLogger = applicationLogger;

        Properties props = new Properties();
        props.put("resource.loader","class");
        props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(props);

        Server server = new Server(port);
        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(Servlet.class, "/log");
        server.addHandler(handler);
        try {
            server.start();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static int getPort() {
        return port;
    }

    public static class Servlet extends HttpServlet {
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

            Template template = Velocity.getTemplate("log.vm");

            Context context = new VelocityContext();
            context.put("request", req);
            context.put("log", applicationLogger);

            resp.setContentType("text/html");
            resp.setCharacterEncoding("utf8");
            template.merge(context, resp.getWriter());
        }
    }
}