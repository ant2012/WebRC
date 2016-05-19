package ru.ant.rc.web; /**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 04.02.13
 * Time: 21:58
 * To change this template use File | Settings | File Templates.
 */

import org.apache.log4j.Logger;
import ru.ant.common.App;
import ru.ant.common.TriggerPoolManager;
import ru.ant.common.properties.WebPropertiesManager;
import ru.ant.iot.cloud.queue.JsonTaskFactory;
import ru.ant.iot.cloud.queue.JsonTaskTrigger;
import ru.ant.iot.cloud.queue.RpiTaskFactory;
import ru.ant.iot.ifttt.NewIpTrigger;
import ru.ant.rc.serial.Command;
import ru.ant.rc.serial.SerialDriver;
import ru.ant.rc.serial.SerialService;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class WebRCContextHolder implements ServletContextListener {

    //SerialHardwareDetector serialHardwareDetector;
    private SerialDriver serialDriver;
    private SerialService serialService;
    private final Logger logger;

    private final ScheduledExecutorService pool;

    // Public constructor is required by servlet spec
    public WebRCContextHolder() {
        logger = Logger.getLogger(this.getClass());
        pool = Executors.newScheduledThreadPool(1);
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

        String workingPath = servletContext.getRealPath("/WEB-INF/classes/");
        workingPath = workingPath.substring(0, workingPath.length()-1);

        PriorityBlockingQueue<Command> commandQueue = new PriorityBlockingQueue<Command>();

        this.serialService = new SerialService(commandQueue, workingPath);
        this.serialDriver = serialService.getSerialDriver();

        servletContext.setAttribute("CommandQueue", commandQueue);
        servletContext.setAttribute("SerialDriver", serialDriver);

        Thread serialServiceThread = new Thread(this.serialService);
        serialServiceThread.start();

        App.getInstance().setPropertiesManager(new WebPropertiesManager(sce.getServletContext()));

        TriggerPoolManager.addTrigger(pool, new NewIpTrigger());
        JsonTaskFactory jsonTaskFactory = new RpiTaskFactory();
        TriggerPoolManager.addTrigger(pool, new JsonTaskTrigger(jsonTaskFactory));

    }

    public void contextDestroyed(ServletContextEvent sce) {
      /* This method is invoked when the Servlet Context 
         (the Web application) is undeployed or 
         Application Server shuts down.
      */
        logger.info("Destroying ServletContext..");
        pool.shutdown();

        if(this.serialService != null)
            this.serialService.stop();

        try {
            pool.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Pool termination timeout exeeded", e);
        }
        logger.info("ServletContext destroyed..");
    }

}
