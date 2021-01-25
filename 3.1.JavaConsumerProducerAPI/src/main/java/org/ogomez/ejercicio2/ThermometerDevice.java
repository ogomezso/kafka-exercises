package org.ogomez.ejercicio2;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThermometerDevice {

  public static void main(String[] args) throws ExecutionException, InterruptedException {

    Logger logger = LoggerFactory.getLogger(ThermometerDevice.class.getName());

    final Random random = new Random();
    ThermometerProducer device1 = new ThermometerProducer();

    while (true) {

      TimeUnit.SECONDS.sleep(random.longs(0, 5).findFirst().orElse(0));
      String key = "device-" + String.valueOf(random.ints(0, 15).findFirst().orElse(0));
      Long value = random.longs(-12, 40).findFirst().orElse(0);

      ProducerRecord<String, Long> record = new ProducerRecord<>(KafkaConfig.INPUT_TOPIC, key,
          value);

      logger.info("record produced with  key: {} and value: {}",key,value);

      device1.produceRecord(record);
    }

  }

}
