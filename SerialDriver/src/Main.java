import gnu.io.*;
import net.ant.rc.serial.CommPortException;
import net.ant.rc.serial.SerialCommunicator;

import java.io.*;
import java.util.TooManyListenersException;

public class Main {

    public static void main(String[] args) {
        SerialCommunicator serialCommunicator = null;
        try {
            serialCommunicator = new SerialCommunicator();

            System.out.println(serialCommunicator.commandWithResult("version"));
            System.out.println(serialCommunicator.digitalCommandWithResult(-100,0));
        } catch (CommPortException e) {
            e.printStackTrace();
        } catch (NoSuchPortException e) {
            e.printStackTrace();
        } catch (PortInUseException e) {
            e.printStackTrace();
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        } finally {
            if (serialCommunicator != null)
                serialCommunicator.disconnect();
        }
    }
}
