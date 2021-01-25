package org.ogomez.nontx;

import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleProducer {

  public static void main(String[] args) throws ExecutionException, InterruptedException {

    //instancia de Logger solo para poder pintar y facilitar el seguimiento de nuestro proceso
    final Logger logger = LoggerFactory.getLogger(SimpleProducer.class);

    //creamos una instnacia de nuestro producer usando la clase de configuración que hemos
    //creado para esto.
    KafkaProducer<String, String> producer = KafkaConfig.createKafkaProducer();

    //Clase de utilidad java para generar aleatorios
    Random random = new Random();

    //bucle de mil iteraciones que usaremos para generar mensajes aleatorios
    for (int i = 0; i < 1000; i++) {

      //Generamos una key aleatorio usando Random, para poder simular el balanceo por una partition key
      String key = String.valueOf(random.ints(0, 9).findFirst().orElse(0));
      //mensaje en un string en formato json (en realidad valdria cualquier string ya que no estamos serializando con ningun esquema)
      //con el numero de iteracion que genero el mensaje (atentos a la asincronia al producir)
      String value = "{\"msg\":\"" + i + "\"}";

      //Producer record, parametrizado con los mismos tipos que nuestro KafkaProducer. Le pasamos
      // 3 valores: Topic a producir, key por la que particionar (recordemos en caso de no pasarla
      //particionaria por round robin), y el value o mensaje a producir
      ProducerRecord<String, String> record =
          new ProducerRecord<>(KafkaConfig.INPUT_TOPIC, key, value);

      logger.info("Key: " + key);
      logger.info("value:" + value);

      //Enviamos el mensaje al broker:
      //    - Notar que el metodo devuelve los metadatos del record producido, no el metodo en si
      //    - Este metodo devuelve un Futuro, esto quiere decir que es un proceso asincrono, send
      //      enviara el mensaje al broker y quedara esperando la respuesta de este sin bloquear el
      //      hilo de ejecución.
      //    - Por esta naturaleza de "Futuro" la funcion send recibe como parametro una  funcion
      //      de callback es decir le decimos como tratar la respuesta, tratando un RecordMetadata
      //      en caso de ir bien, y una excepcion si falla.
      producer.send(record, (recordMetadata, e) -> {
        if (e == null) {
          logger.info("Received new metadata. \n" +
              "Topic:" + recordMetadata.topic() + "\n" +
              "Partition: " + recordMetadata.partition() + "\n" +
              "Offset: " + recordMetadata.offset() + "\n" +
              "Timestamp: " + recordMetadata.timestamp());
        } else {
          logger.error("Error while producing", e);
        }
      }).get();
    }

    //limpiamos la cola del productor (forzamos a que los envie a Kafka, resolviendo los futuros)
    // antes de cerrar la conexion
    producer.flush();
    //cerramos la conexion con el broker.
    producer.close();

  }
}
