<%@ page import="ru.ant.iot.rpi.RpiState" %>
<%@ page import="ru.ant.iot.rpi.Shell" %>
<%@ page import="ru.ant.rc.serial.*" %>
<%@ page import="java.util.concurrent.PriorityBlockingQueue" %>
<%@ page import="ru.ant.iot.cloud.queue.CloudQueue" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.apache.commons.lang.time.DateFormatUtils" %>
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
    <link href="https://getbootstrap.com/dist/css/bootstrap.css" rel="stylesheet">
    <!-- Bootstrap theme -->
    <link href="https://getbootstrap.com/dist/css/bootstrap-theme.min.css" rel="stylesheet">

    <!-- Custom styles for this template -->
    <link href="https://getbootstrap.com/examples/theme/theme.css" rel="stylesheet">

    <!-- Just for debugging purposes. Don't actually copy this line! -->
    <!--[if lt IE 9]>
    <!--<script src="http://getbootstrap.com/docs-assets/js/ie8-responsive-file-warning.js"></script>--><![endif]-->

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
                <li><a href="sound.html">Sound</a></li>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">RPi <b class="caret"></b></a>
                    <ul class="dropdown-menu">
                        <li><a href="?command=reboot">Reboot</a></li>
                        <li><a href="?command=shutdown">Shutdown</a></li>
                        <li><a href="?command=toggleQueueActivity">Switch Queue</a></li>
                    </ul>
                </li>
            </ul>
        </div><!--/.nav-collapse -->
    </div>
</div>

