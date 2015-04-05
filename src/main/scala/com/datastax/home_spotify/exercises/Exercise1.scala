package com.datastax.home_spotify.exercises

import com.datastax.home_spotify.exercises.Constants.EXERCISE_1
import com.datastax.home_spotify.exercises.Schema._
import com.datastax.spark.connector._
import com.datastax.spark.connector.rdd.CassandraRDD
import org.apache.spark.rdd.RDD
import scala.collection.convert.Wrappers._

object Exercise1 extends BaseExercise {

  def main (args: Array[String]) {


    val sc = buildSparkContext(EXERCISE_1)

    /*
     * performers table structure
     *
     * CREATE TABLE IF NOT EXISTS performers (
     *   name TEXT,
     *   country TEXT,
     *   gender TEXT,
     *   type TEXT,
     *   born TEXT,
     *   died TEXT,
     *   styles LIST<TEXT>,
     *   PRIMARY KEY (name)
     * );
     */
    val rdd:CassandraRDD[CassandraRow] = sc.cassandraTable(KEYSPACE, PERFORMERS)

    val performerAndStyles:RDD[(String,String)] =
      rdd
        .map[(String,List[String])](row => (row.getString("name"),row.getList[String]("styles").toList))
        .flatMap[(String,String)]{ case (performer,styles) => styles.map(style => (style,performer))}

    //Save data back to Cassandra
    performerAndStyles.saveToCassandra(KEYSPACE,PERFORMERS_BY_STYLE,SomeColumns("style","performer"))
    
    sc.stop()
  }
}
