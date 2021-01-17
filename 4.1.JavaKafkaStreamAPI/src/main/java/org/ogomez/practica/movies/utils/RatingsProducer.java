package org.ogomez.practica.movies.utils;

import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.ogomez.practica.movies.MovieTopology;
import org.ogomez.practica.movies.model.Movie;
import org.ogomez.practica.movies.model.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RatingsProducer {


  private final ObjectMapper mapper = new ObjectMapper();

  private static KafkaProducer<String, Rating> createKafkaProducer() {

    Properties props = new Properties();
    props.put(BOOTSTRAP_SERVERS_CONFIG, MovieTopology.BOOTSTRAP_SERVERS);
    props.put(KEY_SERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringSerializer");
    props.put(VALUE_SERIALIZER_CLASS_CONFIG,
        "org.ogomez.practica.movies.serializers.RatingSerializer");

    return new KafkaProducer<>(props);

  }

  public static void main(String[] args)
      throws ExecutionException, InterruptedException, IOException, URISyntaxException {

    RatingsProducer app = new RatingsProducer();

    MovieTopology.createTopics();

    final Logger logger = LoggerFactory.getLogger(RatingsProducer.class);

    KafkaProducer<String, Rating> producer = createKafkaProducer();

    List<Rating> ratings = app.getMoviesFromFile();

    for (Rating rating : ratings) {
      ProducerRecord<String, Rating> record = new ProducerRecord<>(MovieTopology.RATINGS_TOPIC,
          rating.getId(), rating);
      producer.send(record, (recordMetadata, e) -> {
        if (e == null) {
          logger.info("Received new metadata. \n" +
              "Topic:" + recordMetadata.topic() + "\n" +
              "msgSize:" + recordMetadata.serializedValueSize() + "\n" +
              "Partition: " + recordMetadata.partition() + "\n" +
              "Offset: " + recordMetadata.offset() + "\n" +
              "Timestamp: " + recordMetadata.timestamp());
        } else {
          logger.error("Error while producing", e);
        }
      }).get();
    }
  }

  private List<Rating> getMoviesFromFile() throws URISyntaxException, IOException {

    return mapper.readValue(getFileFromResource(), new TypeReference<>() {
    });
  }

  private File getFileFromResource() throws URISyntaxException {

    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource("ratings.json");

    if (resource == null) {
      throw new IllegalArgumentException("file not found! " + "ratings.json");
    } else {
      return new File(resource.toURI());
    }
  }
}
