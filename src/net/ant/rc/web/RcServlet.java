package net.ant.rc.web;

import net.ant.rc.serial.*;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.PrintWriter;
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
            commandQueue.put(new VectorCommand(x, y, timeMillis));
        }
        if (commandType.equals("Tractor")) {
            commandQueue.put(new TractorCommand(x, y, timeMillis));
        }

        logger.info("RCServlet: x=" + String.valueOf(x) + "; y=" + String.valueOf(y));
    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        String command = request.getParameter("do");
        if(command != null && command.equals("Init")){
            final ServletContext servletContext = request.getServletContext();
            PrintWriter out = response.getWriter();

            SerialDriver serialDriver = (SerialDriver) servletContext.getAttribute("SerialDriver");
            if(serialDriver != null){
                out.println("<p>Already initialized</p>");
            }else{
                logger.info("Init Service Manually..");

                String workingPath = servletContext.getRealPath("/WEB-INF/classes/.");
                workingPath = workingPath.substring(0, workingPath.length()-1);

                PriorityBlockingQueue<Command> commandQueue = new PriorityBlockingQueue<Command>();

                SerialService serialService = new SerialService(commandQueue, workingPath);
                serialDriver = serialService.getSerialDriver();

                servletContext.setAttribute("CommandQueue", commandQueue);
                servletContext.setAttribute("SerialDriver", serialDriver);

                Thread serialServiceThread = new Thread(serialService);
                serialServiceThread.start();
            }
        }
    }
}
