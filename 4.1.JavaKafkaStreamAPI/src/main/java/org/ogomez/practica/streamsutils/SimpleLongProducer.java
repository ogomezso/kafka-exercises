package org.ogomez.practica.streamsutils;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleLongProducer {

  public static void main(String[] args) throws ExecutionException, InterruptedException {

    final Logger logger = LoggerFactory.getLogger(SimpleLongProducer.class);

    KafkaProducer<String, Long> producer = KafkaConfig.createKafkaLongProducer();

    String inputTopic = readFromConsole("Introduce el topic: ");
    System.out.println("Estas produciendo en el topic: " + inputTopic);

    while (true) {

      String key = readFromConsole("Introduce la key: ");

      Long value = Long.valueOf(readFromConsole("Introduce el mensaje: "));

      ProducerRecord<String, Long> record =
          new ProducerRecord<>(inputTopic, key, value);

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

  }

  private static String readFromConsole(String msg) {
    Scanner sc = new Scanner(System.in); //System.in is a standard input stream
    System.out.print(msg);
    return sc.nextLine();              //reads string
  }
}
