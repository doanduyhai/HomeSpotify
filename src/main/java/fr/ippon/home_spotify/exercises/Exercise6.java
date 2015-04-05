package fr.ippon.home_spotify.exercises;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.spark.connector.cql.CassandraConnector;
import com.datastax.spark.connector.cql.CassandraConnectorConf;
import com.google.common.base.Joiner;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static fr.ippon.home_spotify.exercises.Constants.EXERCISE_6;
import static java.util.stream.Collectors.toList;


public class Exercise6 extends BaseExercise {

    private static final String TOP_K_USERS_RATINGS_FOR_ARTIST = "SELECT * FROM home_spotify.ratings_by_artist WHERE artist = :artist LIMIT 10";

    private static final String TOP_K_RATINGS_BY_USER = "SELECT * FROM home_spotify.ratings_by_user WHERE user = :user LIMIT 10";

    public static void main(String[] args) {

        /**
         * Try the following combinations for recommendations:
         *
         * - new Tuple2<>("Coldplay",5),new Tuple2<>("U2",4)
         * - new Tuple2<>("The Beatles",5)
         * - new Tuple2<>("Mariah Carey",4)
         * - new Tuple2<>("2Pac",5),new Tuple2<>("Beyoncé",4)
         */
        Set<String> recommendations = recommendSimilarArtistsAs(new Tuple2<>("Coldplay", 5), new Tuple2<>("U2", 4));


        System.out.println("*************************************************************************************");
        System.out.println("*");
        System.out.println("*");
        System.out.println("* Recommended artists : " + Joiner.on(",").join(recommendations));
        System.out.println("*");
        System.out.println("*");
        System.out.println("*************************************************************************************");
    }


    private static Set<String> recommendSimilarArtistsAs(Tuple2<String,Integer> ... artists) {

        JavaSparkContext sc = buildSparkContext(EXERCISE_6);

        final Session session = new CassandraConnector(CassandraConnectorConf.apply(sc.getConf())).openSession();

        final PreparedStatement userRatingsForArtistPs = session.prepare(TOP_K_USERS_RATINGS_FOR_ARTIST);
        final PreparedStatement ratingsByUserPs = session.prepare(TOP_K_RATINGS_BY_USER);

        final List<Tuple2<String, Integer>> sourceArtists = Arrays.asList(artists);

        final List<Tuple2<String,List<Tuple2<String,Integer>>>> candidateRatings = new ArrayList<>(sourceArtists)
                .stream()
                .map(tuple -> tuple._1())
                .flatMap(artist -> usersRatingThisArtist(artist, userRatingsForArtistPs, session).stream())
                .map(userThatRatedThisArtist -> new Tuple2<>(userThatRatedThisArtist,ratingsForUser(userThatRatedThisArtist, ratingsByUserPs, session)))
                .collect(toList());

        final List<Tuple2<String,Integer>> normalizedArtistRatings;
        if (artists.length == 1) {
            final List<Tuple2<String,Integer>> myRating = new ArrayList<>(sourceArtists);
            myRating.add(new Tuple2<>("xxxxxxx",0));
            normalizedArtistRatings = myRating;
        } else {
            normalizedArtistRatings = new ArrayList<>(sourceArtists);
        }

        candidateRatings.add(new Tuple2<>("ME", normalizedArtistRatings));

        final JavaRDD<Tuple2<String, String>> recommendations = CollaborativeFilteringAlgorithm.computeRecommendations(sc, candidateRatings, 0.0, 6);

        final Set<String> results = new HashSet<>(recommendations
                .filter(couple -> couple._1().equals("ME"))
                .map(couple -> couple._2())
                .collect());

        session.close();

        sc.stop();

        return results;
    }

    private static List<String> usersRatingThisArtist(String artist,PreparedStatement userRatingsForArtistPs, Session session){
        return session.execute(userRatingsForArtistPs.bind(artist))
                .all()
                .stream()
                .map(row -> row.getString("user"))
                .collect(toList());
    }

    private static List<Tuple2<String,Integer>> ratingsForUser(String user,PreparedStatement ratingsByUserPs, Session session) {
        return session.execute(ratingsByUserPs.bind(user))
                .all()
                .stream()
                .map(row -> new Tuple2<>(row.getString("artist"), row.getInt("rating")))
                .collect(toList());
    }

}