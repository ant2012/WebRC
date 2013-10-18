package net.ant.rc.rpi;

import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 19.10.13
 * Time: 3:00
 */
public class Shell {
    public static String execute(String command){
        Logger logger = Logger.getLogger("log4j.logger.net.ant.rc");
        if (command == null) return null;

        if (!"reboot".equals(command)&&!"shutdown".equals(command)){
            return null;
        }

        try {
            Process p = Runtime.getRuntime().exec(new String[]{"/etc/init.d/" + command});
            byte[] buf = new byte[500];
            p.getInputStream().read(buf);
            return "Shell command result:" + new String(buf);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return e.getMessage();
        }
    }
}
