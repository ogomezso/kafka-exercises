package org.ogomez.nontx;

import java.time.Duration;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleConsumer2 {

  public static void main(String[] args) {

    Logger logger = LoggerFactory.getLogger(SimpleConsumer2.class.getName());

    KafkaConsumer<String, String> consumer = CreateKafkaConfig.createKafkaConsumer();

    while (true) {
      ConsumerRecords<String, String> records =
          consumer.poll(Duration.ofMillis(100));

      for (ConsumerRecord<String, String> record : records) {
        logger.info("Key: " + record.key() + ", Value: " + record.value());
        logger.info("Partition: " + record.partition() + ", Offset:" + record.offset());
      }
    }

  }

}
