package net.ant.rc.serial;

import net.ant.rc.serial.exception.CommPortException;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 04.02.13
 * Time: 23:34
 * To change this template use File | Settings | File Templates.
 */
public class SerialService implements Runnable {

    private final long MAX_QUEUE_SIZE = 20;
    private final SerialCommunicatorInterface serialCommunicator;
    private final PriorityBlockingQueue<VectorCommand> commandQueue;
    private VectorCommand lastCommand = new VectorCommand("Digital", 0, 0, 0);
    private boolean serviceStopped = false;

    @Override
    public void run() {

        while(!this.serviceStopped){
            try {
                VectorCommand vectorCommand = this.commandQueue.take();
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
                    System.out.println(serialCommunicator.sendVectorCommand(vectorCommand.x, vectorCommand.y));
                    lastCommand = vectorCommand;
                }
            } catch (InterruptedException | CommPortException e) {
                e.printStackTrace();
            }
        }
   }

    public SerialService(SerialCommunicatorInterface serialCommunicator, PriorityBlockingQueue<VectorCommand> commandQueue) {
        this.serialCommunicator = serialCommunicator;
        this.commandQueue = commandQueue;
    }

    public void stop(){
        this.serviceStopped = true;
    }
}
