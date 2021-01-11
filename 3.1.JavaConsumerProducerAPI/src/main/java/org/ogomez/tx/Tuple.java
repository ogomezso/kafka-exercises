package org.ogomez.tx;

public class Tuple {

  private final String key;
  private final Integer value;

  private Tuple(String key, Integer value) {
    this.key = key;
    this.value = value;
  }

  public static Tuple of(String key, Integer value){
    return new Tuple(key,value);
  }

  public String getKey() {
    return key;
  }

  public Integer getValue() {
    return value;
  }

}
