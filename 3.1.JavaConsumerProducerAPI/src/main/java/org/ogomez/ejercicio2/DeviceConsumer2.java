package org.ogomez.ejercicio2;

import java.time.Duration;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceConsumer2 {

  public static void main(String[] args) {

    Logger logger = LoggerFactory.getLogger(DeviceConsumer2.class.getName());

    KafkaConsumer<String, Long> consumer = KafkaConfig.createKafkaConsumer();

    while (true) {
      ConsumerRecords<String, Long> records =
          consumer.poll(Duration.ofMillis(100));

      for (ConsumerRecord<String, Long> record : records) {
        logger.info("Key: " + record.key() + ", Value: " + record.value());
        logger.info("Partition: " + record.partition() + ", Offset:" + record.offset());
      }
    }

  }

}
