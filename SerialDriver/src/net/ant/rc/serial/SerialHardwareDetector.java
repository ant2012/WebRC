package net.ant.rc.serial;

import gnu.io.*;
import net.ant.rc.serial.arduino2wd.Arduino2WDSerialDriver;
import net.ant.rc.serial.exception.CommPortException;
import net.ant.rc.serial.exception.UnsupportedHardwareException;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TooManyListenersException;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 09.02.13
 * Time: 10:14
 * To change this template use File | Settings | File Templates.
 */
public class SerialHardwareDetector {
    public static final int CHASSIS_TYPE_UNDEFINED = 0;
    public static final int CHASSIS_TYPE_ARDUINO_2WD = 1;
    public static final int COMM_OPEN_TIMEOUT = 2000;
    private static final String configFileName = "serial.conf";

    private final String workingPath;
    private final int chassisType;

    private String portName;

    public SerialPort getSerialPort() {
        return serialPort;
    }

    private SerialPort serialPort;

    public InputStream getIn() {
        return in;
    }

    public OutputStream getOut() {
        return out;
    }

    private InputStream in;
    private OutputStream out;

    public SerialCommunicator getSerialCommunicator() {
        return serialCommunicator;
    }

    private final SerialCommunicator serialCommunicator;

    public SerialDriver getSerialDriver() {
        return serialDriver;
    }

    private SerialDriver serialDriver;

    public SerialHardwareDetector(String workingPath) throws CommPortException, UnsupportedHardwareException {
        this.serialCommunicator = new SerialCommunicator(this);
        this.workingPath = workingPath;
        testWorkingPath();

        checkSavedPortName();

        if (serialPort == null) {
            detectCommPort();
        }

        this.chassisType = detectChassisType();

        if (this.chassisType == this.CHASSIS_TYPE_ARDUINO_2WD)
            this.serialDriver = new Arduino2WDSerialDriver(this);
        //Add new hardware here

    }

    private void testWorkingPath() throws CommPortException {
        try {
            String fileName = this.workingPath + "test.conf";
            FileOutputStream o = new FileOutputStream(fileName);
            Properties p = new Properties();
            p.setProperty("test", "Test");
            p.store(o, "Test workingPath");
            o.close();

            //Cleanup
            File f = new File(fileName);
            f.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void getSavedPortName() throws CommPortException {
        System.out.println("Searching for port name configuration..");
        //System.out.println("Load properties: " + this.workingPath + "/" + this.configFileName);
        Properties config = new Properties();
        //System.out.println("First try loading from the current directory");
        try {
            InputStream in = new FileInputStream(this.workingPath + this.configFileName);
            config.load(in);
            in.close();
            portName = config.getProperty("CommPortName");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (portName==null){
            throw new CommPortException("CommPortName is not found in " + this.workingPath + this.configFileName);
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

    private void checkPort(CommPortIdentifier commPortIdentifier) {
        System.out.println("Checking port " + portName);
        try {
            openSerialPort(commPortIdentifier);
            System.out.println("Port " + portName + " is available. Trying to work with it as Arduino.");
            initStreams();
            this.serialCommunicator.initListener();
        } catch (UnsupportedCommOperationException | TooManyListenersException | IOException e) {
            if (serialPort != null)serialPort.close();
            clearPortAttributes();
            e.printStackTrace();
        } catch (PortInUseException | CommPortException e) {
            e.printStackTrace();
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

    private void initStreams() throws IOException {
        in = serialPort.getInputStream();
        out = serialPort.getOutputStream();
    }

    private void clearPortAttributes(){
        this.portName = null;
        this.serialPort = null;
        this.in = null;
        this.out = null;
    }

    private void checkFirmwareVersion() throws CommPortException {
        String fwVersion = null;
        try {
            fwVersion = this.serialCommunicator.sendCommand("version");
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
                e.printStackTrace();
            }
        }
        if (serialPort == null) throw new CommPortException("Unable to detect Arduino on any COM port");
        return portName;
    }

    private void saveDetectedPortConfiguration() {
        System.out.println("Saving " + portName + " to configuration file for future runs");
        Properties config = new Properties();
        config.setProperty("CommPortName", portName);
        FileOutputStream out;
        try {
            String fileName = workingPath + configFileName;
            out = new FileOutputStream(fileName);
            config.store(out, "Automatically detected port configuration");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int detectChassisType() throws CommPortException, UnsupportedHardwareException {
        String result = serialCommunicator.sendCommand("hardware");
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

}
