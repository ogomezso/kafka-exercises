package org.ogomez.practica.streamsutils;

import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PojoSerializer<T> implements Serializer<T> {

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {

  }

  @Override
  public byte[] serialize(String s, T pojo) {

    byte[] retVal = null;
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      retVal = objectMapper.writeValueAsBytes(pojo);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return retVal;
  }

  @Override
  public void close() {

  }
}
