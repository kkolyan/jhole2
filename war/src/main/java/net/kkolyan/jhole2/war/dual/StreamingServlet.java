package net.kkolyan.jhole2.war.dual;

import net.kkolyan.jhole2.war.dual.SessionController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author NPlekhanov
 */
public class StreamingServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SessionController controller = (SessionController) req.getSession().getAttribute(SessionController.class.getName());
        if ("downstream".equals(req.getParameter("direction"))) {
            controller.handleDownstream(req, resp);
        } else if ("upstream".equals(req.getParameter("direction"))) {
            controller.handleUpstream(req, resp);
        } else throw new IllegalStateException();
    }
}
