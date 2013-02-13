package net.ant.rc.serial;

import net.ant.rc.serial.exception.CommPortException;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 07.02.13
 * Time: 12:23
 * To change this template use File | Settings | File Templates.
 */
public abstract class SerialDriver {
    public final SerialCommunicator serialCommunicator;
    private final SerialHardwareDetector serialHardwareDetector;

    public abstract String sendVectorCommand(int x, int y) throws CommPortException;
    public abstract String sendEachWheelCommand(EachWheelCommand eachWheelCommand) throws CommPortException;

    public SerialDriver(SerialHardwareDetector serialHardwareDetector) {
        this.serialHardwareDetector = serialHardwareDetector;
        this.serialCommunicator = serialHardwareDetector.getSerialCommunicator();
    }

    public void disconnect(){
        this.serialHardwareDetector.disconnect();
    }
}
