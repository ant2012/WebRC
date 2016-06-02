package ru.ant.iot.rpi;

import org.apache.log4j.Logger;

/**
 * Created by ant on 02.06.2016.
 */
public class MjpgStreamer {
    private static Logger log = Logger.getLogger(MjpgStreamer.class);

    public static void stop() {
        log.info("Stopping MJPG-streamer..");
        String[] c = new String[]{"/home/pi/mjpg-streamer/mjpg-streamer.sh", "stop"};
        Shell.execute(c);
    }

    public static void start(String resolution, String framerate) {
        log.info("Starting MJPG-streamer with resolution=["+resolution+"] and framerate=["+framerate+"]..");
        String[] c = new String[]{"/home/pi/mjpg-streamer/mjpg-streamer.sh", "start", "", ""};

        if(resolution!=null)
            c[2] = resolution;
        if(framerate!=null)
            c[3] = framerate;

        Shell.execute(c);
    }
}
