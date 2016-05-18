package ru.ant.iot.cloud.queue;

import ru.ant.iot.ifttt.TaskReportTrigger;
import ru.ant.iot.rpi.Shell;

/**
 * Created by ant on 17.05.2016.
 */
public class RebootTask extends JsonTask {
    @Override
    public void execute() {
        new TaskReportTrigger(getClass()).run();
        Shell.execute("reboot");
        log.info("Reboot OS");
    }
}
