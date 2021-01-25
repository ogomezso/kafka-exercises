package org.ogomez.nontx;

import java.time.Duration;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleConsumer {

  //Metodo principal de nuestro proceso que nos permite correr nuestra aplicacion, crearemos varias
  //clases como esta para simular varios procesos consumiendo a la vez.
  public static void main(String[] args) {

    //Creamos una instancia de Logger solo para pintar informacion de nuestro proceso
    Logger logger = LoggerFactory.getLogger(SimpleConsumer.class.getName());

    //Creamos una instancia de Kafka Consumer usando el metodo que creamos en nuestrra clase de configuracion
    KafkaConsumer<String, String> consumer = KafkaConfig.createKafkaConsumer();

    while (true) {
      //mantenemos un bucle infinito escuchando (poll) el topic al que nos suscribimos al crear el
      //consumer en nuestra clase de configuración, en este caso tratamos de traernos mensajes cada
      // 100 millisegundos
      ConsumerRecords<String, String> records =
          consumer.poll(Duration.ofMillis(100));
      //Como podemos imaginar en 100 millisegundos es posible que entre mas  de un mensaje por tanto
      //el metodo poll nos devuelve una lista  de registros que podemos iterar como se ve en el bucle.
      for (ConsumerRecord<String, String> record : records) {
        logger.info("Key: " + record.key() + ", Value: " + record.value());
        logger.info("Partition: " + record.partition() + ", Offset:" + record.offset());
      }
    }

  }

}
