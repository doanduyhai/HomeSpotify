package com.datastax.home_spotify.exercises

import Constants._
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext._


object CollaborativeFilteringAlgorithm extends BaseExercise {

  // Program entry
  def main(args: Array[String]) {

    val sc = buildSparkContext(EXERCISE_6)

    // Set recommendation system parameters
    val threshold = 0d // 0.87 > cos(30)
    val topK = 50

    // Initialize a utility matrix
    val data:List[(String,List[(String,Int)])] = List(
      ("User1", List( ("Artist1",5),("Artist2",2),("Artist3",4) )),
      ("User2", List( ("Artist1",4),              ("Artist3",4),("Artist4",5),("Artist5",1) )),
      ("User3", List( ("Artist1",1),("Artist2",5),                                         ("Artist6",2) )),
      ("User4", List(                                           ("Artist4",3),             ("Artist6",3) ))
    )

    val recommendations = computeRecommendations(sc,data, threshold, topK)


    // Output recommendations
    recommendations.collect.foreach(println)

    sc.stop()
  }


  // Compute average
  def avg(r: List[Double]) : Double = r.sum / r.size

  // Compute cosine similarity
  def cosineSimilarity[T](l1: List[(T,Double)], l2: List[(T,Double)]) : Double = {

    val numerator = ( l1 ++ l2 )
      .groupBy{ case(key,value) => key }
      .filter{ case(key, collection) => collection.size == 2}
      .map{case (key,couples) => couples.map{ case(key,value) => value}.product}
      .sum

    val denominator = math.sqrt(l1.map{ case(_,value) => value*value}.sum) *
      math.sqrt(l2.map{ case(_,value) => value*value}.sum)

    numerator / denominator
  }



  def computeRecommendations(sc:SparkContext, data:List[(String,List[(String,Int)])],
                             threshold:Double, topK:Int):RDD[(String,String)] = {
    val matrix = sc.parallelize(data)

    // Center the ratings
    val centeredRatingMatrix:RDD[(String, List[(String,Double)])] =
      matrix.mapValues(
        artistRatingList => {
          val average: Double = avg(artistRatingList.map{ case (artist,rating) => rating.toDouble})
          artistRatingList
            .toMap
            .mapValues(rating => rating - average)
            .filter{ case(artist,rating) => rating != 0} //Remove rating = 0
            .toList
        }
      ).filter{ case (user,artistRatingList) => artistRatingList.size != 0}


    println("Centered matrix : "+centeredRatingMatrix.collect().mkString(","))

    // Find similar users
    // RDD[similarity, (user1, list of (artist,rating)),(user2, list of (artist,rating))]
    val similarUsers: RDD[(Double, ((String, List[(String, Double)]),(String, List[(String, Double)])))] =
      centeredRatingMatrix
        // Cross product of all users ((String,List[..]), (String,List[..]))
        .cartesian(centeredRatingMatrix)
        // Remove identical users
        .filter { case ((user1,_), (user2,_)) => user1 != user2}
        // Compute cosine similarity using the predefined function cosineSimilarity()
        .map { case ((user1,artistRatingList1), (user2,artistRatingList2))
      => (cosineSimilarity(artistRatingList1, artistRatingList2), ((user1,artistRatingList1),(user2,artistRatingList2)))}
        // Keep only users whose similarity is beyond the threshold
        .filter { case(similarity,_)  => similarity >= threshold}

    println("Similar users : "+similarUsers.collect().mkString(","))

    // Merge ratings of all similar users for each user
    val mergedRatings: RDD[((String,List[(String,Double)]), List[(String,Double)])] =
      similarUsers
        // Extract only user and similar ratings list
        .map{case (similarity,((user1,artistRatingList1),(user2,artistRatingList2))) => ((user1,artistRatingList1),artistRatingList2)}
        // Merge all similar ratings for each user together
        .reduceByKey{ case (artistRatingList1,artistRatingList2) => artistRatingList1 ++ artistRatingList2}

    println("Merged ratings : "+mergedRatings.collect().mkString(","))

    val averagedRatings = mergedRatings
      // Compute an average rating for each artist across similar users
      .mapValues(artistRatingList =>
      artistRatingList
        // Group ratings by artist
        .groupBy{case(artist,rating) => artist}
        // Average rating
        .mapValues(artistRatingList => avg(artistRatingList.map(_._2)))
        .toList
      )

    println("Averaged ratings : "+averagedRatings.collect().mkString(","))

    val cleanedRatings = averagedRatings
      // Filter out non-positive ratings and albums that the user already rated
      .map{ case((user,artistRatingsList),similarRatings) =>
      (user, similarRatings
        .filter{ case (artist,rating) => rating > 0 && !artistRatingsList.map(_._1).contains(artist)} )}

    println("Cleaned ratings : "+cleanedRatings.collect().mkString(","))

    // Compute recommendations
    val recommendations:RDD[(String,String)] = cleanedRatings
      // Keep topK albums with the highest average ratings
      .mapValues(ratings => ratings.sortBy{case (artist,rating) => rating}(Ordering[Double].reverse).take(topK))
      // Convert records into “flat” (user, artist) pairs
      .flatMap{ case(user,topRatings) => topRatings.map{ case(artist,rating) => (user,artist)}}

    recommendations
  }

}
