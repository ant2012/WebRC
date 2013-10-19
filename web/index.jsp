<%@ page import="net.ant.rc.rpi.Shell" %>
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
            out.println(shellResult);
        }
    %>
</body>
</html>