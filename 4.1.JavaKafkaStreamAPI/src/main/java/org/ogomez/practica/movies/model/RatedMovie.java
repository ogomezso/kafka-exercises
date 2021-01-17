package org.ogomez.practica.movies.model;

import java.io.Serializable;

public class RatedMovie implements Serializable {

  private String id;
  private String title;
  private String releaseYear;
  private Long ratedMovie;

  public RatedMovie() {}

  public RatedMovie(String id, String title, String releaseYear, Long ratedMovie) {
    this.id = id;
    this.title = title;
    this.releaseYear = releaseYear;
    this.ratedMovie = ratedMovie;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getReleaseYear() {
    return releaseYear;
  }

  public void setReleaseYear(String releaseYear) {
    this.releaseYear = releaseYear;
  }

  public Long getRatedMovie() {
    return ratedMovie;
  }

  public void setRatedMovie(Long ratedMovie) {
    this.ratedMovie = ratedMovie;
  }

  @Override
  public String toString() {
    return "RatedMovie{" +
        "id='" + id +
        ", title='" + title +
        ", releaseYear='" + releaseYear +
        ", ratedMovie=" + ratedMovie +
        '}';
  }
}
