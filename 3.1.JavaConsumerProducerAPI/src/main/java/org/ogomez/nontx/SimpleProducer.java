package org.ogomez.nontx;

import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleProducer {

  public static void main(String[] args) throws ExecutionException, InterruptedException {

    final Logger logger = LoggerFactory.getLogger(SimpleProducer.class);

    KafkaProducer<String, String> producer = CreateKafkaConfig.createKafkaProducer();

    Random random = new Random();

    for (int i = 0; i < 1000; i++) {

      String key = String.valueOf(random.ints(0, 9).findFirst().orElse(0));
      String value = "{\"msg\":\"" + i + "\"}";

      ProducerRecord<String, String> record =
          new ProducerRecord<>(CreateKafkaConfig.INPUT_TOPIC, key, value);

      logger.info("Key: " + key);
      logger.info("value:" + value);

      producer.send(record, (recordMetadata, e) -> {
        if (e == null) {
          logger.info("Received new metadata. \n" +
              "Topic:" + recordMetadata.topic() + "\n" +
              "Partition: " + recordMetadata.partition() + "\n" +
              "Offset: " + recordMetadata.offset() + "\n" +
              "Timestamp: " + recordMetadata.timestamp());
        } else {
          logger.error("Error while producing", e);
        }
      }).get();
    }

    producer.flush();
    producer.close();

  }
}
