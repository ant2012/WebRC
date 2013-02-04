package net.ant.rc.serial;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 29.01.13
 * Time: 3:06
 * To change this template use File | Settings | File Templates.
 */
public class CommPortException extends Exception{
        String message = null;
        CommPortException(String message){
            this.message = message;
        }
        public String toString(){
            return message;
        }
    }

