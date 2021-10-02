package org.ogomez.tx;

import java.util.Properties;
import java.util.stream.Stream;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;

import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.TRANSACTIONAL_ID_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

public class TxMessageProducer {

  //Mensaje que producimos en el topic de entrada
  private static final String DATA_MESSAGE_1 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum";

  //Mensaje 2
  private static final String DATA_MESSAGE_2 = "En un lugar de la Mancha, de cuyo nombre no quiero acordarme, no ha mucho tiempo que vivía un hidalgo de los de lanza en astillero, adarga antigua, rocín flaco y galgo corredor. Una olla de algo más vaca que carnero, salpicón las más noches, duelos y quebrantos los sábados, lantejas los viernes, algún palomino de añadidura los domingos, consumían las tres cuartas partes de su hacienda. El resto della concluían sayo de velarte, calzas de velludo para las fiestas, con sus pantuflos de lo mesmo, y los días de entresemana se honraba con su vellorí de lo más fino. Tenía en su casa una ama que pasaba de los cuarenta, y una sobrina que no llegaba a los veinte, y un mozo de campo y plaza, que así ensillaba el rocín como tomaba la podadera. Frisaba la edad de nuestro hidalgo con los cincuenta años; era de complexión recia, seco de carnes, enjuto de rostro, gran madrugador y amigo de la caza. Quieren decir que tenía el sobrenombre de Quijada, o Quesada, que en esto hay alguna diferencia en los autores que deste caso escriben; aunque, por conjeturas verosímiles, se deja entender que se llamaba Quejana. Pero esto importa poco a nuestro cuento; basta que en la narración dél no se salga un punto de la verdad. ";

  public static void main(String[] args) {

    KafkaProducer<String, String> producer = createKafkaProducer();

    producer.initTransactions();

    try{

      producer.beginTransaction();

      Stream.of(DATA_MESSAGE_1, DATA_MESSAGE_2).forEach(s -> producer.send(
          new ProducerRecord<>("wordcount-input-topic", null, s)));

      producer.commitTransaction();

    }catch (KafkaException e){

      producer.abortTransaction();

    }

  }

  private static KafkaProducer<String,String> createKafkaProducer() {

    //Creamos nuestra clase de propiedades para el productor.
    Properties props = new Properties();
    //Dirección de los Broker
    props.put(BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092, 127.0.0.1:9093, 127.0.0.1:9094");
    //Propiedad con la que indicamos que queremos recibir el ACK de todas las replicas antes de dar el ACK a cliente
    props.put(ACKS_CONFIG,"all");
    //Indicamos que queremos que cada mensaje sea idempotente, es decir el mismo evento (OJO EVENTO no CONTENIDO DEL MISMO)
    //se producirá una y solo una vez, si el mismo evento se produjera varias veces Kafka lo descartaria.
    props.put(ENABLE_IDEMPOTENCE_CONFIG, "true");
    //Id con el que marcaremos las transacciones de este productor
    props.put(TRANSACTIONAL_ID_CONFIG, "prod-0");
    //Clase con la que serializamos las keys
    props.put(KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
    //Clase cono la que serializamos los mensajes
    props.put(VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

    //devolvemos un KafkaProducer con las propiedades seteadas en la clase de configuración
    return new KafkaProducer<>(props);

  }
}
