package com.cecton.mu_resources.jsonapi

import io.circe._
import io.circe.syntax._

sealed trait Response {
  var jsonapi: JsonObject = JsonObject.empty
  var links: JsonObject = JsonObject.empty

  def renderMeta: Map[String, Json] = Seq(
      if( jsonapi.nonEmpty ) Seq("jsonapi" -> jsonapi.asJson) else Seq(),
      if( links.nonEmpty ) Seq("links" -> links.asJson) else Seq()
    ).flatten.toMap

  def renderContent: Map[String, Json]

  def asJson: Json = (renderMeta ++ renderContent).asJson

  def withLinks(x: JsonObject): Response

}

object Response {

  sealed trait Resource extends Response {
    val data: Json

    def renderContent = Map("data" -> data)

    def withLinks(x: JsonObject): Response
  }

  case class MissingResource() extends Resource {
    val data = Json.Null

    def withLinks(x: JsonObject) = {
      val response = this.copy()
      response.links = x
      response
    }

  }

  case class SingleResource(
      val id: Any, val `type`: String, val resource: JsonObject)
      extends Resource {

    val data = Map[String, Json](
      "id" -> id.asJson,
      "type" -> `type`.asJson,
      "attributes" -> resource.asJson).asJson

    def withLinks(x: JsonObject) = {
      val response = this.copy()
      response.links = x
      response
    }

  }

  case class MultiResource(
      val resources: Seq[(Int, String, JsonObject)])
      extends Resource {

    private def mkData(x: (Int, String, JsonObject)) = Map[String, Json](
      "id" -> x._1.asJson,
      "type" -> x._2.asJson,
      "attributes" -> x._3.asJson)

    val data = resources.map(mkData(_).asJson).asJson

    def withLinks(x: JsonObject) = {
      val response = this.copy()
      response.links = x
      response
    }

  }

}

object `package` {

  implicit val idEncoder: Encoder[Any] = new Encoder[Any] {
    final def apply(value: Any): Json = value match {
      case value: Int => value.asJson
      case value: String => value.asJson
    }
  }

}
