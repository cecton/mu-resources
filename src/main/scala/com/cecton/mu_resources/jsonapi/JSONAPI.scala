package com.cecton.mu_resources.jsonapi

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

sealed trait Response {
  def withLinks(x: JsonObject): Response

  def withJsonapi(x: JsonObject): Response
}

case class Error(val id: Json = Json.Null)

case class Data(val id: Json, val `type`: String, val attributes: JsonObject)

object Response {

  case class Data(
      val data: Json,
      val errors: Option[Seq[Error]] = None,
      val meta: Option[JsonObject] = None,
      val jsonapi: Option[JsonObject] = None,
      val links: Option[JsonObject] = None)
      extends Response {

    def withLinks(x: JsonObject) = this.copy(links=Some(x))

    def withJsonapi(x: JsonObject) = this.copy(jsonapi=Some(x))
  }

  case class Errors(
      val errors: Seq[Error],
      val meta: Option[JsonObject] = None,
      val jsonapi: Option[JsonObject] = None,
      val links: Option[JsonObject] = None)
      extends Response {

    def withLinks(x: JsonObject) = this.copy(links=Some(x))

    def withJsonapi(x: JsonObject) = this.copy(jsonapi=Some(x))
  }

}

object `package` {

  private def removesNull[A](e: ObjectEncoder[A]) = removesNullExcept(e)

  private def removesNullExcept[A](e: ObjectEncoder[A], except: String*) =
    e.mapJsonObject(x => x.filter(
      y => except.contains(y._1) || !y._2.isNull))

  implicit val idEncoder: Encoder[Any] = new Encoder[Any] {
    final def apply(value: Any): Json = value match {
      case value: Int => value.asJson
      case value: String => value.asJson
    }
  }

  implicit val errorEncoder: Encoder[Error] = removesNull(deriveEncoder[Error])

  implicit val responseDataEncoder: Encoder[Response.Data] =
    removesNullExcept(deriveEncoder[Response.Data], "data")

  implicit val responseErrorsEncoder: Encoder[Response.Errors] =
    removesNull(deriveEncoder[Response.Errors])

}
