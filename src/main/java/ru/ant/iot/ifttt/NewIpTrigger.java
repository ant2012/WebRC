package ru.ant.iot.ifttt;

import org.afraid.freedns.DdnsUpdater;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ant on 15.05.2016.
 */
public class NewIpTrigger extends BaseIftttTrigger {

    private final String ADDRESS_TEMPLATE = "172.20";

    @Override
    public String getIftttEventName() {
        return "newip";
    }

    @Override
    protected IftttMessage initMessage() {
        return new IftttMessage(getVpnIp());
    }

    private String getVpnIp() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface i : interfaces) {
                if(!i.isUp() || i.isLoopback()) continue;
                List<InetAddress> addresses = Collections.list(i.getInetAddresses());
                for (InetAddress inetAddress : addresses) {
                    if(!(inetAddress instanceof Inet4Address)) continue;
                    String addr = inetAddress.getHostAddress();
                    if(!addr.startsWith(ADDRESS_TEMPLATE)) continue;
                    return addr;
                }
            }
        } catch (SocketException e) {
            log.error(e);
        }
        return "";
    }

    @Override
    protected void sendMessageToIfttt() {
        super.sendMessageToIfttt();
        String ddnsResponse = DdnsUpdater.update(preveousMsg.getValue1());
        if(ddnsResponse!=null)
            log.info("DDNS Response: " + ddnsResponse);
    }
}
