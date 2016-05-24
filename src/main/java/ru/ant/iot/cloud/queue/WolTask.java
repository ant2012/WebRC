package ru.ant.iot.cloud.queue;

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
        String ip = json.getString("ip");
        WakeOnLan.send(mac, ip);
        log.info("WOL sent to MAC=" + mac + ";IP=" + ip);
    }
}
