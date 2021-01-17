package org.ogomez.practica.movies.utils;

import static java.util.Collections.singleton;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import java.time.Duration;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.ogomez.practica.movies.MovieTopology;
import org.ogomez.practica.movies.model.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoviesConsumer {

  static final String BOOTSTRAP_SERVERS = "127.0.0.1:9092, 127.0.0.1:9093, 127.0.0.1:9094";
  static final String  CONSUMER_GROUP_ID = "rated-movies-group";

  static KafkaConsumer<String, Movie> createKafkaConsumer() {
    Properties props = new Properties();
    props.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    props.put(GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
    props.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(KEY_DESERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringDeserializer");
    props.put(VALUE_DESERIALIZER_CLASS_CONFIG,
        "org.ogomez.practica.movies.serializers.MovieDeserializer");

    KafkaConsumer<String, Movie> consumer = new KafkaConsumer<>(props);
    consumer.subscribe(singleton(MovieTopology.MOVIES_TOPIC
    ));
    return consumer;
  }

  public static void main(String[] args) {

    Logger logger = LoggerFactory.getLogger(MoviesConsumer.class.getName());

    KafkaConsumer<String, Movie> consumer = createKafkaConsumer();

    while (true) {
      ConsumerRecords<String, Movie> records =
          consumer.poll(Duration.ofMillis(100));

      for (ConsumerRecord<String, Movie> record : records) {
        logger.info("Key: " + record.key() + ", Value: " + record.value());
        logger.info("Partition: " + record.partition() + ", Offset:" + record.offset());
      }
    }

  }

}
