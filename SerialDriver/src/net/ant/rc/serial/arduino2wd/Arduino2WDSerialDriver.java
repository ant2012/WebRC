package net.ant.rc.serial.arduino2wd;

import net.ant.rc.serial.EachWheelCommand;
import net.ant.rc.serial.SerialDriver;
import net.ant.rc.serial.SerialHardwareDetector;
import net.ant.rc.serial.exception.CommPortException;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 07.02.13
 * Time: 11:03
 * To change this template use File | Settings | File Templates.
 */
public class Arduino2WDSerialDriver extends SerialDriver {

    int maxClientValue = 0; // X or Y
    int maxSpeed = 0; // speed is c = sqrt(x2+y2)

    public Arduino2WDSerialDriver(SerialHardwareDetector serialHardwareDetector) {
        super(serialHardwareDetector);
    }

    @Override
    public String sendVectorCommand(int x, int y) throws CommPortException {
        String command = generateDigitalCommand(x, -y);
        return this.serialCommunicator.sendCommand(command);
    }

    @Override
    public String sendEachWheelCommand(EachWheelCommand eachWheelCommand) throws CommPortException {
        Arduino2WDEachWheelCommand arduino2WDEachWheelCommand = (Arduino2WDEachWheelCommand)eachWheelCommand;
        return this.serialCommunicator.sendCommand(generateDigitalCommand(arduino2WDEachWheelCommand));
    }

    private String generateDigitalCommand(Arduino2WDEachWheelCommand arduino2WDEachWheelCommand) {
        return "Digital:" + arduino2WDEachWheelCommand.leftWheelSpeed + "," + arduino2WDEachWheelCommand.rightWheelSpeed;
    }

    private String generateDigitalCommand(int x, int y){

        //Save max values - it is self adaptation
        if(Math.abs(x) > this.maxClientValue)this.maxClientValue = Math.abs(x);
        if(Math.abs(y) > this.maxClientValue)this.maxClientValue = Math.abs(y);
        int c = (int) Math.round(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)));
        if(c > this.maxSpeed)this.maxSpeed = c;

        //Normalize speeds to range 0..255 using max value
        c = (c==0)?0:255 * c / this.maxSpeed;
        x = (this.maxClientValue==0)?0:255 * x / this.maxClientValue;
        y = (this.maxClientValue==0)?0:255 * y / this.maxClientValue;

        //Set direction sign
        int sign = (y<0)?-1:1;
        c = c * sign;

        //2WD transform from joystic Vector to wheel's speed
        int  leftWheelSpeed = 0;
        int rightWheelSpeed = 0;

        // (I) & (IV) quadrants
        if (x >= 0) {
            leftWheelSpeed = c;
            rightWheelSpeed = y;
        }
        // (II) & (III) quadrants
        if (x < 0) {
            rightWheelSpeed = c;
            leftWheelSpeed = y;
        }
        //Format is "Digital:leftWheelSpeed,rightWheelSpeed"
        return generateDigitalCommand(new Arduino2WDEachWheelCommand(leftWheelSpeed, rightWheelSpeed));
    }
}
