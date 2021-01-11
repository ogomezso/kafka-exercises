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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.Duration.ofSeconds;
import static java.util.Collections.singleton;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

public class TxWordCount {

  private static final String CONSUMER_GROUP_ID = "my-group-id";
  private static final String OUTPUT_TOPIC = "output";
  private static final String INPUT_TOPIC = "input";
  private static final String BOOTSTRAP_SERVER = "127.0.0.1:9092, 127.0.0.1:9093, 127.0.0.1:9094";
  public static void main(String[] args) {

    KafkaConsumer<String, String> consumer = createKafkaConsumer();
    KafkaProducer<String, String> producer = createKafkaProducer();

    producer.initTransactions();

    try {

      while (true) {

        ConsumerRecords<String, String> records = consumer.poll(ofSeconds(60));

        Map<String, Integer> wordCountMap = records.records(new TopicPartition(INPUT_TOPIC, 0))
            .stream()
            .flatMap(record -> Stream.of(record.value().split(" ")))
            .map(word -> Tuple.of(word, 1))
            .collect(Collectors.toMap(Tuple::getKey, Tuple::getValue, Integer::sum));

        producer.beginTransaction();

        wordCountMap.forEach((key, value) -> producer.send(
            new ProducerRecord<>(OUTPUT_TOPIC, key, value.toString())));

        Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>();

        for (TopicPartition partition : records.partitions()) {
          List<ConsumerRecord<String, String>> partitionedRecords = records.records(partition);
          long offset = partitionedRecords.get(partitionedRecords.size() - 1).offset();

          offsetsToCommit.put(partition, new OffsetAndMetadata(offset + 1));
        }

        producer.sendOffsetsToTransaction(offsetsToCommit, CONSUMER_GROUP_ID);
        producer.commitTransaction();

      }

    } catch (KafkaException e) {

      producer.abortTransaction();

    }


  }

  private static KafkaConsumer<String, String> createKafkaConsumer() {
    Properties props = new Properties();
    props.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
    props.put(GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
    props.put(ENABLE_AUTO_COMMIT_CONFIG, "false");
    props.put(ISOLATION_LEVEL_CONFIG, "read_committed");
    props.put(KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
    props.put(VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
    consumer.subscribe(singleton(INPUT_TOPIC));
    return consumer;
  }

  private static KafkaProducer<String,String> createKafkaProducer() {

    Properties props = new Properties();
    props.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
    props.put(ENABLE_IDEMPOTENCE_CONFIG, "true");
    props.put(TRANSACTIONAL_ID_CONFIG, "prod-1");
    props.put(KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
    props.put(VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

    return new KafkaProducer<>(props);

  }
}
