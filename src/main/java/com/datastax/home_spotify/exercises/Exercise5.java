package com.datastax.home_spotify.exercises;

import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapToRow;
import static com.datastax.home_spotify.exercises.Constants.EXERCISE_5;
import static com.datastax.home_spotify.exercises.Schema.*;

import com.datastax.home_spotify.entity.AlbumByDecadeAndCountry;
import com.datastax.spark.connector.util.JavaApiHelper;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import scala.runtime.AbstractFunction1;

import java.io.Serializable;



public class Exercise5 extends BaseExercise {

    public static void main(String[] args) {

        SparkContext sc = new SparkContext(buildScalaSparkConf(EXERCISE_5));
        final JavaSparkContext javaSc = new JavaSparkContext(sc);
        final SQLContext sqlContext = new SQLContext(javaSc);

        // Register performers in DataFrame
        sqlContext.sql("CREATE TEMPORARY TABLE performers" +
                " USING org.apache.spark.sql.cassandra" +
                " OPTIONS (keyspace \"" + Schema.KEYSPACE + "\",  table \"" + Schema.PERFORMERS + "\", pushdown \"true\")");

        // Register albums in DataFrame
        sqlContext.sql("CREATE TEMPORARY TABLE albums" +
                " USING org.apache.spark.sql.cassandra" +
                " OPTIONS (keyspace \"" + Schema.KEYSPACE + "\",  table \"" + Schema.ALBUMS + "\", pushdown \"true\")");

        // Register computeDecade() as a SparkSQL function
        // Declare the UDF using the Scala API because of this bug
        // https://issues.apache.org/jira/browse/SPARK-9435
        sqlContext.udf().register("computeDecade", new Exercise5.ComputeDecadeFn(),
                JavaApiHelper.getTypeTag(String.class),
                JavaApiHelper.getTypeTag(Integer.class));

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

        //TODO
        String query = " SELECT ???" +
                "      ???";

        // Execute the SQL statement against Cassandra and Spark
        final DataFrame dataFrame = sqlContext.sql(query);

        // Map back the DataFrame into a the AlbumByDecadeAndCountry POJO
        final JavaRDD<AlbumByDecadeAndCountry> mapped = dataFrame
                .javaRDD()
                .map(row -> new AlbumByDecadeAndCountry(row.getString(0), row.getString(1), new Long(row.getLong(2)).intValue()));

        // Save back to Cassandra
        javaFunctions(mapped)
                .writerBuilder(Schema.KEYSPACE, Schema.ALBUMS_BY_DECADE_AND_COUNTRY_SQL,mapToRow(AlbumByDecadeAndCountry.class))
                .saveToCassandra();

        sc.stop();
    }

    private static String computeDecade(Integer year) {
        int lowerBound = (year/10)*10;
        int upperBound = ((year/10)+1)*10;
        return lowerBound+"-"+upperBound;
    }

    public static final class ComputeDecadeFn extends AbstractFunction1<Integer,String> implements Serializable {

        @Override
        public String apply(Integer year) {
            return computeDecade(year);
        }
    }
}
