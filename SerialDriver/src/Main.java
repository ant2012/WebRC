import net.ant.rc.serial.SerialDriver;
import net.ant.rc.serial.SerialHardwareDetector;
import net.ant.rc.serial.exception.CommPortException;
import net.ant.rc.serial.exception.UnsupportedHardwareException;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        File f = new File(".");
        String workingPath = f.getAbsolutePath();
        workingPath = workingPath.substring(0, workingPath.length()-1);

        SerialDriver serialDriver = null;
        try {
            SerialHardwareDetector serialHardwareDetector = new SerialHardwareDetector(workingPath);
            serialDriver = serialHardwareDetector.getSerialDriver();

            System.out.println(serialDriver.sendVectorCommand(0, 0));
        } catch (CommPortException | UnsupportedHardwareException e) {
            e.printStackTrace();
        } finally {
            if (serialDriver != null)
                serialDriver.disconnect();
        }
    }
}
