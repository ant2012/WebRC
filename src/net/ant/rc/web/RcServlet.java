package net.ant.rc.web;

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
    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        int x = Integer.valueOf(request.getParameter("x"));
        int y = Integer.valueOf(request.getParameter("y"));
        long timeMillis = Long.valueOf(request.getParameter("milliseconds"));

        final ServletContext servletContext = request.getServletContext();

        PriorityBlockingQueue<SerialCommand> commandQueue = (PriorityBlockingQueue<SerialCommand>) servletContext.getAttribute("CommandQueue");
        commandQueue.put(new SerialCommand("Digital", x, y, timeMillis));

        System.out.println("RCServlet: x=" + String.valueOf(x) + "; y=" + String.valueOf(y));
    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        //String jsonData = request.getQueryString();

    }
}
