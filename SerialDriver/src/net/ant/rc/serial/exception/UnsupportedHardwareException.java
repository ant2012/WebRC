package net.ant.rc.serial.exception;

public class UnsupportedHardwareException extends Exception{
    String message = null;
    public UnsupportedHardwareException(String hardwareType){
        this.message = "Hardware of type " + hardwareType + " not supported";
    }
    public String toString(){
        return message;
    }
}
