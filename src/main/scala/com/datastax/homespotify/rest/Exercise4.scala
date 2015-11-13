package com.datastax.homespotify.rest

import com.datastax.driver.core._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._

import scala.collection.JavaConverters._

class Exercise4(implicit session: Session) extends HomespotifyStack with JacksonJsonSupport{

  protected implicit val jsonFormats: Formats = DefaultFormats
  lazy val checkResults:PreparedStatement = session.prepare("SELECT count(*) FROM albums_by_decade_and_country")
  lazy val getDecades:PreparedStatement = session.prepare("SELECT distinct decade FROM albums_by_decade_and_country")
  lazy val getCountriesForDecade:PreparedStatement = session.prepare("SELECT country,album_count FROM albums_by_decade_and_country WHERE decade = ?")

  type Str = java.lang.String
  type I = java.lang.Integer

  before() {
    contentType = formats("json")
  }

  get("/verify_results") {
    val row = session.execute(checkResults.bind()).one()
    val count: Long = row.getLong("count")
    Map("result" -> (count == 410L))
  }

  get("/load_decades") {
    val rows = session.execute(getDecades.bind()).all().asScala.toList
    rows.map(r => r.getString("decade"))
    .sorted
  }

  get("/countries_for_decade/:decade") {
    val rows = session.execute(getCountriesForDecade.bind(params("decade"))).all().asScala.toList
    val countries: List[List[Any]] = rows
      .map(r => (r.getString("country"), r.getInt("album_count")))
      .sortBy(couple => couple._2)(Ordering[Int].reverse)
      .map{case (country,count) => List(country,count)}

    Map("countries" -> countries)
  }
}
