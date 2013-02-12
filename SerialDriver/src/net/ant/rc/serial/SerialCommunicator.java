package net.ant.rc.serial;

import gnu.io.*;
import net.ant.rc.serial.exception.CommPortException;

import java.io.*;
import java.util.Date;
import java.util.TooManyListenersException;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 29.01.13
 * Time: 2:21
 * To change this template use File | Settings | File Templates.
 */
public class SerialCommunicator implements SerialPortEventListener{
    private static final int NEW_LINE_ASCII = 10;
    private static final int LISTENER_INIT_TIMEOUT = 5000;

    private boolean isReadComplete = false;
    private byte[] receivedData = new byte[200];
    private int receivedCount = 0;
    private final SerialHardwareDetector serialHardwareDetector;

    //Constructor for cast to subclasses
    public SerialCommunicator(SerialCommunicator sc) {
        this.serialHardwareDetector = sc.serialHardwareDetector;
        try {
            //Reset listener from super to subclass
            initListener();
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
    }

    public SerialCommunicator(SerialHardwareDetector serialHardwareDetector) {
        this.serialHardwareDetector = serialHardwareDetector;
    }

    void initListener() throws TooManyListenersException {
        SerialPort serialPort = serialHardwareDetector.getSerialPort();
        serialPort.removeEventListener();
        serialPort.addEventListener(this);
        serialPort.notifyOnDataAvailable(true);
        try {
            Thread.sleep(LISTENER_INIT_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void readLineFromInput(){
        InputStream in = serialHardwareDetector.getIn();
        if (isReadComplete) return;
        byte byteOfData;
        try {
            while((byteOfData = (byte)in.read()) > -1){
                if (byteOfData == NEW_LINE_ASCII) {
                    isReadComplete = (receivedCount > 0);
                    break;
                } else {
                    receivedData[receivedCount++] = byteOfData;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE)
            readLineFromInput();
    }

    private String checkMessage() {
        readLineFromInput();
        String message = null;
        if (isReadComplete) {
            message = new String(receivedData, 0, receivedCount);
            receivedData = new byte[200];
            receivedCount = 0;
            isReadComplete = false;
        }
        return message;
    }

    private void sendMessage(String message){
        OutputStream out = serialHardwareDetector.getOut();
        try {
            out.write((message + "\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendCommand(String command) throws CommPortException {
        this.sendMessage(command);

        String message = null;
        long timestamp = new Date().getTime();
        while (message == null){
            message = this.checkMessage();
            if ((new Date().getTime() - timestamp)> LISTENER_INIT_TIMEOUT){
                throw new CommPortException("Answer timeout expired");
            }
        }
        return message;
    }

    public void disconnect(){
        this.serialHardwareDetector.disconnect();
    }
}
