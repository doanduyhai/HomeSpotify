package fr.ippon.home_spotify.exercises;

import com.datastax.spark.connector.util.JavaApiHelper;
import fr.ippon.home_spotify.entity.AlbumByDecadeAndCountry;

import org.apache.spark.SparkContext;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SchemaRDD;
import org.apache.spark.sql.cassandra.CassandraSQLContext;

import org.apache.spark.sql.catalyst.expressions.Row;
import scala.runtime.AbstractFunction1;

import java.io.Serializable;


import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapToRow;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.typeTag;
import static fr.ippon.home_spotify.exercises.Schema.*;
import static fr.ippon.home_spotify.exercises.Constants.EXERCISE_5;

public class Exercise5 extends BaseExercise {

    public static void main(String[] args) {

        SparkContext sc = new SparkContext(buildScalaSparkConf(EXERCISE_5));

        final CassandraSQLContext sqlContext = new CassandraSQLContext(sc);

        // Set the Cassandra keyspace to be used
        sqlContext.setKeyspace(KEYSPACE);

        // Register computeDecade() as a SparkSQL function
        sqlContext.registerFunction("computeDecade", new Exercise5.ComputeDecadeFn(), typeTag(String.class));

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


        String query = "SELECT computeDecade(a.year),p.country,count(a.title) " +
            " FROM performers p JOIN albums a " +
            " ON p.name = a.performer " +
            " WHERE p.country is not null " +
            " AND p.country != 'Unknown' " +
            " AND a.year >= 1900 " +
            " GROUP BY computeDecade(a.year),p.country " +
            " HAVING count(a.title) > 250";

        // Execute the SQL statement against Cassandra and Spark
        final SchemaRDD schemaRDD = sqlContext.cassandraSql(query);

        final JavaRDD<Row> javaRDD = JavaRDD.fromRDD(schemaRDD, JavaApiHelper.getClassTag(Row.class));

        // Map back the Schema RDD into a the AlbumByDecadeAndCountry POJO
        final JavaRDD<AlbumByDecadeAndCountry> mapped = javaRDD.map(row -> new AlbumByDecadeAndCountry(row.getString(0), row.getString(1), new Long(row.getLong(2)).intValue()));

        // Save back to Cassandra
        javaFunctions(mapped)
                .writerBuilder(KEYSPACE,ALBUMS_BY_DECADE_AND_COUNTRY_SQL,mapToRow(AlbumByDecadeAndCountry.class))
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