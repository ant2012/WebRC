package net.ant.rc.serial;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 04.02.13
 * Time: 23:34
 * To change this template use File | Settings | File Templates.
 */
public class SerialService implements Runnable {

    //private final long MAX_LAG_AALOWED = 2000;
    private final SerialCommunicator serialCommunicator;
    private final PriorityBlockingQueue<SerialCommand> commandQueue;
    private SerialCommand lastCommand = new SerialCommand("Digital", 0, 0, 0);
    private boolean serviceStopped = false;

    @Override
    public void run() {

        while(!this.serviceStopped){
            try {
                SerialCommand serialCommand = commandQueue.take();

                //Bypass the entries older then last sended
                if (serialCommand.timeMillis < lastCommand.timeMillis){
                    System.out.println("Bypass1 value of [" + serialCommand.x + "," + serialCommand.y + "] for " + serialCommand.timeMillis + " < " + lastCommand.timeMillis);
                    continue;
                }
                /* incorrect algorithm!!!
                if (lastCommand.timeMillis != 0 &&
                        serialCommand.timeMillis - lastCommand.timeMillis > MAX_LAG_AALOWED){

                    System.out.println("Bypass2 value of [" + serialCommand.x + "," + serialCommand.y + "] for " + serialCommand.timeMillis + " - " + lastCommand.timeMillis + ">" + MAX_LAG_AALOWED);
                    continue;
                }
                */

                //Bypass the same command
                if (lastCommand.x == serialCommand.x &&
                    lastCommand.y == serialCommand.y){
                    System.out.println("Bypass3 value of [" + serialCommand.x + "," + serialCommand.y + "] already sent");
                    continue;
                }

                lastCommand = serialCommand;

                //Bypass enries older then the LAG bound
                if (serialCommand.commandType.equals("Digital")){
                    System.out.println(serialCommunicator.digitalCommandWithResult(serialCommand.x, serialCommand.y));
                }
            } catch (InterruptedException | CommPortException e) {
                e.printStackTrace();
            }
        }
   }

    public SerialService(SerialCommunicator serialCommunicator, PriorityBlockingQueue<SerialCommand> commandQueue) {
        this.serialCommunicator = serialCommunicator;
        this.commandQueue = commandQueue;
    }

    public void stop(){
        this.serviceStopped = true;
    }
}
