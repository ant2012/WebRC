package net.ant.rc.web;

import net.ant.rc.serial.CommPortException;
import net.ant.rc.serial.SerialCommunicator;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 04.02.13
 * Time: 23:34
 * To change this template use File | Settings | File Templates.
 */
public class SerialService implements Runnable {

    private final SerialCommunicator serialCommunicator;
    private final PriorityBlockingQueue<SerialCommand> commandQueue;

    @Override
    public void run() {

        while(true){
            try {
                SerialCommand serialCommand = commandQueue.take();
                if (serialCommand.commandType.equals("Digital")){
                    System.out.println(serialCommunicator.digitalCommandWithResult(serialCommand.x, serialCommand.y));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (CommPortException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
   }

    public SerialService(SerialCommunicator serialCommunicator, PriorityBlockingQueue<SerialCommand> commandQueue) {
        this.serialCommunicator = serialCommunicator;
        this.commandQueue = commandQueue;
    }
}
