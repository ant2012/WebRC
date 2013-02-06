import net.ant.rc.serial.CommPortException;
import net.ant.rc.serial.SerialCommunicator;

public class Main {

    public static void main(String[] args) {
        SerialCommunicator serialCommunicator = null;
        try {
            serialCommunicator = new SerialCommunicator();
            System.out.println(serialCommunicator.digitalCommandWithResult(-100,0));
        } catch (CommPortException e) {
            e.printStackTrace();
        } finally {
            if (serialCommunicator != null)
                serialCommunicator.disconnect();
        }
    }
}
