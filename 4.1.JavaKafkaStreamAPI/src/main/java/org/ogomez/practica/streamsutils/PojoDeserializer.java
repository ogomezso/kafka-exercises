package org.ogomez.practica.streamsutils;

import java.io.IOException;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PojoDeserializer<T> implements Deserializer<T> {

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {

  }

  @Override
  public T deserialize(String value, byte[] bytes) {

    ObjectMapper objectMapper = new ObjectMapper();
    T pojo = null;

    try {
      pojo = objectMapper.readValue(bytes, new TypeReference<T>(){});
    } catch (IOException e) {
      e.printStackTrace();
    }
    return pojo;
  }

  @Override
  public void close() {

  }
}
