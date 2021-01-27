package org.ogomez.practica.streambasics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;

public class KafkaStreamGroupedKey {

  private static final String BOOTSTRAP_SERVERS = "127.0.0.1:9092, 127.0.0.1:9093, 127.0.0.1:9094";
  private static final String INPUT_TOPIC = "grouped-input-topic";
  private static final String OUTPUT_TOPIC = "grouped-output-topic";
  private static final String TEMP_STATE_DIR = "./temp";
  private static final String PROCESSING_GUARANTEE_CONFIG = "exactly_once";
  private static final int NUM_PARTITIONS = 3;
  private static final short REPLICATION_FACTOR = 3;

  public static Properties createStreamsConfigProperties(String applicationId) {

    Properties streamsConfiguration = new Properties();
    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
    streamsConfiguration.put(
        StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
        Serdes.String().getClass().getName());
    streamsConfiguration.put(
        StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
        Serdes.Long().getClass().getName());
    streamsConfiguration.put(
        StreamsConfig.STATE_DIR_CONFIG, TEMP_STATE_DIR);
    streamsConfiguration.put(
        StreamsConfig.PROCESSING_GUARANTEE_CONFIG, PROCESSING_GUARANTEE_CONFIG);

    return streamsConfiguration;
  }

  private static void createTopics() {
    Map<String, Object> config = new HashMap<>();

    config.put("bootstrap.servers", BOOTSTRAP_SERVERS);
    AdminClient client = AdminClient.create(config);

    List<NewTopic> topics = new ArrayList<>();

    topics.add(new NewTopic(
        INPUT_TOPIC,
        NUM_PARTITIONS,
        REPLICATION_FACTOR));

    topics.add(new NewTopic(
        OUTPUT_TOPIC,
        NUM_PARTITIONS,
        REPLICATION_FACTOR));

    client.createTopics(topics);
    client.close();

  }

  public static void main(String[] args) {

    createTopics();

    final StreamsBuilder builder = new StreamsBuilder();
    KStream<String, Long> outputStream = builder
        .stream(INPUT_TOPIC, Consumed.with(Serdes.String(), Serdes.Long()))
        .groupByKey()
        .reduce(Long::sum)
        .toStream();


//    outputStream.print(Printed.toSysOut());
    outputStream.to(OUTPUT_TOPIC,
        Produced.with(Serdes.String(),Serdes.Long()));

    Topology topology = builder.build();
    final KafkaStreams streams = new KafkaStreams(topology,
        createStreamsConfigProperties("groupedKey"));
    streams.cleanUp();

    streams.start();

    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }

}
