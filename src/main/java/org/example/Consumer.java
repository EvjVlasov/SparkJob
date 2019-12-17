package org.example;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka010.*;
import scala.Tuple2;

import java.util.*;

public class Consumer {
    private static final Logger LOG = LogManager.getLogger(Consumer.class);

    public void creatorConsumer(String[] args) {
        String host = "localhost:9092";
        if (args.length > 0 && !args[0].equals("-")) {
            host = args[0];
        }

        SparkConf sparkConf = new SparkConf().setAppName("My app").setMaster("local[*]");

        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, Durations.seconds(20));

        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, host);
        kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, "app");
        kafkaParams.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        kafkaParams.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        Collection<String> topics = Arrays.asList("mytopic");

        JavaInputDStream<ConsumerRecord<String, String>> stream =
                KafkaUtils.createDirectStream(
                        jssc,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.Subscribe(topics, kafkaParams));

        JavaPairDStream<String, String> pairRDD = stream.mapToPair(record -> new Tuple2<>(record.key(), record.value()));

        pairRDD.foreachRDD(pRDD -> {
            pRDD.foreach(tuple -> System.out.println("Kafka msg key ::" + tuple._1() + " the value is ::" + tuple._2()));
        });

        jssc.start();
        try {
            jssc.awaitTermination();
        } catch (InterruptedException e) {
            LOG.error("Exception: ", e);
        }

    }
}

