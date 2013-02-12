import net.ant.rc.serial.SerialHardwareDetector;
import net.ant.rc.serial.arduino2wd.Arduino2WDSerialCommunicator;
import net.ant.rc.serial.exception.CommPortException;
import net.ant.rc.serial.exception.UnsupportedHardwareException;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        File f = new File(".");
        String workingPath = f.getAbsolutePath();
        workingPath = workingPath.substring(0, workingPath.length()-1);

        Arduino2WDSerialCommunicator serialCommunicator = null;
        try {
            SerialHardwareDetector serialHardwareDetector = new SerialHardwareDetector(workingPath);
            serialCommunicator = (Arduino2WDSerialCommunicator) serialHardwareDetector.getSerialCommunicator();

            System.out.println(serialCommunicator.sendVectorCommand(-100, 0));
        } catch (CommPortException | UnsupportedHardwareException e) {
            e.printStackTrace();
        } finally {
            if (serialCommunicator != null)
                serialCommunicator.disconnect();
        }
    }
}
