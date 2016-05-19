package ru.ant.iot.cloud.queue;

import ru.ant.common.net.WakeOnLan;
import ru.ant.iot.ifttt.TaskReportTrigger;

import javax.json.JsonObject;

/**
 * Created by ant on 17.05.2016.
 */
public class WolTask extends JsonTask {
    public WolTask(JsonObject json) {
        this.json = json;
    }

    @Override
    public void execute() {
        new TaskReportTrigger(getClass()).run();
        String mac = json.getString("mac");
        new WakeOnLan(mac);
        log.info("WOL sent to " + mac);
    }
}
