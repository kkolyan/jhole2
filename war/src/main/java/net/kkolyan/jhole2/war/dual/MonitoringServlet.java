package net.kkolyan.jhole2.war.dual;

import net.kkolyan.jhole2.log.H2ApplicationLogger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class MonitoringServlet extends HttpServlet  {

    @Override
    public void init() throws ServletException {

        Properties props = new Properties();
        props.put("resource.loader","class");
        props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(props);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        H2ApplicationLogger applicationLogger = (H2ApplicationLogger) getServletContext().getAttribute("applicationLogger");
        Template template = Velocity.getTemplate("log.vm");

        Context context = new VelocityContext();
        context.put("request", req);
        context.put("log", applicationLogger);

        resp.setContentType("text/html");
        resp.setCharacterEncoding("utf8");
        template.merge(context, resp.getWriter());
    }
}
