package com.datastax.home_spotify.exercises

import Constants._
import com.datastax.driver.core.PreparedStatement
import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.rdd.RDD
import scala.collection.JavaConversions._

object Exercise6 extends BaseExercise {

  val TOP_K_USERS_RATINGS_FOR_ARTIST: String = "SELECT * FROM home_spotify.ratings_by_artist WHERE artist = :artist LIMIT 10"

  val TOP_K_RATINGS_BY_USER: String = "SELECT * FROM home_spotify.ratings_by_user WHERE user = :user LIMIT 10"

  // Program entry
  def main(args: Array[String]) {

    /**
     * Try the following combinations for recommendations:
     *
     * - ("Coldplay",5),("U2",4)
     * - ("The Beatles",5)
     * - ("Mariah Carey",4)
     * - ("2Pac",5),("Beyoncé",4)
     */
    val recommendations = recommendSimilarArtistsAs(("Coldplay",5),("U2",4))

    println("*************************************************************************************")
    println("*")
    println("*")
    println("* Recommended artists : "+recommendations.mkString(","))
    println("*")
    println("*")
    println("*************************************************************************************")
  }

  /**
   * Output similar artists as those given in input
   * @param artists couple of (artist,rating) with rating ranging from 1 to 5
   * @return List of "similar" artists
   */
  def recommendSimilarArtistsAs(artists:(String,Int)*): Set[String] = {
    val sc = buildSparkContext(EXERCISE_6)

    implicit val connector: CassandraConnector = CassandraConnector(sc.getConf)

    val userRatingsForArtistPs:PreparedStatement = connector.withSessionDo(_.prepare(TOP_K_USERS_RATINGS_FOR_ARTIST))
    val ratingsByUserPs:PreparedStatement = connector.withSessionDo(_.prepare(TOP_K_RATINGS_BY_USER))

    val candidateRatings: List[(String, List[(String, Int)])] = for (
      artist: String <- artists.toList.map(_._1);
      userThatRatedThisArtist: String <- usersRatingThisArtist(artist, userRatingsForArtistPs)
    ) yield (userThatRatedThisArtist, ratingsForUser(userThatRatedThisArtist, ratingsByUserPs))

    val normalizedArtistRatings = if(artists.size == 1) ("xxxxxx",0)::(artists(0)._1,artists(0)._2*2)::Nil else artists.toList

    val data = ("ME",normalizedArtistRatings)::candidateRatings

    val recommendations:RDD[(String,String)] = CollaborativeFilteringAlgorithm.computeRecommendations(sc,data,0.0,6)

    val result = recommendations.filter{case(user,artist) => user == "ME"}.collect().map(_._2).toSet

    sc.stop()

    result
  }
  
  def usersRatingThisArtist(artist:String,userRatingsForArtistPs:PreparedStatement)(implicit connector:CassandraConnector):List[String] = {
    connector.withSessionDo(_.execute(userRatingsForArtistPs.bind(artist))).all().toList.map(_.getString("user"))
  }

  def ratingsForUser(user:String,ratingsByUserPs:PreparedStatement)(implicit connector:CassandraConnector):List[(String,Int)] = {
    connector.withSessionDo(_.execute(ratingsByUserPs.bind(user))).toList.map(row => (row.getString("artist"), row.getInt("rating")))
  }
}
