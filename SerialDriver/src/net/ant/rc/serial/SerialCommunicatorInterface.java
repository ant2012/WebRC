package net.ant.rc.serial;

import net.ant.rc.serial.exception.CommPortException;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 07.02.13
 * Time: 12:23
 * To change this template use File | Settings | File Templates.
 */
public interface SerialCommunicatorInterface {
    String sendVectorCommand(int x, int y) throws CommPortException;
    String sendEachWheelCommand(EachWheelCommand eachWheelCommand) throws CommPortException;
    void disconnect();
}
