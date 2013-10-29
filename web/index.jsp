<%@ page import="net.ant.rc.rpi.Shell" %>
<%@ page import="net.ant.rc.serial.SerialDriver" %>
<%--
  Created by IntelliJ IDEA.
  User: Ant
  Date: 17.02.13
  Time: 1:10
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>RC Index</title>
</head>
<body>
    <p><a href="/webrc">Home</a></p>
    <!--p><a href="vectorRC.html">VectorRC</a></p-->
    <p><a href="tractorRC.html">TractorRC</a></p>
    <!--iframe src="http://embed.bambuser.com/channel/webrc" width="460" height="396" frameborder="0">Your browser does not support iframes.</iframe-->
    <p><a href="?command=reboot">Reboot RaspberryPI</a></p>
    <p><a href="?command=shutdown">Shutdown RaspberryPI</a></p>
    <%
        String shellResult = Shell.execute(request.getParameter("command"));
        if(shellResult != null){
            out.println("<p>Shell result: " + shellResult + "</p>");
        }
    %>
    <%
        shellResult = Shell.execute("temperature");
        Double temperature = Double.parseDouble(shellResult) / 1000;
        if(shellResult != null){
            out.println("<p>RPi onboard temperature: " + temperature + "C&deg;</p>");
        }
        final ServletContext servletContext = request.getServletContext();

        SerialDriver serialDriver = (SerialDriver) servletContext.getAttribute("SerialDriver");
        if(serialDriver != null){
            double t = serialDriver.getChipTemperature() / 1000;
            out.println("<p>Arduino onboard temperature: " + t + "C&deg;</p>");
            t = serialDriver.getChipVoltage() / 1000;
            out.println("<p>Arduino onboard voltage: " + t + "V</p>");
        }else{
            out.println("<p>SerialDriver was not initialized. <a href=\"/webrc/servlet?do=Init\">Initialize it!</a></p>");
        }
    %>
</body>
</html>