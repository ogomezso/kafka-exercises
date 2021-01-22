package org.ogomez.practica.movies.serializers;

import java.io.IOException;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.ogomez.practica.movies.model.RatedMovie;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RatedMovieDeserializer implements Deserializer<RatedMovie> {

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {

  }

  @Override
  public RatedMovie deserialize(String value, byte[] bytes) {

    ObjectMapper objectMapper = new ObjectMapper();
    RatedMovie pojo = null;

    try {
      pojo = objectMapper.readValue(bytes, RatedMovie.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return pojo;
  }

  @Override
  public void close() {

  }
}
