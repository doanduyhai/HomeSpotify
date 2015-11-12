package com.datastax.home_spotify.exercises;

import com.google.common.collect.ImmutableMap;
import com.datastax.home_spotify.entity.PerformerDistributionByStyle;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapToRow;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.typeConverter;

public class Exercise2 extends BaseExercise {

    public static final Map<String,String> PERFORMERS_TYPES = ImmutableMap.of("Person","artist","Group","group");

    public static void main(String[] args) {

        JavaSparkContext sc = buildSparkContext(Constants.EXERCISE_2);

        /*
         * Read columns "type" and "styles" from table 'performers'
         * and save them as (String,List[String]) RDDs
         * using the .as((_:String,_:List[String])) type conversion function
         * Normalize the performer type by filtering out 'Unknown' types
         */
        JavaRDD<Tuple2<String,List<String>>> rows = javaFunctions(sc)
            .cassandraTable(Schema.KEYSPACE, Schema.PERFORMERS)
            .select("type", "styles")
            .map(row -> new Tuple2<String, List<String>>(PERFORMERS_TYPES.getOrDefault(row.getString("type"), "Unknown"),
                            row.getList("styles", typeConverter(String.class)))
            )
            .filter(tuple -> !tuple._1().equals("Unknown"));

        /*
         * Transform the previous tuple RDDs into a key/value RDD (PairRDD) of type
         * ((String,String),Integer). The (String,String) pair is the key(performer type,style)
         * The Integer value should be set to 1 for each element of the RDD
         */
        final JavaPairRDD<Tuple2<String, String>, Integer> pairs = rows
            .flatMapToPair(tuple -> tuple.<List<String>>_2()
                .stream()
                .map(style -> new Tuple2<>(new Tuple2<String, String>(tuple._1(), style), 1))
                .collect(Collectors.toList()));

        /*
         * Reduce the previous tuple of ((performer type,style),1) by
         * adding up all the 1's into a  ((performer type,style),count)
         */
        final JavaPairRDD<Tuple2<String,String>, Integer> reduced = pairs
            .reduceByKey((left, right) -> left + right);

        /*
         * Map the ((performer type,style),count) into the PerformerDistributionByStyle POJO
         */
        final JavaRDD<PerformerDistributionByStyle> performersDistributionByStyle = reduced
            .map(tuple -> new PerformerDistributionByStyle(tuple._1()._1(),tuple._1()._2(),tuple._2()) );

        // Save data back to the performers_distribution_by_style table
        javaFunctions(performersDistributionByStyle)
            .writerBuilder(Schema.KEYSPACE, Schema.PERFORMERS_DISTRIBUTION_BY_STYLE,
                    mapToRow(PerformerDistributionByStyle.class))
            .saveToCassandra();
    }

}
