package ru.ant.rc.web;

import org.apache.log4j.Logger;
import ru.ant.rc.serial.CommandQueue;
import ru.ant.rc.serial.SerialService;
import ru.ant.rc.serial.TractorCommand;
import ru.ant.rc.serial.VectorCommand;

import javax.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 03.02.13
 * Time: 19:09
 * To change this template use File | Settings | File Templates.
 */
@WebServlet(name = "RcServlet", urlPatterns = {"/servlet"})
public class RcServlet extends javax.servlet.http.HttpServlet {
    private final Logger logger = Logger.getLogger(this.getClass());

    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {

        CommandQueue commandQueue = SerialService.getInstance().getCommandQueue();

        int x = Integer.valueOf(request.getParameter("x").replaceAll("[\\.,].*", ""));
        int y = Integer.valueOf(request.getParameter("y").replaceAll("[\\.,].*", ""));
        String commandType = request.getParameter("type");
        long timeMillis = Long.valueOf(request.getParameter("milliseconds"));

        if (commandType.equals("Vector")) {
            commandQueue.put(new VectorCommand(x, y, timeMillis));
        }
        if (commandType.equals("Tractor")) {
            commandQueue.put(new TractorCommand(x, y, timeMillis));
        }

        logger.debug("RCServlet: x=" + String.valueOf(x) + "; y=" + String.valueOf(y));
    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        String command = request.getParameter("do");
        if(command != null && command.equals("Init")){
        }
    }

}
