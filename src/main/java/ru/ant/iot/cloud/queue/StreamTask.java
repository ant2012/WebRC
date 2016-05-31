package ru.ant.iot.cloud.queue;

import ru.ant.iot.ifttt.TaskReportTrigger;
import ru.ant.iot.rpi.Shell;

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

        log.info("Stopping MJPG-streamer..");
        String[] c = new String[]{"/home/pi/mjpg-streamer/mjpg-streamer.sh", "stop"};
        Shell.execute(c);

        log.info("Starting MJPG-streamer with resolution=["+resolution+"] and framerate=["+framerate+"]..");
        c = new String[]{"/home/pi/mjpg-streamer/mjpg-streamer.sh", "start", "", ""};

        if(resolution!=null)
            c[2] = resolution;
        if(framerate!=null)
            c[3] = framerate;

        Shell.execute(c);

        new TaskReportTrigger(getClass(), String.format("[%1$s %2$s]", resolution, framerate)).run();
    }
}
