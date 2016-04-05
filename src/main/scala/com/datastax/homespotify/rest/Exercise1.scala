package com.datastax.homespotify.rest

import java.util

import com.datastax.driver.core._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._

import scala.collection.JavaConverters._

class Exercise1(implicit session: Session) extends HomespotifyStack with JacksonJsonSupport {

  protected implicit val jsonFormats: Formats = DefaultFormats
  lazy val checkResults:PreparedStatement = session.prepare("SELECT count(*) FROM performers_by_style")
  lazy val allStylesPs:PreparedStatement = session.prepare("SELECT distinct style FROM performers_by_style")
  lazy val performersByStylePs:PreparedStatement = session.prepare("SELECT performer FROM performers_by_style WHERE style = ?")

  type Str = java.lang.String
  type I = java.lang.Integer

  before() {
    contentType = formats("json")
  }

  get("/verify_results") {
    val row = session.execute(checkResults.bind()).one()
    val count: Long = row.getLong("count")
    Map("result" -> (count == 18187L))
  }

  get("/all_styles") {

    val rows = session.execute(allStylesPs.bind()).all().asScala.toList
    rows.map(r => r.getString("style"))
  }


  get("/performers_by_style/:style") {
    var performers: Vector[String] = Vector.empty
    val iterator: util.Iterator[Row] = session.execute(performersByStylePs.bind(params("style")).setFetchSize(100)).iterator()
    while (iterator.hasNext) {
      val performer = iterator.next().getString("performer")
      performers = performers :+ performer
    }

    performers
  }


}
