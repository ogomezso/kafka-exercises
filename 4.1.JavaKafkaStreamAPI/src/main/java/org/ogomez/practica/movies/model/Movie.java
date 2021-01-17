package org.ogomez.practica.movies.model;

import java.io.Serializable;

public class Movie implements Serializable {

  private String id;
  private String title;
  private String releaseYear;

  public Movie() {}

  public Movie(String id, String title, String releaseYear) {
    this.id = id;
    this.title = title;
    this.releaseYear = releaseYear;
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

//  @Override
//  public String toString() {
//    return "Movie{" +
//        "id=" + id +
//        ", title=" + title +
//        ", releaseYear=" + releaseYear +
//        '}';
//  }
}