<div class="container theme-showcase">

    <!-- Main jumbotron for a primary marketing message or call to action -->
    <div class="jumbotron">
        <h1>HelloMaster!</h1>
        <p>You are on WebRC Home. Here you can monitor and control your Robot.</p>
        <p><a href="tractorRC.html" title="Runs Tractor-style control as Default" class="btn btn-primary btn-lg"
              role="button">Let's drive! &raquo;</a></p>
    </div>
    <div class="row">
        <%
            String command = request.getParameter("command");
            String shellResult = Shell.execute(command);

            final ServletContext servletContext = request.getServletContext();

            //Raspberry Info
            RpiState rpiState;
            Object o = servletContext.getAttribute("RpiState");
            if (o == null) {
                rpiState = new RpiState(servletContext);
                servletContext.setAttribute("RpiState", rpiState);
            } else {
                rpiState = (RpiState) o;
                rpiState.refresh();
            }

            String alertDangerText = rpiState.getErrorsHtml();
        %>

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
                    Timestamp:<strong><%out.println(rpiState.getSystemDateTime());%></strong><br>
                    RPi onboard temperature:<strong><%out.println(rpiState.getChipTemperature());%></strong>
                </div>
            </div>
        </div><!-- /.col-sm-4 -->

        <%  //Arduino info
            SerialDriver serialDriver = SerialService.getInstance().getSerialDriver();
            String alertInfoText = "";

            //WebRC info
            double voltage = Double.NEGATIVE_INFINITY;
            double temperature = Double.NEGATIVE_INFINITY;
            String batteryLevel = "n/a";
            String fwVersion = null;
            int sketchSize = -1;
            Date buildDate = null;
            int upTime = 0;
            if (serialDriver != null) {
                ArduinoState arduinoState = serialDriver.getArduinoState();
                if (arduinoState != null) {
                    temperature = arduinoState.getTemperature() / 1000;
                    Battery battery = arduinoState.getBattery();
                    if (battery != null) {
                        batteryLevel = String.valueOf(battery.checkVoltageLevel()) + "%";
                        voltage = battery.getCurrentVoltage() / 1000;
                    }
                    fwVersion = arduinoState.getFirmwareVersion();
                    sketchSize = arduinoState.getSketchSize();
                    buildDate = arduinoState.getBuildDate();
                    upTime = arduinoState.getUpTime();
                    //arduinoState.
                }
            }
            String arduinoTemp = (Double.compare(temperature, Double.NEGATIVE_INFINITY) == 0 ? "n/a" : temperature + "&deg;C");
            String arduinoVoltage = (Double.compare(voltage, Double.NEGATIVE_INFINITY) == 0 ? "n/a" : voltage + "V");
            String fwVersionStr = (fwVersion == null) ? "n/a" : fwVersion;
            String sketchSizeStr = (sketchSize < 0) ? "n/a" : String.valueOf(sketchSize)+"B";
            String buildDateStr = (buildDate == null) ? "n/a" : DateFormatUtils.format(buildDate, "MMM d yyyy HH:mm:ss");
            String upTimeStr = (upTime < 0) ? "n/a" : String.valueOf(upTime)+"ms";

        %>
        <div class="col-sm-4">
            <div class="panel panel-primary">
                <div class="panel-heading">
                    <h3 class="panel-title">Arduino Info</h3>
                </div>
                <div class="panel-body">
                    Arduino onboard temperature:<strong><%out.println(arduinoTemp);%></strong><br>
                    Arduino onboard voltage:<strong><%out.println(arduinoVoltage);%></strong><br>
                    Battery level:<strong><%out.println(batteryLevel);%></strong><br/>
                    FW version:<strong><%out.println(fwVersionStr);%></strong><br/>
                    Sketch size:<strong><%out.println(sketchSizeStr);%></strong><br/>
                    Build date:<strong><%out.println(buildDateStr);%></strong><br/>
                    Up time:<strong><%out.println(upTimeStr);%></strong><br/>
                    SerialService:<strong><%out.println(SerialService.getInstance().getStatus());%></strong>
                </div>
            </div>
        </div><!-- /.col-sm-4 -->

        <%  //WebRC info
            String serialPortName = "n/a";
            String batteryMin = "n/a";
            String batteryMax = "n/a";
            String serialListenerTimeout = "n/a";
            String serialPortInternalTimeout = "n/a";
            String hardwareSensorRefreshPeriod = "n/a";
            String serialDriverClass = "n/a";
            String serviceMaxQueueSize = "n/a";
            String servicePollWaitTimeout = "n/a";
            String serviceReconnectTimeout = "n/a";
            if (serialDriver != null) {
                SerialConnection connection = serialDriver.getSerialConnection();
                if (connection != null) serialPortName = connection.getPortName();

                serialDriverClass = serialDriver.getClass().getSimpleName();
                Config conf = serialDriver.getConfig();

                if (conf != null) {
                    batteryMin = String.valueOf(Double.parseDouble(conf.getOption(Config.BATTERY_MIN_VOLTAGE)) / 1000) + "V";
                    batteryMax = String.valueOf(Double.parseDouble(conf.getOption(Config.BATTERY_MAX_VOLTAGE)) / 1000) + "V";
                    serialListenerTimeout = String.valueOf(Double.parseDouble(conf.getOption(Config.SERIAL_LISTENER_TIMEOUT)) / 1000) + "s";
                    serialPortInternalTimeout = String.valueOf(Double.parseDouble(conf.getOption(Config.COMM_PORT_INTERNAL_TIMEOUT)) / 1000) + "s";
                    hardwareSensorRefreshPeriod = String.valueOf(Double.parseDouble(conf.getOption(Config.STATE_REFRESH_PERIOD)) / 1000) + "s";
                    serviceMaxQueueSize = conf.getOption(Config.SERVICE_MAX_QUEUE_SIZE);
                    servicePollWaitTimeout = String.valueOf(Double.parseDouble(conf.getOption(Config.SERVICE_POLL_WAIT_TIMEOUT)) / 1000) + "s";
                    serviceReconnectTimeout = String.valueOf(Double.parseDouble(conf.getOption(Config.SERVICE_RECONNECT_TIMEOUT)) / 1000) + "s";

                }

                alertInfoText = "Arduino2WD Robot successfully connected trough the " + serialPortName;
            } else {
                if (!alertDangerText.equals("")) alertDangerText = alertDangerText + "<br>";
                alertDangerText = alertDangerText + "SerialDriver was not initialized! <!--a href=\"/webrc/servlet?do=Init\">Initialize it!</a-->";
            }
            PriorityBlockingQueue<Command> commandQueue = SerialService.getInstance().getCommandQueue();
            String commandQueueSize = "n/a";
            if (commandQueue != null) commandQueueSize = String.valueOf(commandQueue.size());

            String cloudQueueActivity = (CloudQueue.isEnabled()) ? "Enabled" : "Disabled";

        %>
        <div class="col-sm-4">
            <div class="panel panel-primary">
                <div class="panel-heading">
                    <h3 class="panel-title">WebRC Info</h3>
                </div>
                <div class="panel-body">
                    SerialDriver class:<strong><%out.println(serialDriverClass);%></strong><br>
                    Serial port name:<strong><%out.println(serialPortName);%></strong><br>
                    Queue current size:<strong><%out.println(commandQueueSize);%></strong>
                    <hr>
                    Battery min Voltage:<strong><%out.println(batteryMin);%></strong><br>
                    Battery max Voltage:<strong><%out.println(batteryMax);%></strong><br>
                    Port listener timeout:<strong><%out.println(serialListenerTimeout);%></strong><br>
                    Port internal timeout:<strong><%out.println(serialPortInternalTimeout);%></strong><br>
                    Hardware sensor refresh period:<strong><%out.println(hardwareSensorRefreshPeriod);%></strong><br>
                    Max queue size:<strong><%out.println(serviceMaxQueueSize);%></strong><br>
                    Queue poll wait timeout:<strong><%out.println(servicePollWaitTimeout);%></strong><br>
                    Service reconnect period:<strong><%out.println(serviceReconnectTimeout);%></strong><br>
                    Cloud Queue monitoring:<strong><%out.println(cloudQueueActivity);%></strong>
                </div>
            </div>
        </div><!-- /.col-sm-4 -->
    </div>

    <%
        if (shellResult != null) {
            if (!alertInfoText.equals("")) alertInfoText = alertInfoText + "<br>";
            alertInfoText = alertInfoText + "<strong>Shell result for \"" + command + "\":</strong> " + shellResult;
        }
        if (!alertDangerText.equals("")) {
            String alert =
                    "<div class=\"alert alert-danger\">\n" +
                            "    " + alertDangerText +
                            "</div>";
            out.println(alert);
        }
        if (!alertInfoText.equals("")) {
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
<script src="https://getbootstrap.com/dist/js/bootstrap.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/holder/2.9.3/holder.min.js"></script>
</body>
</html>
