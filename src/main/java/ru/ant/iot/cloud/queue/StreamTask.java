package ru.ant.iot.cloud.queue;

import ru.ant.iot.ifttt.TaskReportTrigger;
import ru.ant.iot.rpi.MjpgStreamer;

import javax.json.JsonObject;

/**
 * Created by ant on 17.05.2016.
 */
public class StreamTask extends JsonTask {
    public StreamTask(JsonObject json) {
        this.json = json;
    }

    @Override
    public void execute() {

        String resolution = (json.containsKey("resolution"))?json.getString("resolution"):null;
        String framerate = (json.containsKey("framerate"))?json.getString("framerate"):null;

        MjpgStreamer.stop();
        MjpgStreamer.start(resolution, framerate);

        new TaskReportTrigger(getClass(), String.format("[%1$s %2$s]", resolution, framerate)).run();
    }

}
