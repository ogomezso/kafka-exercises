package org.ogomez.practica.movies.serializers;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.ogomez.practica.movies.model.Movie;
import org.ogomez.practica.movies.model.RatedMovie;
import org.ogomez.practica.movies.model.Rating;

public final class MovieCustomSerdes {

  public static Serde<Movie> Movie() {
    return new MovieSerde();
  }

  public static Serde<Rating> Rating() {
    return new RatingSerde();
  }

  public static Serde<RatedMovie> RatedMovie() {
    return new RatedMovieSerde();
  }

  public static final class MovieSerde extends Serdes.WrapperSerde<Movie> {

    public MovieSerde() {
      super(new MovieSerializer(), new MovieDeserializer());
    }
  }

  public static final class RatingSerde extends Serdes.WrapperSerde<Rating> {

    public RatingSerde() {
      super(new RatingSerializer(), new RatingDeserializer());
    }
  }

  public static final class RatedMovieSerde extends Serdes.WrapperSerde<RatedMovie> {

    public RatedMovieSerde() {
      super(new RatedMovieSerializer(), new RatedMovieDeserializer());
    }
  }
}
