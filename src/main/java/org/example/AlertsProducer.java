package org.example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

class AlertsProducer {
    private static final Logger LOG = LogManager.getLogger(AlertsProducer.class);

    public void sendAlert(String message) {
        createProducer(message);
    }

    private void createProducer(String message) {
        final String TOPIC = "alerts";

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, "alert", message);
            producer.send(record);
            System.out.println("The message has been sent to a topic: " + record.topic());
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }

    }
}
