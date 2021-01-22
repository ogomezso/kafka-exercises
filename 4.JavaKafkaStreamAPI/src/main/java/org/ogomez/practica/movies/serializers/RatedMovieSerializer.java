package org.ogomez.practica.movies.serializers;

import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;
import org.ogomez.practica.movies.model.RatedMovie;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RatedMovieSerializer implements Serializer<RatedMovie> {

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {

  }

  @Override
  public byte[] serialize(String s, RatedMovie pojo) {

    byte[] retVal = null;
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      retVal = objectMapper.writeValueAsString(pojo).getBytes();
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return retVal;
  }

  @Override
  public void close() {

  }
}
