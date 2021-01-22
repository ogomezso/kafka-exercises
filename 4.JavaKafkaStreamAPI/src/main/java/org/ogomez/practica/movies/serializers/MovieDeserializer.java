package org.ogomez.practica.movies.serializers;

import java.io.IOException;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.ogomez.practica.movies.model.Movie;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MovieDeserializer implements Deserializer<Movie> {

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {

  }

  @Override
  public Movie deserialize(String value, byte[] bytes) {

    ObjectMapper objectMapper = new ObjectMapper();
    Movie pojo = null;

    try {
      pojo = objectMapper.readValue(bytes, Movie.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return pojo;
  }

  @Override
  public void close() {

  }
}
