package net.ant.rc.web; /**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 04.02.13
 * Time: 21:58
 * To change this template use File | Settings | File Templates.
 */

import net.ant.rc.serial.Command;
import net.ant.rc.serial.SerialDriver;
import net.ant.rc.serial.SerialHardwareDetector;
import net.ant.rc.serial.SerialService;
import net.ant.rc.serial.exception.CommPortException;
import net.ant.rc.serial.exception.UnsupportedHardwareException;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.PriorityBlockingQueue;

@WebListener()
public class WebRCContextHolder implements ServletContextListener,
        HttpSessionListener, HttpSessionAttributeListener {

    //SerialHardwareDetector serialHardwareDetector;
    SerialDriver serialDriver;
    SerialService serialService;
    private final Logger logger;

    // Public constructor is required by servlet spec
    public WebRCContextHolder() {
        logger = Logger.getLogger(this.getClass());
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
      /* This method is called when the servlet context is
         initialized(when the Web application is deployed). 
         You can initialize servlet context related data here.
      */
        ServletContext servletContext = sce.getServletContext();

        String workingPath = servletContext.getRealPath("/WEB-INF/classes/.");
        workingPath = workingPath.substring(0, workingPath.length()-1);

        PriorityBlockingQueue<Command> commandQueue = new PriorityBlockingQueue<Command>();

        this.serialService = new SerialService(commandQueue, workingPath);
        this.serialDriver = serialService.getSerialDriver();

        servletContext.setAttribute("CommandQueue", commandQueue);
        servletContext.setAttribute("SerialDriver", serialDriver);

        Thread serialServiceThread = new Thread(this.serialService);
        serialServiceThread.start();
    }

    public void contextDestroyed(ServletContextEvent sce) {
      /* This method is invoked when the Servlet Context 
         (the Web application) is undeployed or 
         Application Server shuts down.
      */
        if(this.serialDriver !=null)
            this.serialDriver.disconnect();
        if(this.serialService != null)
            this.serialService.stop();
    }

    // -------------------------------------------------------
    // HttpSessionListener implementation
    // -------------------------------------------------------
    public void sessionCreated(HttpSessionEvent se) {
      /* Session is created. */
    }

    public void sessionDestroyed(HttpSessionEvent se) {
      /* Session is destroyed. */
    }

    // -------------------------------------------------------
    // HttpSessionAttributeListener implementation
    // -------------------------------------------------------

    public void attributeAdded(HttpSessionBindingEvent sbe) {
      /* This method is called when an attribute 
         is added to a session.
      */
    }

    public void attributeRemoved(HttpSessionBindingEvent sbe) {
      /* This method is called when an attribute
         is removed from a session.
      */
    }

    public void attributeReplaced(HttpSessionBindingEvent sbe) {
      /* This method is invoked when an attibute
         is replaced in a session.
      */
    }
}
