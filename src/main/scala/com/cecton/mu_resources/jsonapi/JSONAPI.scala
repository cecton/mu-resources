package com.cecton.mu_resources.jsonapi

import co.blocke.scalajack._
import co.blocke.scalajack.json.JsonKind
import scala.reflect.runtime.universe.TypeTag

case class LinkObject(val href: String, val meta: Map[String, Any])

case class Links(
  val self: Option[Either[String, LinkObject]] = None,
  val related: Option[Either[String, LinkObject]] = None,
  val first: Option[Either[String, LinkObject]] = None,
  val last: Option[Either[String, LinkObject]] = None,
  val prev: Option[Either[String, LinkObject]] = None,
  val next: Option[Either[String, LinkObject]] = None)

trait Response {
  val jsonapi: Option[Map[String, Any]] = None
  val links: Option[Links] = None
}

case class DataResponse(
  val data: Map[String, Any],
  val meta: Option[Map[String, Any]] = None,
  val included: Option[List[Any]] = None) extends Response

case class ErrorsResponse(
  val errors: List[Map[String, Any]],
  val meta: Option[Map[String, Any]] = None) extends Response

case class MetaResponse(
  val meta: Map[String, Any]) extends Response

object `package` {
  private val scalaJack = ScalaJack()
  private val visitorContext = VisitorContext().copy(
    customHandlers = Map(
      "scala.util.Either" -> CustomReadRender(
      {
          case (j: JsonKind, js: String) =>
            throw new UnsupportedOperationException("Parse Links from JSON")
      },
      {
          case (j: JsonKind, thing: Either[Any, Any]) => thing.merge match {
            case x: LinkObject => scalaJack.render(x)
            case x: String => scalaJack.render(x)
            case x =>
              throw new UnsupportedOperationException(s"Unknown type: ${x.getClass}")
          }
      })
    ))

  def render[T](something: T)(implicit tt: TypeTag[T]): String =
    scalaJack.render[T](something, visitorContext)
}
