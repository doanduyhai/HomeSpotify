package com.datastax.home_spotify.exercises

import com.datastax.home_spotify.exercises.Schema._

object Constants {

  val CASSANDRA_HOST_NAME_PARAM = "spark.cassandra.connection.host"
  val CASSANDRA_IP = "192.168.51.10"
  val LOCAL_MODE = "local"
  
  val EXERCISE_1 = "exercise1"
  val EXERCISE_2 = "exercise2"
  val EXERCISE_3 = "exercise3"
  val EXERCISE_4 = "exercise4"
  val EXERCISE_5 = "exercise5"
  val EXERCISE_6 = "exercise6"

  val TABLES = Map( EXERCISE_1 -> PERFORMERS_BY_STYLE,
                    EXERCISE_2 -> PERFORMERS_DISTRIBUTION_BY_STYLE,
                    EXERCISE_3 -> TOP_10_STYLES,
                    EXERCISE_4 -> ALBUMS_BY_DECADE_AND_COUNTRY,
                    EXERCISE_5 -> ALBUMS_BY_DECADE_AND_COUNTRY_SQL)
}
