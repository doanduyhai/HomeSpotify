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
        //TODO Transform a CassandraRow object into a (performer,list of styles) tuple
        //TODO Use the API CassandraRow.getString("???"), CassandraRow.getList[???]("???").toList ...
        .map[(String,List[String])](???)
        //TODO Extract all styles for each performer and create the tuple (style,performer)
        //TODO Each performer can have more than 1 style
        //TODO BE CAREFUL!!! The columns ordering ("style" and then "performer") is IMPORTANT
        .flatMap[(String,String)](???)

    //Save data back to Cassandra
    performerAndStyles.saveToCassandra(KEYSPACE,PERFORMERS_BY_STYLE,SomeColumns("style","performer"))
    
    sc.stop()
  }
}
