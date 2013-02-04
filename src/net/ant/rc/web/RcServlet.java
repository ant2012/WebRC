package net.ant.rc.web;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import net.ant.rc.serial.CommPortException;
import net.ant.rc.serial.SerialCommunicator;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.TooManyListenersException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 03.02.13
 * Time: 19:09
 * To change this template use File | Settings | File Templates.
 */
public class RcServlet extends javax.servlet.http.HttpServlet {
    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        int x = Integer.valueOf(request.getParameter("x"));
        int y = Integer.valueOf(request.getParameter("y"));

        final ServletContext servletContext = request.getServletContext();

        LinkedBlockingQueue<SerialCommand> commandQueue = (LinkedBlockingQueue<SerialCommand>) servletContext.getAttribute("CommandQueue");
        try {
            commandQueue.put(new SerialCommand("Digital", x, y));
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        System.out.println("RCServlet: x=" + String.valueOf(x) + "; y=" + String.valueOf(y));
    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        //String jsonData = request.getQueryString();

    }
}
