import net.ant.rc.serial.arduino2wd.Arduino2WDSerialCommunicator;
import net.ant.rc.serial.exception.CommPortException;
import net.ant.rc.serial.exception.UnsupportedHardwareException;

public class Main {

    public static void main(String[] args) {
        Arduino2WDSerialCommunicator serialCommunicator = null;
        try {
            serialCommunicator = new Arduino2WDSerialCommunicator();
            System.out.println(serialCommunicator.sendVectorCommand(-100, 0));
        } catch (CommPortException | UnsupportedHardwareException e) {
            e.printStackTrace();
        } finally {
            if (serialCommunicator != null)
                serialCommunicator.disconnect();
        }
    }
}
