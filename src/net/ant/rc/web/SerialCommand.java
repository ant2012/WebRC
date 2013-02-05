package net.ant.rc.web;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 05.02.13
 * Time: 0:09
 * To change this template use File | Settings | File Templates.
 */
public class SerialCommand implements Comparable{
    final public String commandType;
    final public int x;
    final public int y;
    final public long timeMillis;

    public SerialCommand(String commandType, int x, int y, long timeMillis) {
        this.commandType = commandType;
        this.x = x;
        this.y = y;
        this.timeMillis = timeMillis;
    }

    @Override
    public int compareTo(Object o) {
        //this > object => 1
        int result = 0;
        SerialCommand obj = (SerialCommand)o;
        if (this.timeMillis == obj.timeMillis){
            result = 0;
        } else {
            result = (this.timeMillis > obj.timeMillis)?1:-1;
        }
        return result;
    }
}
