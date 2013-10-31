<%@ page import="net.ant.rc.rpi.Shell" %>
<%@ page import="net.ant.rc.serial.SerialDriver" %>
<%@ page import="net.ant.rc.rpi.RpiState" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>WebRC Home</title>

    <!-- Bootstrap core CSS -->
    <link href="http://getbootstrap.com/dist/css/bootstrap.css" rel="stylesheet">
    <!-- Bootstrap theme -->
    <link href="http://getbootstrap.com/dist/css/bootstrap-theme.min.css" rel="stylesheet">

    <!-- Custom styles for this template -->
    <link href="http://getbootstrap.com/examples/theme/theme.css" rel="stylesheet">

    <!-- Just for debugging purposes. Don't actually copy this line! -->
    <!--[if lt IE 9]><script src="http://getbootstrap.com/docs-assets/js/ie8-responsive-file-warning.js"></script><![endif]-->

    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
    <script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
    <![endif]-->
</head>

<body>

<!-- Fixed navbar -->
<div class="navbar navbar-inverse navbar-fixed-top">
    <div class="container">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="/webrc/">WebRC</a>
        </div>
        <div class="navbar-collapse collapse">
            <ul class="nav navbar-nav">
                <li class="active"><a href="/webrc/">Home</a></li>
                <li><a href="tractorRC.html">TractorRC</a></li>
                <li><a href="vectorRC.html">VectorRC</a></li>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">RPi <b class="caret"></b></a>
                    <ul class="dropdown-menu">
                        <li><a href="?command=reboot">Reboot</a></li>
                        <li><a href="?command=shutdown">Shutdown</a></li>
                    </ul>
                </li>
            </ul>
        </div><!--/.nav-collapse -->
    </div>
</div>

<div class="container theme-showcase">

    <!-- Main jumbotron for a primary marketing message or call to action -->
    <div class="jumbotron">
        <h1>Hello, Master!</h1>
        <p>You are on WebRC Home. Here you can monitor and control your Robot.</p>
        <p><a href="tractorRC.html" title="Runs Tractor-style control as Default" class="btn btn-primary btn-lg" role="button">Let's drive! &raquo;</a></p>
    </div>


    <%
        final ServletContext servletContext = request.getServletContext();
        RpiState rpiState;
        Object o = servletContext.getAttribute("PriState");
        if (o == null){
            rpiState = new RpiState(servletContext);
            servletContext.setAttribute("RpiState", rpiState);
        }else{
            rpiState = (RpiState) o;
            rpiState.refresh();
        }

        String alertDangerText = rpiState.getErrorsHtml();

        SerialDriver serialDriver = (SerialDriver) servletContext.getAttribute("SerialDriver");
        String alertInfoText = "";
        double voltage = Double.NEGATIVE_INFINITY;
        double temperature = Double.NEGATIVE_INFINITY;
        if(serialDriver != null){
            alertInfoText = "Arduino2WD Robot successfully connected trough the SerialPort.";
            temperature = serialDriver.getChipTemperature() / 1000;
            voltage = serialDriver.getChipVoltage() / 1000;
        }else{
            if(!alertDangerText.equals(""))alertDangerText = alertDangerText + "<br>";
            alertDangerText = alertDangerText + "SerialDriver was not initialized! <!--a href=\"/webrc/servlet?do=Init\">Initialize it!</a-->";
        }
        String arduinoTemp = (Double.compare(temperature, Double.NEGATIVE_INFINITY)==0?"unavailable":temperature + "C&deg;");
        String arduinoVoltage = (Double.compare(voltage, Double.NEGATIVE_INFINITY)==0?"unavailable":voltage + "V");

    %>



    <div class="row">
        <div class="col-sm-4">
            <div class="panel panel-primary">
                <div class="panel-heading">
                    <h3 class="panel-title">Raspberry PI Info</h3>
                </div>
                <div class="panel-body">
                    Operating system:<strong><%out.println(rpiState.getOsDescription());%></strong><br>
                    JRE runner user:<strong><%out.println(rpiState.getOsUser());%></strong><br>
                    JRE:<strong><%out.println(rpiState.getJavaRuntimeDescription());%></strong><br>
                    Application Server:<strong><%out.println(rpiState.getServletEngineVersion());%></strong><br>
                    RXTX Library:<strong><%out.println(rpiState.getRXTXLibVersion());%></strong><br>
                    RXTX Native:<strong><%out.println(rpiState.getRXTXNativeVersion());%></strong><br>
                    RPi Timestamp:<strong><%out.println(rpiState.getSystemDateTime());%></strong><br>
                    RPi onboard temperature:<strong><%out.println(rpiState.getChipTemperature());%></strong>
                </div>
            </div>
        </div><!-- /.col-sm-4 -->
        <div class="col-sm-4">
            <div class="panel panel-primary">
                <div class="panel-heading">
                    <h3 class="panel-title">Arduino Info</h3>
                </div>
                <div class="panel-body">
                    Arduino onboard temperature:<strong><%out.println(arduinoTemp);%></strong><br>
                    Arduino onboard voltage:<strong><%out.println(arduinoVoltage);%></strong>
                </div>
            </div>
        </div><!-- /.col-sm-4 -->
        <div class="col-sm-4">
            <div class="panel panel-primary">
                <div class="panel-heading">
                    <h3 class="panel-title">WebRC Info</h3>
                </div>
                <div class="panel-body">
                    Panel content
                </div>
            </div>
        </div><!-- /.col-sm-4 -->
    </div>

    <%
        String command = request.getParameter("command");
        String shellResult = Shell.execute(command);
        if(shellResult != null){
            if(!alertInfoText.equals(""))alertInfoText = alertInfoText + "<br>";
            alertInfoText = alertInfoText + "<strong>Shell result for \"" + command + "\":</strong> " + shellResult;
        }
        if(!alertDangerText.equals("")){
            String alert =
                    "<div class=\"alert alert-danger\">\n" +
                            "    " + alertDangerText +
                            "</div>";
            out.println(alert);
        }
        if(!alertInfoText.equals("")){
            String alert =
                    "<div class=\"alert alert-info\">\n" +
                            "    " + alertInfoText +
                            "</div>";
            out.println(alert);
        }
    %>

</div> <!-- /container -->


<!-- Bootstrap core JavaScript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="https://code.jquery.com/jquery-1.10.2.min.js"></script>
<script src="http://getbootstrap.com/dist/js/bootstrap.min.js"></script>
<script src="http://getbootstrap.com/docs-assets/js/holder.js"></script>
</body>
</html>
