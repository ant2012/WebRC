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
    public final int HW_TYPE_ARDUINO_2WD = 1;

    private final String realPath;

    private boolean isReadComplete = false;
    private byte[] receivedData = new byte[200];
    private int receivedCount = 0;

    private SerialPort serialPort = null;
    private InputStream in = null;
    private OutputStream out = null;

    private final int NEW_LINE_ASCII = 10;
    private final int TIMEOUT = 5000;
    private final String configFileName = "arduino.conf";

    public SerialCommunicator(SerialCommunicator sc) {
        this.realPath = sc.realPath;
        this.isReadComplete = sc.isReadComplete;
        this.receivedData= sc.receivedData;
        this.receivedCount = sc.receivedCount;

        this.serialPort = sc.serialPort;
        this.in = sc.in;
        this.out = sc.out;
    }

    public SerialCommunicator() throws UnsupportedHardwareException, CommPortException {
        this.realPath = null;
        init();
    }

    public int getMovingHardwareType() {
        return movingHardwareType;
    }

    private int movingHardwareType = 0;

    public SerialCommunicator(String realPath) throws CommPortException, UnsupportedHardwareException {
        this.realPath = realPath;
        init();
    }

    private void init() throws CommPortException, UnsupportedHardwareException {
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
        getHardwareType();
    }

    private void getHardwareType() throws CommPortException, UnsupportedHardwareException {
        String result = sendCommand("hardware");
        if (result.startsWith("Arduino2WD"))
            movingHardwareType = HW_TYPE_ARDUINO_2WD;
        //Add new platform here

        if (movingHardwareType == 0) {
            if (result.startsWith("Arduino"))
               throw new UnsupportedHardwareException("Hardware of type " + result + " not supported");
            else
               throw new UnsupportedHardwareException("\"Hardware\" command is not supported by Firmware");
        }
        System.out.println("Hardware detected: " + result);
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
            e.printStackTrace();
        }
    }

    private String getConfiguredPortName() throws CommPortException {
        System.out.println("Searching for port name configuration..");
        Properties config = new Properties();
        String commPortName = null;
        InputStream in = null;
        System.out.println("First try loading from the current directory");
        try {
            in = new FileInputStream(configFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            if (in == null){
                System.out.println("Try loading from classpath related project root");
                in = this.getClass().getClassLoader()
                        .getResourceAsStream("../../" +configFileName);
            }
            config.load(in);
            in.close();
            commPortName = config.getProperty("CommPortName");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (commPortName==null){
            throw new CommPortException("CommPortName is not found in " + configFileName + " file!");
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

    private String checkMessage() {
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

    private void sendMessage(String message){
        try {
            out.write((message + "\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendCommand(String command) throws CommPortException {
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

    private void detectCommPort() throws CommPortException {
        if (this.serialPort != null)return;
        System.out.println("Trying to detect Arduino on any serial port..");
        Enumeration thePorts = CommPortIdentifier.getPortIdentifiers();
        while (thePorts.hasMoreElements()) {
            CommPortIdentifier commPortIdentifier = (CommPortIdentifier) thePorts.nextElement();
            String portName = commPortIdentifier.getName();
            if (commPortIdentifier.getPortType() != CommPortIdentifier.PORT_SERIAL)continue;
            System.out.println("Trying to open port " + commPortIdentifier.getName() + " as Serial");
            try {
                openSerialPort(commPortIdentifier);
            } catch (PortInUseException | CommPortException e) {
                e.printStackTrace();
            } catch (UnsupportedCommOperationException e) {
                this.serialPort = null;
                e.printStackTrace();
            }
            if (this.serialPort == null)continue;

            System.out.println("Port " + portName + " is available. Trying to work with it as Arduino.");
            //Check port by querying Firmware version
            try {
                initStreams();
                initListener();
                getFirmwareVersion(portName);
                saveDetectedPortConfiguration(portName);
                break;
            } catch (Exception e) {
                this.serialPort.close();
                this.serialPort = null;
                e.printStackTrace();
            }
        }
        if (this.serialPort == null) throw new CommPortException("Unable to detect Arduino on any COM port");
    }

    private void getFirmwareVersion(String portName) throws CommPortException {
        String fwVersion = sendCommand("version");
        if (fwVersion == null || !fwVersion.startsWith("Arduino"))
            throw new CommPortException("There is no Arduino on " + portName);
        System.out.println("Port " + portName + " looks like her magesty Arduino!");
        System.out.println("Detected: " + fwVersion);
    }

    private void saveDetectedPortConfiguration(String portName) {
        //TODO: Не работает, FileNotFoundException (Access is denied)
        System.out.println("Saving " + portName + " to configuration file for future runs");
        Properties config = new Properties();
        config.setProperty("CommPortName", portName);
        FileOutputStream out;
        try {
            String fileName = (this.realPath!=null)?realPath + "/" + configFileName:configFileName;
            out = new FileOutputStream(fileName);
            config.store(out, "Automatically detected port configuration");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
