package ru.ant.rc.web; /**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 04.02.13
 * Time: 21:58
 * To change this template use File | Settings | File Templates.
 */

import ru.ant.common.App;
import ru.ant.common.Loggable;
import ru.ant.common.TriggerPoolManager;
import ru.ant.common.properties.WebPropertiesManager;
import ru.ant.iot.cloud.queue.JsonTaskFactory;
import ru.ant.iot.cloud.queue.JsonTaskTrigger;
import ru.ant.iot.cloud.queue.RpiTaskFactory;
import ru.ant.iot.ifttt.NewIpTrigger;
import ru.ant.rc.serial.Config;
import ru.ant.rc.serial.SerialService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class WebRCContextHolder extends Loggable implements ServletContextListener {

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

//        SerialService.getInstance().start();

        TriggerPoolManager.addTrigger(pool, new NewIpTrigger());
        JsonTaskFactory jsonTaskFactory = new RpiTaskFactory();
        TriggerPoolManager.addTrigger(pool, new JsonTaskTrigger(jsonTaskFactory));

    }

    public void contextDestroyed(ServletContextEvent sce) {
      /* This method is invoked when the Servlet Context 
         (the Web application) is undeployed or 
         Application Server shuts down.
      */
        log.info("Destroying ServletContext..");

        pool.shutdown();

        SerialService.getInstance().stop();

        try {
            pool.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Pool termination timeout exeeded", e);
        }

        log.info("ServletContext destroyed..");
    }

}
