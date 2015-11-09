package com.datastax.home_spotify.exercises

import com.datastax.home_spotify.exercises.Constants._
import com.datastax.home_spotify.exercises.Schema._
import com.datastax.spark.connector._
import org.apache.spark.sql.{DataFrame, SQLContext}

object Exercise5 extends BaseExercise {

  case class AlbumByDecadeAndCountry(decade: String, country: String, albumCount: Long)

  def main (args: Array[String]) {

    val sc = buildSparkContext(EXERCISE_5)
    val sqlContext: SQLContext = new org.apache.spark.sql.SQLContext(sc)

    // Register performers in DataFrame
    sqlContext.sql(
      s"""
        |CREATE TEMPORARY TABLE performers
        | USING org.apache.spark.sql.cassandra
        | OPTIONS (
        |   keyspace "$KEYSPACE",
        |   table "$PERFORMERS",
        |   pushdown "true"
        | )
      """.stripMargin)

    // Register albums in DataFrame
    sqlContext.sql(
      s"""
        |CREATE TEMPORARY TABLE albums
        | USING org.apache.spark.sql.cassandra
        | OPTIONS (
        |   keyspace "$KEYSPACE",
        |   table "$ALBUMS",
        |   pushdown "true"
        | )
      """.stripMargin)

    // Register computeDecade() as a DataFrame function
    sqlContext.udf.register("computeDecade", computeDecade _)

    /*
     * CREATE TABLE IF NOT EXISTS performers (
     *   name TEXT,
     *   ...
     *   country TEXT,
     *   ...
     * );
     *
     *
     * CREATE TABLE IF NOT EXISTS albums (
     *   ...
     *   title TEXT,
     *   year INT,
     *   performer TEXT,
     *   ...
     * );
     *
     *  - SELECT computeDecade(album release year),artist country, count(album title)
     *  - FROM performers & albums JOINING on performers.name=albums.performer
     *  - WHERE performer' country is not null and different than 'Unknown'
     *  - AND album' release year is greater or equal to 1900
     *  - GROUP BY computeDecade(album release year) and artist country
     *  - HAVING count(albums title)>250 to filter out low values count countries
     */

    val query: String = """
        SELECT computeDecade(a.year),p.country,count(a.title)
        FROM performers p JOIN albums a
        ON p.name = a.performer
        WHERE p.country is not null
        AND p.country != 'Unknown'
        AND a.year >= 1900
        GROUP BY computeDecade(a.year),p.country
        HAVING count(a.title) > 250
      """.stripMargin

    // Execute the SQL statement against Cassandra and Spark
    val rows: DataFrame = sqlContext.sql(query)

    // Map back the Schema RDD into a triplet (decade,country,count)
    rows.map(row => AlbumByDecadeAndCountry(row.getString(0),row.getString(1),row.getLong(2)))
      .saveToCassandra(KEYSPACE, ALBUMS_BY_DECADE_AND_COUNTRY_SQL)

    sc.stop()
  }

  def computeDecade(year:Int):String = {
    s"${(year/10)*10}-${((year/10)+1)*10}"
  }
}
