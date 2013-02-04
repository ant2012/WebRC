package net.ant.rc.web;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import net.ant.rc.serial.CommPortException;
import net.ant.rc.serial.SerialCommunicator;

import java.io.IOException;
import java.util.TooManyListenersException;

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

        SerialCommunicator serialCommunicator = null;
        try {
            serialCommunicator = new SerialCommunicator();
            System.out.println(serialCommunicator.digitalCommandWithResult(x, y));
        } catch (CommPortException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NoSuchPortException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (PortInUseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (TooManyListenersException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if (serialCommunicator != null)
                serialCommunicator.disconnect();
        }
        System.out.println("x=" + String.valueOf(x) + "; y=" + String.valueOf(y));
    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        //String jsonData = request.getQueryString();

    }
}
