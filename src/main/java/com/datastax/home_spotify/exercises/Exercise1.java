package com.datastax.home_spotify.exercises;

import static com.datastax.home_spotify.exercises.Schema.*;

import com.datastax.spark.connector.japi.CassandraRow;
import com.datastax.home_spotify.entity.PerformerByStyle;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;


import java.util.List;

import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapToRow;
import static com.datastax.home_spotify.exercises.Constants.EXERCISE_1;
import static java.util.stream.Collectors.toList;

public class Exercise1 extends BaseExercise {

    public static void main(String[] args) {

        JavaSparkContext sc = buildSparkContext(EXERCISE_1);

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
        final JavaRDD<CassandraRow> rows = javaFunctions(sc).cassandraTable(KEYSPACE, PERFORMERS);

        final JavaRDD<PerformerByStyle> performerAndStyles =
            rows
            //TODO Transform a CassandraRow object into a Tuple2<>(performer,list of styles)
            //TODO Use the API CassandraRow.getString("???")
            //TODO and CassandraRow.getList("???",CassandraJavaUtil.typeConverter(???.class)) ...
                    .map(row -> new Tuple2<String,List<String>>(null, null))
            //TODO Extract all styles for each performer using flatMap
            //TODO and create the POJO PerformerByStyle
                    .flatMap(tuple -> tuple._2()
                            .stream()
                            .map(style -> (PerformerByStyle) null)
                    .collect(toList()));

        // Save data back to Cassandra
        javaFunctions(performerAndStyles).writerBuilder(KEYSPACE, PERFORMERS_BY_STYLE, mapToRow(PerformerByStyle.class))
            .saveToCassandra();
    }

}