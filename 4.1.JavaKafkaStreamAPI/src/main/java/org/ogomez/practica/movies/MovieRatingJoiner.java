package org.ogomez.practica.movies;

import org.apache.kafka.streams.kstream.ValueJoiner;
import org.ogomez.practica.movies.model.Movie;
import org.ogomez.practica.movies.model.RatedMovie;
import org.ogomez.practica.movies.model.Rating;

public class MovieRatingJoiner implements ValueJoiner<Rating, Movie, RatedMovie> {

  @Override
  public RatedMovie apply(Rating rating, Movie movie) {
    return new RatedMovie(movie.getId(), movie.getTitle(), movie.getReleaseYear(),
        rating.getRating());
  }
}
