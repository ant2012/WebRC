package net.ant.rc.web;

import net.ant.rc.serial.Command;
import net.ant.rc.serial.TractorCommand;
import net.ant.rc.serial.VectorCommand;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 03.02.13
 * Time: 19:09
 * To change this template use File | Settings | File Templates.
 */
public class RcServlet extends javax.servlet.http.HttpServlet {
    private final Logger logger = Logger.getLogger(this.getClass());

    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        final ServletContext servletContext = request.getServletContext();

        @SuppressWarnings("unchecked")//Yes, i thought wery hard :) servlet may just throw ClassCastException
        PriorityBlockingQueue<Command> commandQueue = (PriorityBlockingQueue<Command>) servletContext.getAttribute("CommandQueue");

        //SerialService may be stopped (In case of start failure)
        if (commandQueue == null) return;

        int x = Integer.valueOf(request.getParameter("x"));
        int y = Integer.valueOf(request.getParameter("y"));
        String commandType = request.getParameter("type");
        long timeMillis = Long.valueOf(request.getParameter("milliseconds"));

        if (commandType.equals("Vector")) {
            commandQueue.put(new VectorCommand("Digital", x, y, timeMillis));
        }
        if (commandType.equals("Tractor")) {
            commandQueue.put(new TractorCommand("Digital", x, y, timeMillis));
        }

        logger.info("RCServlet: x=" + String.valueOf(x) + "; y=" + String.valueOf(y));
    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        //String jsonData = request.getQueryString();

    }
}
