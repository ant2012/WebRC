package ru.ant.iot.cloud.queue;

import ru.ant.iot.ifttt.TaskReportTrigger;
import ru.ant.iot.utils.WakeOnLan;

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
        String mac = json.getString("mac");
        String ip = null;
        if(json.containsKey("ip"))
            ip = json.getString("ip");
        WakeOnLan.send(mac, ip);
        new TaskReportTrigger(getClass(), String.format("[mac=%1$s; ip=%2$s]", mac, ip)).run();
        log.info("WOL sent to MAC=" + mac + ";IP=" + ip);
    }
}
