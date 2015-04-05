package com.datastax.home_spotify.exercises

import com.datastax.home_spotify.exercises.Constants._
import com.datastax.home_spotify.exercises.Schema._
import com.datastax.spark.connector._
import com.datastax.spark.connector.rdd.CassandraRDD
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

object Exercise4 extends BaseExercise {

  def main (args: Array[String]) {

    val sc = buildSparkContext(EXERCISE_4)


    /*
     * CREATE TABLE IF NOT EXISTS performers (
     *   name TEXT,
     *   ...
     *   country TEXT,
     *   ...,
     *   PRIMARY KEY(name)
     * );
     *
     *
     * CREATE TABLE IF NOT EXISTS albums (
     *   ...
     *   year INT,
     *   performer TEXT,
     *   ...,
     * );
     */

    val performers:RDD[(String,String)] = sc.cassandraTable(KEYSPACE, PERFORMERS)
      .select("name","country")
      .as((_:String,_:String))
      //TODO Filter out null countries or "Unknown" countries
      .filter(???)

    val albums:RDD[(String,Int)] = sc.cassandraTable(KEYSPACE, ALBUMS)
      .select("performer","year")
      .as((_:String,_:Int))
      //TODO Only takes years >= 1900
      .filter(???)


    // join performers with albums
    val join: RDD[(String, (String, Int))] = performers.join(albums)

    join
      //TODO RDD into a ((decade,country),1) using the computeDecade() pre-defined function
      .map[((String,String),Int)] {???}
      //TODO Reduce by key to count the number of occurrence for each key (decade,country)
      .reduceByKey(???)
      //TODO Flatten the tuple to (decade,country,count) triplet
      .map[(String,String,Int)] {???}
      .saveToCassandra(KEYSPACE, ALBUMS_BY_DECADE_AND_COUNTRY, SomeColumns("decade","country","album_count"))

    sc.stop()
  }

  def computeDecade(year:Int):String = {
    s"${(year/10)*10}-${((year/10)+1)*10}"
  }
}
