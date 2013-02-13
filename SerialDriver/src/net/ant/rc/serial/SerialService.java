package net.ant.rc.serial;

import net.ant.rc.serial.exception.CommPortException;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 04.02.13
 * Time: 23:34
 * To change this template use File | Settings | File Templates.
 */
public class SerialService implements Runnable {

    private final long MAX_QUEUE_SIZE = 20;
    private final long POLL_WAIT_TIMEOUT = 3000;
    private final SerialDriver serialDriver;
    private final PriorityBlockingQueue<VectorCommand> commandQueue;
    private final VectorCommand STOP = VectorCommand.STOP();
    private VectorCommand lastCommand = STOP;
    private boolean serviceStopped = false;

    @Override
    public void run() {

        while(!this.serviceStopped){
            try {
                VectorCommand vectorCommand = this.commandQueue.poll(POLL_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
                //If timeout was expired
                if (vectorCommand == null) {
                    //if last command was STOP then continue waiting, else go to send STOP
                    if (lastCommand.equals(STOP)) continue;
                }
                int queueSize = this.commandQueue.size();

                //Bypass the entries older then last sended
                if (vectorCommand.timeMillis < lastCommand.timeMillis){
                    System.out.println("Bypass1 value of [" + vectorCommand.x + "," + vectorCommand.y + "] for " + vectorCommand.timeMillis + " < " + lastCommand.timeMillis);
                    continue;
                }

                //Bypass the same command
                if (lastCommand.x == vectorCommand.x &&
                    lastCommand.y == vectorCommand.y){
                    System.out.println("Bypass2 value of [" + vectorCommand.x + "," + vectorCommand.y + "] already sent");
                    continue;
                }

                //Bypass entries if queue is too long
                if (queueSize > MAX_QUEUE_SIZE){
                    System.out.println("Bypass3 value of [" + vectorCommand.x + "," + vectorCommand.y + "] for " + queueSize + ">" + MAX_QUEUE_SIZE);
                    continue;
                }

                if (vectorCommand.commandType.equals("Digital")){
                    System.out.println(serialDriver.sendVectorCommand(vectorCommand.x, vectorCommand.y));
                    lastCommand = vectorCommand;
                }
            } catch (InterruptedException | CommPortException e) {
                e.printStackTrace();
            }
        }
   }

    public SerialService(SerialDriver serialDriver, PriorityBlockingQueue<VectorCommand> commandQueue) {
        this.serialDriver = serialDriver;
        this.commandQueue = commandQueue;
    }

    public void stop(){
        this.serviceStopped = true;
        this.serialDriver.disconnect();
    }
}
