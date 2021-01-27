package org.ogomez.practica.streambasics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

public class KafkaStreamsWordCount {

  private static final String BOOTSTRAP_SERVERS = "127.0.0.1:9092, 127.0.0.1:9093, 127.0.0.1:9094";
  private static final String INPUT_TOPIC = "wordcount-input-topic";
  private static final String OUTPUT_TOPIC = "wordcount-output-topic";
  private static final String PROCESSING_GUARANTEE_CONFIG= "exactly_once";
  private static final String TEMP_STATE_DIR = "./temp";
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
        Serdes.String().getClass().getName());
    streamsConfiguration.put(
        StreamsConfig.STATE_DIR_CONFIG, TEMP_STATE_DIR);
    streamsConfiguration.put(
        StreamsConfig.PROCESSING_GUARANTEE_CONFIG,PROCESSING_GUARANTEE_CONFIG);

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

  private static void createWordCountStream(final StreamsBuilder builder) {

    createTopics();

    final KStream<String, String> textLines = builder.stream(INPUT_TOPIC);

    final Pattern pattern = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);

    final KTable<String, Long> wordCounts = textLines
        .flatMapValues(value -> Arrays.asList(pattern.split(value.toLowerCase())))
        .groupBy((keyIgnored, word) -> word)
        .count();

    wordCounts.toStream().to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.Long()));
  }

  public static void main(String[] args) {

    final StreamsBuilder builder = new StreamsBuilder();
    createWordCountStream(builder);
    final KafkaStreams streams = new KafkaStreams(builder.build(),
        createStreamsConfigProperties("wordCount"));
    streams.cleanUp();

    streams.start();

    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }

}
