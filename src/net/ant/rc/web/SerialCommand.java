package net.ant.rc.web;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 05.02.13
 * Time: 0:09
 * To change this template use File | Settings | File Templates.
 */
public class SerialCommand {
    final public String commandType;
    final public int x;
    final public int y;

    public SerialCommand(String commandType, int x, int y) {
        this.commandType = commandType;
        this.x = x;
        this.y = y;
    }
}
