package ru.ant.rc.web; /**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 04.02.13
 * Time: 21:58
 * To change this template use File | Settings | File Templates.
 */

import org.apache.log4j.Logger;
import ru.ant.common.App;
import ru.ant.common.properties.WebPropertiesManager;
import ru.ant.iot.TriggerPoolManager;
import ru.ant.iot.cloud.queue.*;
import ru.ant.iot.ifttt.NewIpTrigger;
import ru.ant.iot.rpi.MjpgStreamer;
import ru.ant.rc.serial.Config;
import ru.ant.rc.serial.SerialService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class WebRCContextHolder implements ServletContextListener {
    private Logger log = Logger.getLogger(getClass());

    private final ScheduledExecutorService pool;

    // Public constructor is required by servlet spec
    public WebRCContextHolder() {
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
        log.info("Initialising ServletContext..");

        WebPropertiesManager propertiesManager = new WebPropertiesManager(sce.getServletContext());
        propertiesManager.addFile(Config.FILE_NAME);
        App.getInstance().setPropertiesManager(propertiesManager);

        TriggerPoolManager.addTrigger(pool, new NewIpTrigger());
        JsonTaskFactory jsonTaskFactory = new RpiTaskFactory();
        TriggerPoolManager.addTrigger(pool, new JsonTaskTrigger(App.getProperty("cloud.key"), jsonTaskFactory));

        MjpgStreamer.start("320x240", "5");
    }

    public void contextDestroyed(ServletContextEvent sce) {
      /* This method is invoked when the Servlet Context 
         (the Web application) is undeployed or 
         Application Server shuts down.
      */
        log.info("Destroying ServletContext..");

        pool.shutdown();

        SerialService.getInstance().destroy();

        try {
            pool.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Pool termination timeout exeeded", e);
        }

        MjpgStreamer.stop();

        log.info("ServletContext destroyed..");
    }

}
