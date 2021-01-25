package org.ogomez.ejercicio2;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThermometerProducer {


  final Logger logger = LoggerFactory.getLogger(ThermometerProducer.class);
  private final KafkaProducer<String, Long> producer = KafkaConfig.createKafkaProducer();

  public void produceRecord(ProducerRecord<String, Long> record) {

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
    });
  }
}
