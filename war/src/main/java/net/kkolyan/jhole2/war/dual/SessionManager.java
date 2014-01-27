package net.kkolyan.jhole2.war.dual;

import net.kkolyan.jhole2.log.H2ApplicationLogger;
import net.kkolyan.jhole2.monitoring.Monitoring;
import net.kkolyan.jhole2.remoting.LocalRawEndpoint;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author NPlekhanov
 */
public class SessionManager implements HttpSessionListener, ServletContextListener {
    private List<SessionController> sessionControllers = new CopyOnWriteArrayList<SessionController>();

    private H2ApplicationLogger applicationLogger;

    @Override
    public void sessionCreated(final HttpSessionEvent se) {
        SessionController controller = new SessionController(new LocalRawEndpoint(new Object() {
            @Override
            public String toString() {
                return se.getSession().getId();
            }
        },applicationLogger));
        se.getSession().setAttribute(SessionController.class.getName(), controller);
        sessionControllers.add(controller);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        SessionController controller = (SessionController) se.getSession().getAttribute(SessionController.class.getName());
        controller.destroy();
        sessionControllers.remove(controller);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        applicationLogger = new H2ApplicationLogger();
        sce.getServletContext().setAttribute("applicationLogger", applicationLogger);
        sce.getServletContext().setAttribute(SessionManager.class.getName(), this);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
