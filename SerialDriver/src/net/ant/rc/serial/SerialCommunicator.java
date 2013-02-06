package net.ant.rc.serial;

import gnu.io.*;

import java.io.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TooManyListenersException;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 29.01.13
 * Time: 2:21
 * To change this template use File | Settings | File Templates.
 */
public class SerialCommunicator implements SerialPortEventListener {
    boolean isReadComplete = false;
    byte[] receivedData = new byte[200];
    int receivedCount = 0;

    SerialPort serialPort = null;
    InputStream in = null;
    OutputStream out = null;

    final static int NEW_LINE_ASCII = 10;
    final static int TIMEOUT = 5000;

    public SerialCommunicator() throws CommPortException {
        //Trying to use configuration file "arduino.conf"
        try {
            String portName = getConfiguredPortName();
            openSerialPort(portName);
            initStreams();
            initListener();
            getFirmwareVersion(portName);
        } catch (CommPortException | PortInUseException | NoSuchPortException | UnsupportedCommOperationException | TooManyListenersException | IOException e) {
            e.printStackTrace();
        }
        detectCommPort();
    }

    private void initStreams() throws IOException {
        this.in = this.serialPort.getInputStream();
        this.out = this.serialPort.getOutputStream();
    }

    private void initListener() throws TooManyListenersException {
        this.serialPort.addEventListener(this);
        this.serialPort.notifyOnDataAvailable(true);
        try {
            Thread.sleep(TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private String getConfiguredPortName() throws CommPortException {
        System.out.println("Searching for port name configuration..");
        Properties config = new Properties();
        String commPortName = null;
        try {
            //InputStream in = new FileInputStream("arduino.conf");
            InputStream in = this.getClass().getClassLoader()
                    .getResourceAsStream("../../arduino.conf");
            if (in == null) in = new FileInputStream("arduino.conf");
            config.load(in);
            in.close();
            commPortName = config.getProperty("CommPortName");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (commPortName==null){
            throw new CommPortException("CommPortName is not found in arduino.conf file!");
        }
        return commPortName;
    }

    private void openSerialPort(String portName) throws NoSuchPortException, CommPortException, PortInUseException, UnsupportedCommOperationException {
        System.out.println("Getting PortID for \"" + portName + "\"..");
        CommPortIdentifier commPortIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        System.out.println("Checking port properties..");
        if(CommPortIdentifier.PORT_SERIAL!=commPortIdentifier.getPortType()){
            throw new CommPortException("Wrong port type. Serial port expected.");
        }
        if(commPortIdentifier.isCurrentlyOwned()){
            throw new CommPortException("Port is already in use.");
        }
        System.out.println("Opening port..");
        openSerialPort(commPortIdentifier);
    }

    private void openSerialPort(CommPortIdentifier commPortIdentifier) throws PortInUseException, CommPortException, UnsupportedCommOperationException {
        CommPort commPort = commPortIdentifier.open(this.getClass().getName(), 2000);
        System.out.println("Checking port properties..");
        if (!(commPort instanceof SerialPort)){
            commPort.close();
            throw new CommPortException("Wrong port type. Serial port expected.");
        }
        this.serialPort = (SerialPort) commPort;
        System.out.println("Setting up the port..");
        this.serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
    }

    private void readSomeBytesFromInput(){
        if (isReadComplete) return;
        byte singleData;
        try {
            while((singleData = (byte)in.read()) > -1){
                if (singleData != NEW_LINE_ASCII)
                {
                    receivedData[receivedCount++] = singleData;
                }
                else
                {
                    isReadComplete = (receivedCount > 0);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE)
        {
            readSomeBytesFromInput();
        }
    }

    public String checkMessage() {
        readSomeBytesFromInput();
        String message = null;
        if (isReadComplete) {
            message = new String(receivedData, 0, receivedCount);
            receivedData = new byte[200];
            receivedCount = 0;
            isReadComplete = false;
        }
        return message;
    }

    public void sendMessage(String message){
        try {
            out.write((message + "\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String commandWithResult(String command) throws CommPortException {
        //System.out.println(command);
        this.sendMessage(command);

        String message = null;
        long timestamp = new Date().getTime();
        while (message == null){
            message = this.checkMessage();
            if ((new Date().getTime() - timestamp)>TIMEOUT){
                throw new CommPortException("Answer timeout expired");
            }
        }
        return message;
    }

    int maxClientValue = 0; // X or Y
    int maxSpeed = 0; // speed is c = sqrt(x2+y2)

    public String digitalCommandWithResult(int x, int y) throws CommPortException {
        String command = generate2WDCommand(x, -y);
        return commandWithResult(command);
    }

    private String generate2WDCommand(int x, int y){

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
        return "Digital:" + leftWheelSpeed + "," + rightWheelSpeed;
    }

    public void disconnect()
    {
            serialPort.removeEventListener();
            serialPort.close();
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void detectCommPort() throws CommPortException {
        if (this.serialPort != null)return;
        System.out.println("Trying to detect Arduino on any serial port..");
        Enumeration thePorts = CommPortIdentifier.getPortIdentifiers();
        while (thePorts.hasMoreElements()) {
            CommPortIdentifier commPortIdentifier = (CommPortIdentifier) thePorts.nextElement();
            if (commPortIdentifier.getPortType() != CommPortIdentifier.PORT_SERIAL)continue;
            System.out.println("Trying to open port " + commPortIdentifier.getName() + " as Serial");
            try {
                openSerialPort(commPortIdentifier);
            } catch (PortInUseException | CommPortException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (UnsupportedCommOperationException e) {
                this.serialPort = null;
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            if (this.serialPort == null)continue;

            //Check port by querying Firmware version
            try {
                initStreams();
                initListener();
                getFirmwareVersion(commPortIdentifier.getName());
                saveDetectedPortConfiguration(commPortIdentifier.getName());
            } catch (Exception e) {
                this.serialPort.close();
                this.serialPort = null;
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        if (this.serialPort == null) throw new CommPortException("Unable to detect Arduino on any COM port");
    }

    private void getFirmwareVersion(String portName) throws CommPortException {
        String fwVersion = commandWithResult("version");
        System.out.println("Port " + portName + " looks like her magesty Arduino!");
        System.out.println("Detected: " + fwVersion);
    }

    private void saveDetectedPortConfiguration(String portName) {
        //TODO: Save detected portName to file "arduino.conf"
    }
}
