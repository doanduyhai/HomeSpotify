package com.datastax.homespotify.rest

import com.datastax.driver.core._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._

import scala.collection.JavaConverters._

class Exercise3(implicit session: Session) extends HomespotifyStack with JacksonJsonSupport{

  protected implicit val jsonFormats: Formats = DefaultFormats
  lazy val checkResults:PreparedStatement = session.prepare("SELECT * FROM top10_styles")
  lazy val getDistributionsPs:PreparedStatement = session.prepare("SELECT type,style,count FROM top10_styles")

  type Str = java.lang.String
  type I = java.lang.Integer

  before() {
    contentType = formats("json")
  }

  get("/verify_results") {
    val rows = session.execute(checkResults.bind()).all().asScala.toList
    val rockGroupCount: Int = extractCount(rows.filter(r => r.getString("type") == "group" && r.getString("style") == "Rock").headOption)
    val danceGroupCount: Int = extractCount(rows.filter(row => row.getString("type") == "group" && row.getString("style") == "Dance").headOption)

    val jazzArtistCount: Int = extractCount(rows.filter(row => row.getString("type") == "artist" && row.getString("style") == "Jazz").headOption)
    val classicArtistCount: Int = extractCount(rows.filter(row => row.getString("type") == "artist" && row.getString("style") == "Classic").headOption)

    Map("result" -> (rockGroupCount == 1594 && danceGroupCount == 222
                      && jazzArtistCount == 142 && classicArtistCount == 364))
  }

  get("/top10_style") {

    val rows = session.execute(getDistributionsPs.bind()).all().asScala.toList
    val aggregated:Map[String,List[List[Any]]] = rows
      .map(r => (r.getString("type"),(r.getString("style"),r.getInt("count"))))
      .groupBy[String]{ case (performer_type,(style,count)) => performer_type}
      .map{ case(key,list) => (key, list.map{case (performer_type,(style,count)) => List(style,count)})}

    aggregated
  }

  def extractCount(maybeRow: Option[Row]):Int = {
    maybeRow match {
      case Some(row) => row.getInt("\"count\"");
      case None => -1
    }
  }

}
