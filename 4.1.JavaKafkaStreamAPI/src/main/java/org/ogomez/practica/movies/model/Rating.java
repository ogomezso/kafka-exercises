package org.ogomez.practica.movies.model;

import java.io.Serializable;

public class Rating implements Serializable {

  private String id;
  private Long rating;

  public Rating(String id, Long rating) {
    this.id = id;
    this.rating = rating;
  }

  public Rating() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Long getRating() {
    return rating;
  }

  public void setRating(Long rating) {
    this.rating = rating;
  }

  @Override
  public String toString() {
    return "Ratings{" +
        "id='" + id +
        ", rating=" + rating +
        '}';
  }
}
