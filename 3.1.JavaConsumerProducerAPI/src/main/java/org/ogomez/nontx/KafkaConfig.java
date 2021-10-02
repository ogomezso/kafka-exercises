package org.ogomez.nontx;

import static java.util.Collections.singleton;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

public class KafkaConfig {

  //Clase de la libreria Java para la serializacion de Strings
  public static final String SERIALIZATION_STRING_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
  //Clase de la libreria Java  para la Deserializacion de Strings
  public static final String SERIALIZATION_STRING_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
  // Valor de la propiedad donde pasaremos las direcciones de TODOS nuestros Broker
  // Destacar que al llamarlos desde fuera docker utilizaremos localhost como direccion y el puerto por el que docker expone hacia fuera
  static final String BOOTSTRAP_SERVERS_MULTI_BROKER = "127.0.0.1:9092, 127.0.0.1:9093, 127.0.0.1:9094";
  static final String BOOTSTRAP_SERVER_SINGLE_BORKER  = "localhost:9092";
  // ID que usaremos para identitificar nuestro grupo de consumo
  static final String CONSUMER_GROUP_ID = "simple-group";
  // Nombre del topic
  static final String INPUT_TOPIC = "simple-topic";

  //Usaremos este metodo para crear nuestro cliente consumidor KafkaConsumer, parametrizado (<>) por con el tipo de la key y el value
  static KafkaConsumer<String, String> createKafkaConsumer() {

    //Utilizaremos la clase Properties de java para modelar nuestro map de propiedades para el consumidor
    Properties props = new Properties();
    //Direccion de nuestros broker
    props.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS_MULTI_BROKER);
    //Id del consumidor
    props.put(GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
    //Desde donde empezara a leer nuestro consumidor, en este caso desde el primer offset, equivalente al from-beginning del console-consumer
    props.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
    //Tipo por el que deserializaremos el valor de nuestra key
    props.put(KEY_DESERIALIZER_CLASS_CONFIG,
        SERIALIZATION_STRING_DESERIALIZER);
    //Tipo por el que deserializaremos el valor de nuestro value(mensaje)
    props.put(VALUE_DESERIALIZER_CLASS_CONFIG,
        SERIALIZATION_STRING_DESERIALIZER);

    //creamos una nueva instancia de Kafka Consumer pasandole las propiedades que hemos seteado
    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
    //Le indicamos de que topic va a escuchar mensajes (suscribe)
    consumer.subscribe(singleton(INPUT_TOPIC));
    return consumer;
  }

  //Metodo analogo a Create Consumer por el que crearemos una instancia de un productor de kafka parametrizandolo de igual manera con los tipos de nuestra key, value
  static KafkaProducer<String, String> createKafkaProducer() {

    //Creamos nuestro fichero de propiedades para el producer
    Properties props = new Properties();
    //Direccion de TODOS los broker en los que producira (el solo sera capaz de encontrar el coordinador)
    props.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS_MULTI_BROKER);
    //Tipo por el que serializamos nuestra key
    props
        .put(KEY_SERIALIZER_CLASS_CONFIG, SERIALIZATION_STRING_SERIALIZER);
    //tipo por el que serializamos nuestro mensaje (value)
    props.put(VALUE_SERIALIZER_CLASS_CONFIG,
        SERIALIZATION_STRING_SERIALIZER);

    return new KafkaProducer<>(props);

  }

}
