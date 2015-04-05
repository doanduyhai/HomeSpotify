package com.datastax.home_spotify.exercises

import com.datastax.home_spotify.exercises.Constants._
import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.{SparkContext, SparkConf}

trait BaseExercise {

  def buildSparkContext(exerciseName: String):SparkContext = {

    val conf = new SparkConf(true)
      .setAppName(exerciseName)
      .setMaster(LOCAL_MODE)
      .set(CASSANDRA_HOST_NAME_PARAM, CASSANDRA_IP)


    if (Constants.TABLES.contains(exerciseName)) {
      CassandraConnector(conf).withSessionDo {
        session => session.execute(s"TRUNCATE ${Schema.KEYSPACE}.${Constants.TABLES(exerciseName)}")
      }
    }

    new SparkContext(conf)
  }
}
