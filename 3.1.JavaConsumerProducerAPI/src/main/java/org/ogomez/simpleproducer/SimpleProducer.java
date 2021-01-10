package org.ogomez.simpleproducer;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleProducer {

  public static void main(String[] args) throws ExecutionException, InterruptedException {

    final Logger logger = LoggerFactory.getLogger(SimpleProducer.class);

    String bootstrapServers = "127.0.0.1:9092, 127.0.0.1:9093, 127.0.0.1:9094";

    // create Producer properties
    Properties properties = new Properties();
    properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    // create the producer
    KafkaProducer<String, String> producer = new KafkaProducer<>(properties);


    for (int i=0; i<1000; i++ ) {
      // create a producer record

      String topic = "simple-producer-topic";
      String value = "{\"msg\":\"" + i +"\"}";
      Random random = new Random();
      String key = String.valueOf(random.ints(0,9).findFirst().orElse(0));

      ProducerRecord<String, String> record =
          new ProducerRecord<>(topic, key, value);

      logger.info("Key: " + key);
      logger.info("value:" + value);

      // send data - asynchronous
      producer.send(record, new Callback() {
        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
          // executes every time a record is successfully sent or an exception is thrown
          if (e == null) {
            // the record was successfully sent
            logger.info("Received new metadata. \n" +
                "Topic:" + recordMetadata.topic() + "\n" +
                "Partition: " + recordMetadata.partition() + "\n" +
                "Offset: " + recordMetadata.offset() + "\n" +
                "Timestamp: " + recordMetadata.timestamp());
          } else {
            logger.error("Error while producing", e);
          }
        }
      }).get(); // block the .send() to make it synchronous - don't do this in production!
    }

    // flush data
    producer.flush();
    // flush and close producer
    producer.close();

  }
}
