package org.ogomez.tx;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.time.Duration.ofSeconds;
import static java.util.Collections.singleton;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

public class TxWordCount {

  private static final String CONSUMER_GROUP_ID = "wordcount-group-id";
  private static final String OUTPUT_TOPIC = "tx-wordcount-output-topic";
  private static final String INPUT_TOPIC = "tx-wordcount-input-topic";
  private static final String BOOTSTRAP_SERVER = "127.0.0.1:9092, 127.0.0.1:9093, 127.0.0.1:9094";
  public static void main(String[] args) {

    KafkaConsumer<String, String> consumer = createKafkaConsumer();
    KafkaProducer<String, String> producer = createKafkaProducer();

    //Iniciamos las transacciones en nuestro productor
    producer.initTransactions();

    try {

      while (true) {

        //Consumimos mensajes cada 60 segundos
        ConsumerRecords<String, String> records = consumer.poll(ofSeconds(60));

        //Lógica de consumo
        //Recorremos los ConsumerRecord mediante un Stream, y partimos cada mensaje por palabra
        //usando para ello el espacio como separador, para luego guardarlo en una tupla de palabra y un 1
        //de modo que luego los volveremos a juntar en un Map con key cada palabra y value la suma de todas
        //las veces que apareció la palabra.
        Spliterator<ConsumerRecord<String,String>> recordIt = records.records(INPUT_TOPIC).spliterator();
        Map<String, Integer> wordCountMap = StreamSupport
            .stream(recordIt,false)
            .flatMap(record -> Stream.of(record.value().split(" ")))
            .map(word -> Tuple.of(word, 1))
            .collect(Collectors.toMap(Tuple::getKey, Tuple::getValue, Integer::sum));

        //Una vez obtenida la lista de palabras y sus ocurrencias en el texto empezamos la transaccion de nuestro productor
        producer.beginTransaction();

        //vamos recorriendo cada consumer record, produciendo un mensaje con cada uno de ellos
        //en este caso pasamos como key la palabra y como value el numero de veces que aparecio en el mensaje
        wordCountMap.forEach((key, value) -> producer.send(
            new ProducerRecord<>(OUTPUT_TOPIC, key, value.toString())));

        //Preparamos nuestro map de offset por particion
        Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>();

        //rellenamos los offset que hemos consumidos correctamente y vamos rellenando la particion y el offset que hemos
        //consumido
        for (TopicPartition partition : records.partitions()) {
          List<ConsumerRecord<String, String>> partitionedRecords = records.records(partition);
          long offset = partitionedRecords.get(partitionedRecords.size() - 1).offset();

          offsetsToCommit.put(partition, new OffsetAndMetadata(offset + 1));
        }

        //mandamos al productor todos los offset que queremos comitear marcados por el consumer group que los consumio
        producer.sendOffsetsToTransaction(offsetsToCommit, CONSUMER_GROUP_ID);
        //hacemos commit de nuestra transaccion
        producer.commitTransaction();
        //en este momento podemos estar 100% seguros de que nuestros mensajes han sido recibidos y persistidos en el topic de salida de Kafka
        //asegurando de este modo que se habra producido y consumido 1 y solo 1 vez
      }

    } catch (KafkaException e) {

      //en caso de error la transaccion sera abortada y el mensaje podra volver a ser producido/consumido dependendiendo de nuestra política de
      // reintentos.
      producer.abortTransaction();

    }


  }

  private static KafkaConsumer<String, String> createKafkaConsumer() {

    //Creamos nuestra clase de propiedades
    Properties props = new Properties();

    //Direccion de los Broker
    props.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);

    //Id de grupo de consumo
    props.put(GROUP_ID_CONFIG, CONSUMER_GROUP_ID);

    //deshabilitamos el auto commit, queremos controlar el momennto en el que se da por buena la TX
    props.put(ENABLE_AUTO_COMMIT_CONFIG, "false");

    //el nivel de aislamiento de la tx de consumo, con esta propiedad nos aseguramos que no consumiremos
    //ningún mensaje que no haya sido commiteado por el productor (por ej. un productor que se cae antes de recibir los ACK)
    //asi no s aseguramos la semantica Exactly Once
    props.put(ISOLATION_LEVEL_CONFIG, "read_committed");

    props.put(KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
    props.put(VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
    consumer.subscribe(singleton(INPUT_TOPIC));
    return consumer;
  }

  private static KafkaProducer<String,String> createKafkaProducer() {

    //Creamos nuestra clase de propiedades
    Properties props = new Properties();

    //Direccion de los broker
    props.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);

    //Propiedad con la que indicamos que queremos recibir el ACK de todas las replicas antes de dar el ACK a cliente
    props.put(ACKS_CONFIG,"all");

    //Idempotencia del productor habilitada
    props.put(ENABLE_IDEMPOTENCE_CONFIG, "true");

    //Id con el que marcaremos las transacciones de este productor (debe ser único)
    props.put(TRANSACTIONAL_ID_CONFIG, "prod-1");

    //clase con la que serializamos las keys
    props.put(KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

    //clase con la que serializamos los mensajes.
    props.put(VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

    //devolvemos una instancia del kafka producer con las propiedades creadas
    return new KafkaProducer<>(props);

  }
}
