package org.ogomez.practica.movies.serializers;

import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;
import org.ogomez.practica.movies.model.Rating;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RatingSerializer implements Serializer<Rating> {

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {

  }

  @Override
  public byte[] serialize(String s, Rating pojo) {

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
