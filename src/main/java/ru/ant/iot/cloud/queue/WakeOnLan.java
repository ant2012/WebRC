package ru.ant.iot.cloud.queue;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WakeOnLan {
    Logger log = Logger.getLogger(getClass());

    private static final int PORT = 9;
    private List<InetAddress> broadcastIpList;

    public WakeOnLan(){
        broadcastIpList = refreshNetworks();
    }

    public WakeOnLan(String mac) {
        this();
        send(mac);
    }

    public WakeOnLan(byte[] mac){
        this();
        send(mac);
    }

    public List<InetAddress> refreshNetworks() {
        try {
            Enumeration<NetworkInterface> ethList = NetworkInterface.getNetworkInterfaces();
            Stream<InetAddress> resultStream = null;
            while(ethList.hasMoreElements()){
                NetworkInterface eth = ethList.nextElement();
                if(eth.isLoopback() || !eth.isUp()) continue;
                Stream<InetAddress> partStream = eth.getInterfaceAddresses().stream().filter(f -> f.getBroadcast() != null).map(f -> f.getBroadcast());
                resultStream = (resultStream == null) ? partStream : Stream.concat(resultStream, partStream);
            }
            return resultStream.collect(Collectors.toList());
        } catch (SocketException e) {
            log.error("Network error", e);
        }
        return new ArrayList<>();
    }

    public void send(String mac){
        byte[] bytes = getMagicPacket(mac);
        send(bytes);
    }

    public void send(byte[] bytes) {
        for (InetAddress ip : broadcastIpList) {
            send(ip, bytes);
        }
    }

    private void send(InetAddress br, byte[] mac) {

        try {
            DatagramPacket packet = new DatagramPacket(mac, mac.length, br, PORT);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();

            log.info("Wake-on-LAN packet sent to " + br.getHostAddress());

        } catch (Exception e) {
            log.error("Failed to send Wake-on-LAN packet", e);
        }

    }

    public static byte[] getMagicPacket(String macString) {
        String clearMac = macString.replaceAll("(?i)[^0-9A-F]", "");

        if (clearMac.length() != 12)
            throw new IllegalArgumentException("Invalid MAC address.");

        String magicString = "ffffffffffff";
        for (int i = 0; i < 16; i++) {
            magicString += clearMac;
        }
        try {
            return Hex.decodeHex(magicString.toCharArray());
        } catch (DecoderException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }

    }
}