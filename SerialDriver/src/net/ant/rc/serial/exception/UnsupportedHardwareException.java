package net.ant.rc.serial.exception;

public class UnsupportedHardwareException extends Exception{
    String message = null;
    public UnsupportedHardwareException(String message){
        this.message = message;
    }
    public String toString(){
        return message;
    }
}
