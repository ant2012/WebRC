package ru.ant.iot.rpi;

import org.apache.log4j.Logger;
import ru.ant.iot.cloud.queue.CloudQueue;
import ru.ant.rc.serial.SerialService;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 19.10.13
 * Time: 3:00
 */
public class Shell {
    public static String execute(String command){
        if (command == null) return null;

        String[] c;
        switch (command){
            case "reboot":
                c = new String[]{"/usr/bin/sudo", "/sbin/reboot"};
                break;
            case "shutdown":
                c = new String[]{"/usr/bin/sudo", "/sbin/shutdown", "now"};
                break;
            case "temperature":
                c = new String[]{"cat", "/sys/class/thermal/thermal_zone0/temp"};
                break;
            case "toggleQueueActivity":
                CloudQueue.toggleActivity();
                return "Cloud queue monitoring was toggled";
//            case "switchSerialService":
//                SerialService serialService = SerialService.getInstance();
//                if(serialService.isStopped()){
//                    serialService.start();
//                    return "SerialService scheduled to start";
//                }
//                else{
//                    serialService.stopNowait();
//                    return "SerialService scheduled to stop";
//                }

            case "soundStart":
                c = new String[]{"/home/pi/sound.sh", "start"};
                break;
            case "soundStop":
                c = new String[]{"/home/pi/sound.sh", "stop"};
                break;
            default: return null;
        }

        return execute(c);
    }

    public static String execute(String[] c) {
        Logger logger = Logger.getLogger(Shell.class);
        try {
            Process p = Runtime.getRuntime().exec(c);
            byte[] buf = new byte[500];
            Thread.sleep(3000);
            if(p.exitValue()==1){
                p.getErrorStream().read(buf);
            }else{
                p.getInputStream().read(buf);
            }
            String result = new String(buf);
            logger.info(result);
            return result;
        } catch (IOException e) {
            logger.error(e.getMessage());
            return e.getMessage();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            return e.getMessage();
        }
    }
}
