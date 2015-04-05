package com.datastax.homespotify.rest

import com.datastax.driver.core._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._

import scala.collection.JavaConverters._
import scala.collection.immutable.Iterable

class Exercise5(implicit session: Session) extends HomespotifyStack with JacksonJsonSupport{

  protected implicit val jsonFormats: Formats = DefaultFormats
  lazy val checkResults:PreparedStatement = session.prepare("SELECT count(*) FROM albums_by_decade_and_country_sql")
  lazy val getCountries:PreparedStatement = session.prepare("SELECT decade,country,album_count FROM albums_by_decade_and_country_sql")

  type Str = java.lang.String
  type I = java.lang.Integer

  before() {
    contentType = formats("json")
  }

  get("/verify_results") {
    val row = session.execute(checkResults.bind()).one()
    val count: Long = row.getLong("count")
    Map("result" -> (count == 51L))
  }


  get("/countries_distribution") {
    val rows = session.execute(getCountries.bind()).all().asScala.toList

    val distinctCountries: List[String] = rows.map(r => r.getString("country")).toSet.toList.sorted
    val distinctDecades: List[String] = rows.map(r => r.getString("decade")).toSet.toList.sorted

    val countriesDistribution: Iterable[List[Any]] = rows
      .map(r => (r.getString("country"), (r.getString("decade"), r.getInt("album_count"))))
      //group by country => key = country,val = List(country,(decade,count))
      .groupBy { case (country, ((decade, count))) => country}
      // transform the List[(decade,count)] into Map[decade,count]
      .mapValues(list => list.map { case (country, (decade, count)) => (decade, count)}.toMap)
      // transform the Map[decade,count] to a List(decade1_count,decade2_count,..)
      // missing decade has 0 as value
      .mapValues(map => distinctDecades.map(decade => map.getOrElse(decade, "N/A")))
      // merge key and value together to produce
      // List(country,decade1_count,decade2_count,....)
      .map { case (key, value) => List(key) ::: value}

    Map("decades" -> distinctDecades,
      "countries" -> distinctCountries,
      "distribution" -> countriesDistribution)
  }
}
