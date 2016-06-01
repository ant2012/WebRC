package ru.ant.iot.utils;

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
    private static Logger log = Logger.getLogger(WakeOnLan.class);
    private static final int PORT = 9;

    private static List<InetAddress> getAllBroadcasts() {
        try {
            Enumeration<NetworkInterface> ethList = NetworkInterface.getNetworkInterfaces();
            Stream<InetAddress> resultStream = null;
            while(ethList.hasMoreElements()){
                NetworkInterface eth = ethList.nextElement();
                if(eth.isLoopback() || !eth.isUp()) continue;
                Stream<InetAddress> partStream = eth.getInterfaceAddresses().stream().filter(f -> f.getBroadcast() != null).map(f -> f.getBroadcast());
                resultStream = (resultStream == null) ? partStream : Stream.concat(resultStream, partStream);
            }
            return addExtraBroadcasts(resultStream.collect(Collectors.toList()));
        } catch (SocketException e) {
            log.error("Network error", e);
        }
        return new ArrayList<>();
    }

    private static List<InetAddress> addExtraBroadcasts(List<InetAddress> list) {
        ArrayList<InetAddress> result = new ArrayList<>();
        for (InetAddress src : list) {
            result.add(src);
            InetAddress broadcastIp = getBroadcastForIp(src);
            if(broadcastIp!=null) result.add(broadcastIp);
        }
        return result;
    }

    private static InetAddress getBroadcastForIp(InetAddress ip) {
        InetAddress result = null;
        byte[] ipBytes = ip.getAddress();
        if (ipBytes[3]!=(byte)255){
            ipBytes[3] = (byte) 255;
            try {
                result =  InetAddress.getByAddress(ipBytes);
            } catch (UnknownHostException e) {
                log.error("Error constructing broadcast for " + ip.getHostAddress(), e);
            }
        }
        return result;
    }

    public static void send(String mac, String ip){
        byte[] macBytes = getMagicPacket(mac);
        send(macBytes);
        if(ip==null) return;
        InetAddress addr = parseIp(ip);
        send(macBytes, addr);
        send(macBytes, getBroadcastForIp(addr));
    }

    private static InetAddress parseIp(String ip) {
        String[] ipParts = ip.split("\\.");
        if(ipParts.length != 4)
            throw new IllegalArgumentException("Wrong IP");
        byte[] result = new byte[4];
        try{
            for (int i = 0; i < 4; i++) {
                result[i] = Integer.valueOf(ipParts[i]).byteValue();
            }
            return InetAddress.getByAddress(result);
        }catch(NumberFormatException | UnknownHostException e){
            throw new IllegalArgumentException("Wrong IP", e);
        }
    }

    private static void send(byte[] mac) {
        for (InetAddress broadcastIp : getAllBroadcasts()) {
            send(mac, broadcastIp);
        }
    }

    private static void send(byte[] mac, InetAddress ip) {
        if(ip==null) return;
        try {
            DatagramPacket packet = new DatagramPacket(mac, mac.length, ip, PORT);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();

            log.info("Wake-on-LAN packet sent to " + ip.getHostAddress());

        } catch (Exception e) {
            log.error("Failed to send Wake-on-LAN packet", e);
        }

    }

    private static byte[] getMagicPacket(String macString) {
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

    public static void send(String mac) {
        send(mac, null);
    }
}