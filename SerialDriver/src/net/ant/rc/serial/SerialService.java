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

    private final long MAX_LAG_AALOWED = 2000;
    private final SerialCommunicator serialCommunicator;
    private final PriorityBlockingQueue<SerialCommand> commandQueue;
    private long lastCommandMillis = 0;

    @Override
    public void run() {

        while(true){
            try {
                SerialCommand serialCommand = commandQueue.take();

                //Bypass the entries older then last sended
                if (serialCommand.timeMillis < lastCommandMillis)continue;

                //Bypass enries older then the LAG bound
                if (serialCommand.timeMillis - lastCommandMillis > MAX_LAG_AALOWED)continue;

                lastCommandMillis = serialCommand.timeMillis;
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
