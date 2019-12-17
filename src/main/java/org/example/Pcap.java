package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Pcap {
    private static final Logger LOG = LogManager.getLogger(Pcap.class);

    HashMap<Timestamp, Long> minMap = new HashMap();
    HashMap<Timestamp, Long> maxMap = new HashMap();

    public void packetCapture(String[] args) throws IOException, PcapNativeException, NotOpenException {
        final InetAddress ADDRESS;
        final String PORT;
        if (args.length > 0 && !args[0].equals("-")) {
            int indexColon = args[0].indexOf(":");
            ADDRESS = InetAddress.getByName(args[0].substring(indexColon - 1));
            PORT = "port " + args[0].substring(indexColon + 1);
        } else {
            ADDRESS = InetAddress.getByName("localhost");
            PORT = "";
        }

        final long[] totalLengthPackets = new long[1];

        PcapNetworkInterface nif = Pcaps.getDevByAddress(ADDRESS);

        int timeout = 10;
        int snapLen = 65536;
        PcapNetworkInterface.PromiscuousMode mode = PcapNetworkInterface.PromiscuousMode.PROMISCUOUS;
        PcapHandle handle = nif.openLive(snapLen, mode, timeout);

        long start = System.currentTimeMillis();
        long startForUpd = start;

        String filter = "tcp || udp" + PORT;
        handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);

        PacketListener listener = new PacketListener() {
            @Override
            public void gotPacket(Packet packet) {

                // System.out.println(handle.getTimestamp());
                // System.out.println(packet);
                IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
                long totalLength = ipV4Packet.getHeader().getTotalLength();
                totalLengthPackets[0] = (totalLengthPackets[0] + totalLength);

                long now = System.currentTimeMillis();
                long timeWork = now - start;
                if (timeWork >= TimeUnit.MINUTES.toMillis(5)) {
                    comparisonLimits(totalLengthPackets[0], handle.getTimestamp());
                    long start = System.currentTimeMillis();
                    totalLengthPackets[0] = 0;
                }
                long timeWorkForUpdate = now - startForUpd;
                if (timeWork >= TimeUnit.MINUTES.toMillis(20)) {
                    comparisonLimitsForUpd();
                    long startForUpdate = start;
                }
            }
        };

        try {
            handle.loop(-1, listener);
        } catch (InterruptedException e) {
            LOG.error("Exception: ", e);
        }

        handle.close();
    }

    private void comparisonLimits(long totalLengthPacket, Timestamp timestamp) {
        Limits limits = new Limits();
        long max = limits.getLimit("max");
        long min = limits.getLimit("min");

        if (totalLengthPacket > max) {
            maxMap.put(timestamp, totalLengthPacket);
            sendMessage("Max traffic limit exceeded");
        } else if (totalLengthPacket < min) {
            minMap.put(timestamp, totalLengthPacket);
            sendMessage("Min traffic limit exceeded");
        }
    }

    private void comparisonLimitsForUpd() {
        Limits limits = new Limits();

        if (!maxMap.isEmpty()) {
            List<Timestamp> keyList = new ArrayList(maxMap.keySet());
            Collections.sort(keyList);
            updLimits("max", maxMap.get(keyList.get(0)), keyList.get(0));
            maxMap.clear();
        }
        if (!minMap.isEmpty()) {
            List<Timestamp> keyList = new ArrayList(minMap.keySet());
            Collections.sort(keyList);
            updLimits("min", minMap.get(keyList.get(0)), keyList.get(0));
            minMap.clear();
        }

    }

    private void sendMessage(String message) {
        AlertsProducer alert = new AlertsProducer();
        alert.sendAlert(message);
    }

    private void updLimits(String nameLimit, long newLimit, Timestamp newDate) {
        Limits limits = new Limits();
        limits.updateLimit(nameLimit, newLimit, newDate);
    }

}


