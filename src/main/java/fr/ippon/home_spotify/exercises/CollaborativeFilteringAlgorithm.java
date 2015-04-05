package fr.ippon.home_spotify.exercises;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fr.ippon.home_spotify.exercises.Constants.EXERCISE_6;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class CollaborativeFilteringAlgorithm extends BaseExercise {

    public static void main(String[] args) {
        JavaSparkContext sc = buildSparkContext(EXERCISE_6);

        // Initialize a utility matrix

        Tuple2<String,List<Tuple2<String,Integer>>> user1Ratings = new Tuple2<>("User1",
                asList(new Tuple2<>("Artist1", 5),new Tuple2<>("Artist2", 2),new Tuple2<>("Artist3", 4)));

        Tuple2<String,List<Tuple2<String,Integer>>> user2Ratings = new Tuple2<>("User2",
                asList(new Tuple2<>("Artist1", 4),                          new Tuple2<>("Artist3", 4),new Tuple2<>("Artist4", 5),new Tuple2<>("Artist5", 1)));

        Tuple2<String,List<Tuple2<String,Integer>>> user3Ratings = new Tuple2<>("User3",
                asList(new Tuple2<>("Artist1", 1),new Tuple2<>("Artist2", 5),                                                                               new Tuple2<>("Artist6", 2)));

        Tuple2<String,List<Tuple2<String,Integer>>> user4Ratings = new Tuple2<>("User4",
                asList(                                                                                     new Tuple2<>("Artist4", 3),                     new Tuple2<>("Artist6", 3)));

        final List<Tuple2<String,List<Tuple2<String,Integer>>>> data = asList(user1Ratings, user2Ratings, user3Ratings, user4Ratings);



        // Set recommendation system parameters
        final double threshold = 0; // 0.87 > cos(30)
        final int topK = 50;

        final JavaRDD<Tuple2<String, String>> recommendations = computeRecommendations(sc, data, threshold, topK);

        // Output recommendations
        System.out.println("Recommendations : "+recommendations.collect().toString());

        sc.stop();

    }

    public static JavaRDD<Tuple2<String,String>> computeRecommendations(JavaSparkContext sc,List<Tuple2<String,List<Tuple2<String,Integer>>>> data,
                                                                        double threshold, int topK) {

        final JavaRDD<Tuple2<String, List<Tuple2<String, Integer>>>> matrix = sc.parallelize(data);

        // Center the ratings
        final JavaPairRDD<String, List<Tuple2<String, Double>>> centeredRatingMatrix =
                matrix
                        .mapToPair(tuple -> tuple)
                        .mapValues(artistRatingList -> {
                            final Double average = avg(artistRatingList.stream().map(kv -> new Double(kv._2())).collect(toList()));
                            return artistRatingList
                                    .stream()
                                    .map(keyValue -> new Tuple2<>(keyValue._1(), keyValue._2() - average))
                                    .filter(keyValue -> keyValue._2() != 0)
                                    .collect(toList());
                        })
                        .filter(userArtistRatings -> userArtistRatings._2().size() != 0);

        System.out.println("Centered matrix : "+centeredRatingMatrix.collect().toString());

        // Find similar users
        // RDD[similarity, (user1, list of (artist,rating)),(user2, list of (artist,rating))]
        final JavaRDD<Tuple2<Double, Tuple2<Tuple2<String, List<Tuple2<String, Double>>>, Tuple2<String, List<Tuple2<String, Double>>>>>> similarUsers =
                centeredRatingMatrix
                        // Cross product of all users ((String,List[..]), (String,List[..]))
                        .cartesian(centeredRatingMatrix)
                                // Remove identical users
                                // userCouples = ((user1,artistRatingList1), (user2,artistRatingList2))
                        .filter(userCouples -> !userCouples._1()._1().equals(userCouples._2()._1()))
                                // Compute cosine similarity using the predefined function cosineSimilarity()
                                // userCouples = ((user1,artistRatingList1), (user2,artistRatingList2))
                        .map(userCouples -> {
                                    final List<Tuple2<String, Double>> ratings1 = userCouples._1()._2();
                                    final List<Tuple2<String, Double>> ratings2 = userCouples._2()._2();
                                    final Double cosineSimilarity = cosineSimilarity(new ArrayList<>(ratings1), new ArrayList<>(ratings2));
                                    return new Tuple2<>(cosineSimilarity, userCouples);
                                }
                        )
                                // Keep only users whose similarity is beyond the threshold
                                // complexTuple = (similarity,((user1,ratingsList1),(user2,ratingsList2))
                        .filter(complexTuple -> complexTuple._1() >= threshold);

        System.out.println("Similar users : "+similarUsers.collect().toString());

        // Merge ratings of all similar users for each user
        final JavaPairRDD<Tuple2<String, List<Tuple2<String, Double>>>, List<Tuple2<String, Double>>> mergedRatings =
                similarUsers
                        // Extract only user and similar ratings list
                        // complexTuple = (similarity,((user1,ratingsList1),(user2,ratingsList2))
                        // target tuple = ((user1,ratingsList1),ratingsList2)
                        .mapToPair(complexTuple -> new Tuple2<>(complexTuple._2()._1(), complexTuple._2()._2()._2()))
                                // Merge all similar ratings for each user together
                        .reduceByKey(mergeRatings());

        System.out.println("Merged ratings : "+mergedRatings.collect().toString());

        // Compute an average rating for each artist across similar users
        final JavaPairRDD<Tuple2<String, List<Tuple2<String, Double>>>, List<Tuple2<String, Double>>> averageRatings =
                mergedRatings.mapValues(artistRatingList ->
                                artistRatingList
                                        .stream()
                                                // Group ratings by artist
                                        .collect(groupingBy(keyValue -> keyValue._1()))
                                        .entrySet().stream()
                                        // Average rating
                                        .map((Map.Entry<String, List<Tuple2<String, Double>>> keyValue) -> {
                                            final String artist = keyValue.getKey();
                                            final List<Tuple2<String, Double>> ratingsList = keyValue.getValue();
                                            final Double average = avg(ratingsList.stream().map(artistRating -> artistRating._2()).collect(toList()));
                                            return new Tuple2<>(artist, average);
                                        }).collect(toList())
                );

        System.out.println("Average ratings : "+averageRatings.collect().toString());

        // Filter out non-positive ratings and artists that the user already rated
        // complexTuple = ((user,artistRatingsList),similarRatings)
        final JavaPairRDD<String, List<Tuple2<String, Double>>> processedRatings =
                averageRatings.mapToPair(complexTuple -> {
                    final String user = complexTuple._1()._1();
                    final Set<String> artistsRatedByUser = complexTuple._1()._2().stream().map(artistRating -> artistRating._1()).collect(toSet());
                    final List<Tuple2<String, Double>> similarRatings = complexTuple._2();
                    final List<Tuple2<String, Double>> filteredRatings = similarRatings
                            .stream()
                            .filter(artistRating -> artistRating._2() > 0 && !artistsRatedByUser.contains(artistRating._1()))
                            .collect(toList());
                    return new Tuple2<>(user, filteredRatings);
                });

        System.out.println("Processed ratings : "+processedRatings.collect().toString());

        // Compute recommendations
        final JavaRDD<Tuple2<String, String>> recommendations = processedRatings
                // Keep topK albums with the highest average ratings
                .mapValues(filteredRatings -> {
                    final ArrayList<Tuple2<String, Double>> filtered = new ArrayList<>(filteredRatings);
                    filtered.sort(COMPARATOR);
                    return filtered.stream().limit(topK).collect(toList());
                })
                        // Convert records into “flat” (user, album) pairs
                        // .flatMap{ case(user,topRatings) => topRatings.map{ case(artist,rating) => (user,artist)}}
                .flatMap(couple -> couple._2().stream().map(artistRating -> new Tuple2<>(couple._1(), artistRating._1())).collect(toList()));

        return recommendations;
    }

    public static Double avg(List<Double> values) {
        if (values.size() > 0) {
            return values.stream().mapToDouble(x -> x).sum() / values.size();
        } else {
            return 0d;
        }
    }

    public static <T> Double cosineSimilarity(List<Tuple2<T,Double>> l1, List<Tuple2<T,Double>> l2) {
        final List<Tuple2<T,Double>> listUnion = l1;
        listUnion.addAll(l2);

        final double numerator = listUnion
                .stream()
                .collect(groupingBy(keyValue -> keyValue._1()))
                .entrySet().stream()
                .filter(keyValue -> keyValue.getValue().size() == 2)
                .map(keyValue -> keyValue.getValue()
                        .stream()
                        .map(artistRating -> artistRating._2())
                        .reduce(1d, (a, b) -> a * b))
                .mapToDouble(x -> x)
                .sum();

        final double denominator =
                Math.sqrt(l1.stream().map(keyValue -> keyValue._2()*keyValue._2()).mapToDouble(x->x).sum()) *
                        Math.sqrt(l2.stream().map(keyValue -> keyValue._2()*keyValue._2()).mapToDouble(x->x).sum());


        return numerator/denominator;
    }

    private static Function2<List<Tuple2<String, Double>>, List<Tuple2<String, Double>>, List<Tuple2<String, Double>>> mergeRatings() {
        return new Function2<List<Tuple2<String, Double>>, List<Tuple2<String, Double>>, List<Tuple2<String, Double>>>() {
            @Override
            public List<Tuple2<String, Double>> call(List<Tuple2<String, Double>> v1, List<Tuple2<String, Double>> v2) throws Exception {
                final ArrayList<Tuple2<String, Double>> merged = new ArrayList<>(v1);
                merged.addAll(v2);
                return merged;
            }
        };
    }

    public static Comparator<Tuple2<String,Double>> COMPARATOR = Comparator.<Tuple2<String,Double>,Double>comparing(tuple -> tuple._2(), Comparator.<Double>reverseOrder());
}
