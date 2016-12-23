package com.cecton.mu_resources.jsonapi

import co.blocke.scalajack._
import co.blocke.scalajack.json.JsonKind
import scala.reflect.runtime.universe.TypeTag

trait Relationship

case class RelationshipToOne(
  val links: Option[Links] = None,
  val data: ResourceIdentifier,
  val meta: Option[Map[String, Any]] = None) extends Relationship

case class RelationshipToMany(
  val links: Option[Links] = None,
  val data: List[ResourceIdentifier],
  val meta: Option[Map[String, Any]] = None) extends Relationship

case class RelationshipLinks(
  val links: Links,
  val meta: Option[Map[String, Any]] = None) extends Relationship

case class RelationshipMeta(
  val links: Option[Links] = None,
  val meta: Map[String, Any]) extends Relationship

case class Resource(
  val `type`: String,
  val id: String,
  val attributes: Option[Map[String, Any]] = None,
  val relationships: Option[Map[String, Relationship]] = None,
  val links: Option[Links] = None,
  val meta: Option[Map[String, Any]] = None) {

  lazy val toIdentifier = ResourceIdentifier(`type`, id, meta)

}

case class NewResource(
  val `type`: String,
  val attributes: Option[Map[String, Any]] = None,
  val relationships: Option[Map[String, Relationship]] = None,
  val links: Option[Links] = None,
  val meta: Option[Map[String, Any]] = None)

case class ResourceIdentifier(
  val `type`: String,
  val id: String,
  val meta: Option[Map[String, Any]] = None)

case class Link(val href: String, val meta: Map[String, Any])

case class Links(
  val self: Option[Either[String, Link]] = None,
  val related: Option[Either[String, Link]] = None,
  val first: Option[Either[String, Link]] = None,
  val last: Option[Either[String, Link]] = None,
  val prev: Option[Either[String, Link]] = None,
  val next: Option[Either[String, Link]] = None)

trait Response {
  val jsonapi: Option[Map[String, Any]] = None
  val links: Option[Links] = None
}

case class DataResponse(
  val data: Either[Resource, List[Resource]],
  val meta: Option[Map[String, Any]] = None,
  val included: Option[List[Resource]] = None,
  override val jsonapi: Option[Map[String, Any]] = None,
  override val links: Option[Links] = None) extends Response {

    lazy val resources: List[Resource] = data match {
      case Left(resource: Resource) => List(resource)
      case Right(resources: List[Resource]) => resources
    }

    def validate = {
      val resourceIdentifiers: Seq[ResourceIdentifier] = resources
        .collect {
          case resource if resource.relationships.nonEmpty =>
            resource.relationships.get.values.collect {
              case relationship: RelationshipToOne => List(relationship.data)
              case relationship: RelationshipToMany => relationship.data
            }
            .flatten
        }
        .flatten
        .toSeq
      if (included.nonEmpty)
      {
        val notReferenced = included.get.filterNot {
          resource => resourceIdentifiers.contains(resource.toIdentifier)
        }
        if (notReferenced.nonEmpty)
          throw new RuntimeException(
            "The following included are not referenced in the data: "
            + notReferenced.mkString(", "))
      }
    }
}

case class ErrorsResponse(
  val errors: List[Map[String, Any]],
  val meta: Option[Map[String, Any]] = None,
  override val jsonapi: Option[Map[String, Any]] = None,
  override val links: Option[Links] = None) extends Response

case class MetaResponse(
  val meta: Map[String, Any],
  override val jsonapi: Option[Map[String, Any]] = None,
  override val links: Option[Links] = None) extends Response

object `package` {
  private val scalaJack = ScalaJack()
  private val visitorContext = VisitorContext().copy(
      //hintMap = Map("default" -> "type"),
      customHandlers = Map(
        "scala.util.Either" -> CustomReadRender(
        {
            case (j: JsonKind, js: String) =>
              throw new UnsupportedOperationException("Parse Links from JSON")
        },
        {
            case (j: JsonKind, thing: Either[Any, Any]) => thing.merge match {
              case x: Link => render(x)
              case x: Resource => render(x)
              case x: String => render(x)
              case x =>
                throw new UnsupportedOperationException(s"Unknown type: ${x.getClass}")
            }
        }),
        "com.cecton.mu_resources.jsonapi.Relationship" -> CustomReadRender(
        {
            case (j: JsonKind, js: String) =>
              throw new UnsupportedOperationException("Parse Links from JSON")
        },
        {
            case (j: JsonKind, thing: RelationshipToOne) =>
              throw new UnsupportedOperationException(s"Unknown type")
        })
      ))

  def render[T](something: T)(implicit tt: TypeTag[T]): String =
    scalaJack.render[T](something, visitorContext)
}
