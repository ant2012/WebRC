package ru.ant.iot.rpi;

import gnu.io.RXTXVersion;

import javax.servlet.ServletContext;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 31.10.13
 * Time: 19:34
 * To change this template use File | Settings | File Templates.
 */
public class RpiState {
    private final String JAVA_RUNTIME_NAME="java.runtime.name";
    private final String JAVA_RUNTIME_VERSION="java.runtime.version";
    private final String JAVA_VENDOR="java.vendor";
    private final String OS_ARCH="os.arch";
    private final String OS_NAME="os.name";
    private final String OS_VERSION="os.version";
    private final String USER_NAME="user.name";

    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final Properties properties = System.getProperties();

    private final String servletEngineVersion;

    private double chipTemperature; //in milli Celsius
    private Date systemDate;

    private Vector<String> errors;

    public RpiState(ServletContext servletContext) {
        servletEngineVersion = servletContext.getServerInfo();
        refresh();
    }

    public void refresh(){
        errors = new Vector<>();
        setChipTemperature();
        systemDate = new Date();
    }

    private void setChipTemperature() {
        String shellResult = Shell.execute("temperature");
        chipTemperature = Double.NEGATIVE_INFINITY;
        try{
            chipTemperature = Double.parseDouble(shellResult);
        }catch(NumberFormatException e){
            errors.add("Can not get Temperature from the RPi. Shell result is \"" + shellResult + "\"");
        }
    }

    public String getChipTemperature() {
        if (Double.compare(chipTemperature, Double.NEGATIVE_INFINITY)==0){
            return "n/a";
        }else{
            return chipTemperature/1000 + "&deg;C";
        }
    }

    public String getSystemDateTime() {
        return dateTimeFormat.format(systemDate);
    }

    public String getErrorsHtml() {
        String result = "";
        boolean isFirstRow = true;
        for(int i = 0; i < errors.size(); i++){
            if(isFirstRow){
                result = errors.get(i);
                isFirstRow = false;
            }else{
                result = result + "<br>\n" + errors.get(i);
            }
        }
        return result;
    }

    private String getSystemProperty(String key){
        return properties.getProperty(key);
    }

    public String getJavaRuntimeDescription(){
        return getSystemProperty(JAVA_VENDOR) + " "
                + getSystemProperty(JAVA_RUNTIME_NAME) + " "
                + getSystemProperty(JAVA_RUNTIME_VERSION);
    }

    public String getOsDescription(){
        return getSystemProperty(OS_NAME) + " "
                + getSystemProperty(OS_ARCH) + " "
                + getSystemProperty(OS_VERSION);
    }

    public String getOsUser(){
        return getSystemProperty(USER_NAME);
    }

    public String getServletEngineVersion() {
        return servletEngineVersion;
    }

    public String getRXTXLibVersion() {
        return RXTXVersion.getVersion();
    }

    public String getRXTXNativeVersion() {
        return RXTXVersion.nativeGetVersion();
    }
}
