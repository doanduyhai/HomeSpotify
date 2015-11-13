package com.datastax.homespotify.rest

import com.datastax.driver.core._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._

import scala.collection.JavaConverters._

class Exercise2(implicit session: Session) extends HomespotifyStack with JacksonJsonSupport {

  protected implicit val jsonFormats: Formats = DefaultFormats
  lazy val checkResults:PreparedStatement = session.prepare("SELECT * FROM performers_distribution_by_style")
  lazy val getDistributionsPs:PreparedStatement = session.prepare("SELECT type,style,count FROM performers_distribution_by_style")

  type Str = java.lang.String
  type I = java.lang.Integer

  before() {
    contentType = formats("json")
  }

  get("/verify_results") {
    val rows = session.execute(checkResults.bind()).all().asScala.toList
    val count: Long = rows.size
    val symphonicGroupCount = rows
      .filter(row => row.getString("type") == "group" && row.getString("style") == "Symphonic")
      .map(row => row.getInt("count"))
      .headOption
      .getOrElse(0)

    Map("result" -> (count == 154L && symphonicGroupCount == 31))
  }

  get("/distribution_by_type_and_style") {

    val rows = session.execute(getDistributionsPs.bind()).all().asScala.toList
    val aggregated:Map[String,List[List[Any]]] = rows
      .map(r => (r.getString("type"),(r.getString("style"),r.getInt("count"))))
      .groupBy[String]{ case (performer_type,(style,count)) => performer_type}
      .map{ case(key,list) => (key, list.map{case (performer_type,(style,count)) => List(style,count)})}

    aggregated
  }



}
