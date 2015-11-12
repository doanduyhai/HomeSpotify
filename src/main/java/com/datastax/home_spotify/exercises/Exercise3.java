package com.datastax.home_spotify.exercises;

import static com.datastax.home_spotify.exercises.Schema.*;

import com.datastax.spark.connector.japi.CassandraJavaUtil;
import com.datastax.home_spotify.entity.PerformerDistributionByStyle;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.List;

import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;

public class Exercise3 extends BaseExercise {

    public static void main(String[] args) {

        JavaSparkContext sc = buildSparkContext(Constants.EXERCISE_3);

        /*
         * Read data from 'performers_distribution_by_style' table
         * and map to the PerformerDistributionByStyle POJO
         */
        JavaRDD<PerformerDistributionByStyle> rows = javaFunctions(sc)
            .cassandraTable(Schema.KEYSPACE, Schema.PERFORMERS_DISTRIBUTION_BY_STYLE)
            .select("type", "style", "count")
            .map(row -> new PerformerDistributionByStyle(row.getString("type"),
                    row.getString("style"),
                    row.getInt("count")));


        JavaRDD<PerformerDistributionByStyle> sortedPerformerDistribution = rows
            .filter(p -> (Boolean)null) //TODO Filter out the 'Unknown' style
            .sortBy(p -> (Integer)null, (Boolean)null, 1); //TODO Sort by count, highest count fist

        // Put the sorted RDD in cache for re-use
        sortedPerformerDistribution.cache();

        // Extract styles for groups
        final JavaRDD<PerformerDistributionByStyle> groupStyles = sortedPerformerDistribution
            .filter(bean -> (Boolean)null); //TODO Take only 'group' type

        // Extract styles for artists
        final JavaRDD<PerformerDistributionByStyle> artistStyles = sortedPerformerDistribution
            .filter(bean -> (Boolean)null); //TODO Take only 'artist' type

        // Cache the groupStyles
        groupStyles.cache();

        // Cache the artistStyles
        artistStyles.cache();

        // Count total number of artists having styles that are not in the top 10
        final int otherStylesCountForGroup = groupStyles
            .collect()   //Fetch the whole RDD back to driver program
            .stream()
            .skip((Integer)null)  //TODO Drop the first 10 top styles
            .mapToInt(bean -> (Integer)null) //TODO Extract the count
            .sum();

        // Count total number of groups having styles that are not in the top 10
        final int otherStylesCountForArtist = artistStyles
            .collect()   //Fetch the whole RDD back to driver program
            .stream()
            .skip((Integer)null)  //TODO Drop the first 10 top styles
            .mapToInt(bean -> (Integer)null) //TODO Extract the count
            .sum();


        // Take the top 10 styles for groups, with a count for all other styles
        final List<PerformerDistributionByStyle> top10Groups = groupStyles.take(10);
        top10Groups.add(new PerformerDistributionByStyle("group", "Others", otherStylesCountForGroup));

        // Take the top 10 styles for artists, with a count for all other styles
        final List<PerformerDistributionByStyle> top10Artists = artistStyles.take(10);
        top10Artists.add(new PerformerDistributionByStyle("artist", "Others", otherStylesCountForArtist));


        /*
         * Remark: by calling take(n), all the data are shipped back to the driver program
         * the output of take(n) is no longer an RDD but a simple Java collection
         */

        // Merge both top10 lists
        JavaRDD<PerformerDistributionByStyle> merged = sc.parallelize(top10Artists)
                .union(sc.parallelize(top10Groups));

        // Save back to Cassandra
        javaFunctions(merged)
                .writerBuilder(Schema.KEYSPACE, Schema.TOP_10_STYLES,
                        CassandraJavaUtil.mapToRow(PerformerDistributionByStyle.class))
                .saveToCassandra();
    }

}