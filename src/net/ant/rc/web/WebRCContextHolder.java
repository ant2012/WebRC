package net.ant.rc.web; /**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 04.02.13
 * Time: 21:58
 * To change this template use File | Settings | File Templates.
 */

import net.ant.rc.serial.Command;
import net.ant.rc.serial.SerialDriver;
import net.ant.rc.serial.SerialService;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.PriorityBlockingQueue;

@WebListener
public class WebRCContextHolder implements ServletContextListener {

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
        logger.info("Initialising ServletContext..");
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
        logger.info("Destroying ServletContext..");
        if(this.serialDriver !=null)
            this.serialDriver.disconnect();
        if(this.serialService != null)
            this.serialService.stop();
    }

}
