package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;

import java.io.IOException;

public class Main {
    private static final Logger LOG = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length > 0) {
            Limits.DB_URL = "jdbc:postgresql://localhost:5432/" + args[1];
            Limits.USER = args[2];
            Limits.PASS = args[3];
        } else {
            System.out.println("Enter data for postgresql");
            /*
                Limits.DB_URL = "jdbc:postgresql://localhost:5432/test_db";
                Limits.USER =  "postgres";
                Limits.PASS  = "2017";
            */
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Pcap pcap = new Pcap();
                try {
                    pcap.packetCapture(args);
                } catch (IOException | PcapNativeException | NotOpenException e) {
                    LOG.error("Exception: ", e);
                }
            }
        });

        thread.start();
        Consumer consumer = new Consumer();
        consumer.creatorConsumer(args);
    }
}
