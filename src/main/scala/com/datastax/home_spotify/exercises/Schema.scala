package com.datastax.home_spotify.exercises

object Schema {

  val KEYSPACE = "home_spotify"

  val PERFORMERS = "performers"
  val ALBUMS = "albums"
  val PERFORMERS_BY_STYLE = "performers_by_style"
  val PERFORMERS_DISTRIBUTION_BY_STYLE = "performers_distribution_by_style"
  val TOP_10_STYLES = "top10_styles"
  val ALBUMS_BY_DECADE_AND_COUNTRY = "albums_by_decade_and_country"
  val ALBUMS_BY_DECADE_AND_COUNTRY_SQL = "albums_by_decade_and_country_sql"
}
