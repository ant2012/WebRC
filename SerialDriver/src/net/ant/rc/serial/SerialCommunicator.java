package net.ant.rc.serial;

import gnu.io.*;
import net.ant.rc.serial.exception.CommPortException;
import net.ant.rc.serial.exception.UnsupportedHardwareException;

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
public class SerialCommunicator implements SerialPortEventListener{
    public static final int CHASSIS_TYPE_UNDEFINED = 0;
    public static final int CHASSIS_TYPE_ARDUINO_2WD = 1;

    private static final int NEW_LINE_ASCII = 10;
    private static final int LISTENER_INIT_TIMEOUT = 5000;
    private static final int COMM_OPEN_TIMEOUT = 2000;
    private static final String configFileName = "serial.conf";

    private final String realPath;
    private final int chassisType;

    private String portName;
    private SerialPort serialPort;
    private InputStream in;
    private OutputStream out;

    private boolean isReadComplete = false;
    private byte[] receivedData = new byte[200];
    private int receivedCount = 0;

    //Constructor for cast to subclasses
    public SerialCommunicator(SerialCommunicator sc) {
        this.realPath = sc.realPath;
        this.portName = sc.portName;
        this.serialPort = sc.serialPort;
        this.in = sc.in;
        this.out = sc.out;
        try {
            //Reset listener from super to subclass
            initListener();
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
        /*
        this.isReadComplete = sc.isReadComplete;
        this.receivedData= sc.receivedData;
        this.receivedCount = sc.receivedCount;
        */
        this.chassisType = sc.chassisType;
    }

    public SerialCommunicator() throws UnsupportedHardwareException, CommPortException {
        File f = new File(".");
        String realPath = f.getAbsolutePath();
        realPath = realPath.substring(0, realPath.length()-1);
        this.realPath = realPath;

        checkSavedPortName();

        if (serialPort == null) {
            detectCommPort();
        }

        this.chassisType = detectChassisType();
    }

    private void checkSavedPortName(){
        try {
            getSavedPortName();
            CommPortIdentifier commPortIdentifier = getPortIdentifier();
            checkPort(commPortIdentifier);
            //Check port by querying Firmware version
            checkFirmwareVersion();
        } catch (NoSuchPortException | CommPortException e) {
            e.printStackTrace();
        }
    }

    private int detectChassisType() throws CommPortException, UnsupportedHardwareException {
        String result = sendCommand("hardware");
        int chassisType = CHASSIS_TYPE_UNDEFINED;
        if (result.startsWith("Arduino2WD"))
            chassisType = CHASSIS_TYPE_ARDUINO_2WD;
        //Add new platform here

        if (chassisType == CHASSIS_TYPE_UNDEFINED) {
            if (result.startsWith("Arduino"))
               throw new UnsupportedHardwareException("Chassis of type " + result + " is not supported by this driver version");
            else
               throw new UnsupportedHardwareException("\"Hardware\" command is not supported by Firmware");
        }
        System.out.println("Hardware detected: " + result);
        return chassisType;
    }

    private void initStreams() throws IOException {
        in = serialPort.getInputStream();
        out = serialPort.getOutputStream();
    }

    private void initListener() throws TooManyListenersException {
        serialPort.removeEventListener();
        serialPort.addEventListener(this);
        serialPort.notifyOnDataAvailable(true);
        try {
            Thread.sleep(LISTENER_INIT_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getSavedPortName() throws CommPortException {
        System.out.println("Searching for port name configuration..");
        //System.out.println("Load properties: " + this.realPath + "/" + this.configFileName);
        Properties config = new Properties();
        //System.out.println("First try loading from the current directory");
        try {
            InputStream in = new FileInputStream(this.realPath + this.configFileName);
            config.load(in);
            in.close();
            portName = config.getProperty("CommPortName");
        } catch (IOException e) {
            e.printStackTrace();
        }
/*
        try {
            if (in == null){
                System.out.println("Try loading from classpath related project root");
                in = this.getClass().getClassLoader()
                        .getResourceAsStream("../../" +configFileName);
            }
            config.load(in);
            in.close();
            portName = config.getProperty("CommPortName");
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        if (portName==null){
            throw new CommPortException("CommPortName is not found in " + this.realPath + this.configFileName);
        }
    }

    private CommPortIdentifier getPortIdentifier() throws CommPortException, NoSuchPortException {
        System.out.println("Getting PortID for \"" + portName + "\"..");
        CommPortIdentifier commPortIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        checkPortProperties(commPortIdentifier);
        return commPortIdentifier;
    }

    private void checkPortProperties(CommPortIdentifier commPortIdentifier) throws CommPortException {
        System.out.println("Checking port properties..");
        if(CommPortIdentifier.PORT_SERIAL!=commPortIdentifier.getPortType()){
            throw new CommPortException("Wrong port type. Serial port expected.");
        }
        if(commPortIdentifier.isCurrentlyOwned()){
            throw new CommPortException("Port is already in use.");
        }
    }
    private void openSerialPort(CommPortIdentifier commPortIdentifier) throws PortInUseException, UnsupportedCommOperationException, CommPortException {
        System.out.println("Opening port..");
        CommPort commPort = commPortIdentifier.open(this.getClass().getName(), COMM_OPEN_TIMEOUT);
        System.out.println("Checking port properties..");
        if (!(commPort instanceof SerialPort)){
            commPort.close();
            throw new CommPortException("Wrong port type. Serial port expected.");
        }
        serialPort = (SerialPort) commPort;
        System.out.println("Setting up the port..");
        serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
    }

    private void readLineFromInput(){
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

    public void disconnect()
    {
        this.serialPort.removeEventListener();
        this.serialPort.close();
        try {
            this.in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String detectCommPort() throws CommPortException {
        System.out.println("Trying to detect Arduino on any serial port..");
        Enumeration thePorts = CommPortIdentifier.getPortIdentifiers();
        while (thePorts.hasMoreElements()) {
            CommPortIdentifier commPortIdentifier = (CommPortIdentifier) thePorts.nextElement();
            try {
                portName = commPortIdentifier.getName();
                checkPortProperties(commPortIdentifier);
                checkPort(commPortIdentifier);
                if (serialPort == null) continue;

                //Check port by querying Firmware version
                checkFirmwareVersion();
                saveDetectedPortConfiguration();
                break;
            } catch (CommPortException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        if (serialPort == null) throw new CommPortException("Unable to detect Arduino on any COM port");
        return portName;
    }

    private void clearPortAttributes(){
        this.portName = null;
        this.serialPort = null;
        this.in = null;
        this.out = null;
    }

    private void checkPort(CommPortIdentifier commPortIdentifier) {
        System.out.println("Checking port " + portName);
        try {
            openSerialPort(commPortIdentifier);
            System.out.println("Port " + portName + " is available. Trying to work with it as Arduino.");
            initStreams();
            initListener();
        } catch (UnsupportedCommOperationException | TooManyListenersException | IOException e) {
            if (serialPort != null)serialPort.close();
            clearPortAttributes();
            e.printStackTrace();
        } catch (PortInUseException | CommPortException e) {
            e.printStackTrace();
        }
    }

    private void checkFirmwareVersion() throws CommPortException {
        String fwVersion = null;
        try {
            fwVersion = sendCommand("version");
        } catch (CommPortException e) {
            e.printStackTrace();
        }
        if (fwVersion == null || !fwVersion.startsWith("Arduino")) {
            disconnect();
            String portName = this.portName;
            clearPortAttributes();
            throw new CommPortException("There is no Arduino on " + portName);
        }
        System.out.println("Port " + this.portName + " looks like her magesty Arduino!");
        System.out.println("Detected: " + fwVersion);
    }

    private void saveDetectedPortConfiguration() {
        //TODO: Не работает, FileNotFoundException (Access is denied)
        System.out.println("Saving " + portName + " to configuration file for future runs");
        Properties config = new Properties();
        config.setProperty("CommPortName", portName);
        FileOutputStream out;
        try {
            String fileName = realPath + configFileName;
            out = new FileOutputStream(fileName);
            config.store(out, "Automatically detected port configuration");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getChassisType() {
        return chassisType;
    }

}
