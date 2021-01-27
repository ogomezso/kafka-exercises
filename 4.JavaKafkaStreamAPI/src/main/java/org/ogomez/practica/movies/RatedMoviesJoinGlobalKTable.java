package org.ogomez.practica.movies;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.ogomez.practica.movies.model.Movie;
import org.ogomez.practica.movies.model.RatedMovie;
import org.ogomez.practica.movies.model.Rating;
import org.ogomez.practica.movies.serializers.MovieCustomSerdes;

public class RatedMoviesJoinGlobalKTable {

  public static void main(String[] args) {

    MovieTopology.createTopics();

    final StreamsBuilder builder = new StreamsBuilder();

    KStream<String, Movie> movieStream = builder.stream(MovieTopology.MOVIES_TOPIC,
        Consumed.with(Serdes.String(), MovieCustomSerdes.Movie()));

    final GlobalKTable<String, Rating> ratings =
        builder.globalTable(MovieTopology.RATINGS_TOPIC,
            Materialized.<String, Rating, KeyValueStore<Bytes, byte[]>>as(
                MovieTopology.REKEYED_MOVIES_TOPIC)
                .withKeySerde(Serdes.String())
                .withValueSerde(MovieCustomSerdes.Rating()));


    KStream<String,RatedMovie> ratedMovies = movieStream.join(ratings,
        (movieId,movie) -> movie.getId(),
        (movie,rating) -> new RatedMovie(movie.getId(), movie.getTitle(), movie.getReleaseYear(),
            rating.getRating()));

    ratedMovies.print(Printed.toSysOut());
    ratedMovies.to(MovieTopology.RATED_MOVIES_TOPIC,
        Produced.with(Serdes.String(), MovieCustomSerdes.RatedMovie()));

    Topology topology = builder.build();
    final KafkaStreams streams = new KafkaStreams(topology,
        MovieTopology.createStreamsConfigProperties("moviesGlobalTable"));
    streams.cleanUp();

    streams.start();

    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }


}
