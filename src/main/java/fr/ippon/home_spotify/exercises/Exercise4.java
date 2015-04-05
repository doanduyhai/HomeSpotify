package fr.ippon.home_spotify.exercises;


import fr.ippon.home_spotify.entity.AlbumByDecadeAndCountry;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;


import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapToRow;
import static fr.ippon.home_spotify.exercises.Schema.*;

import static fr.ippon.home_spotify.exercises.Constants.EXERCISE_4;

public class Exercise4 extends BaseExercise{

    public static void main(String[] args) {

        JavaSparkContext sc = buildSparkContext(EXERCISE_4);

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
        JavaPairRDD<String, String> performers = javaFunctions(sc)
                .cassandraTable(KEYSPACE, PERFORMERS)
                .select("name", "country")
                .filter(row -> {
                    String country = row.getString("country");
                    return country != null && !country.equals("Unknown");
                })
                .mapToPair(row -> new Tuple2(row.getString("name"), row.getString("country")));

        JavaPairRDD<String, Integer> albums = javaFunctions(sc)
                .cassandraTable(KEYSPACE, ALBUMS)
                .select("performer", "year")
                .filter(row -> row.getInt("year") >= 1900)
                .mapToPair(row -> new Tuple2(row.getString("performer"), row.getInt("year")));


        //Join performers with albums
        final JavaPairRDD<String, Tuple2<String, Integer>> joins = performers.join(albums);


        final JavaRDD<AlbumByDecadeAndCountry> result = joins
            // Map RDD into a ((decade,country),1) using the computeDecade() pre-defined function
            .mapToPair(join -> new Tuple2<>(new Tuple2<>(computeDecade(join._2()._2()), join._2()._1()), 1))
            // Reduce by key to count the number of occurrence for each key (decade,country)
            .reduceByKey((left, right) -> left+right)
            // Map the tuple into AlbumByDecadeAndCountry POJO
            .map(grouped -> new AlbumByDecadeAndCountry(grouped._1()._1(), grouped._1()._2(), grouped._2()));

        // Save back to Cassandra
        javaFunctions(result)
            .writerBuilder(KEYSPACE, ALBUMS_BY_DECADE_AND_COUNTRY, mapToRow(AlbumByDecadeAndCountry.class))
            .saveToCassandra();

        sc.stop();
    }

    private static String computeDecade(Integer year) {
        int lowerBound = (year/10)*10;
        int upperBound = ((year/10)+1)*10;
        return lowerBound+"-"+upperBound;
    }
}
