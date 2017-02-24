package com.cecton.mu_resources.dbadapters

import dispatch._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

import com.cecton.mu_resources.resource._

trait SPARQL {
  val selectReq: Req

  case class JSONResponse(val head: JSONResultHead, val results: JSONResults)
  case class JSONResultHead(val vars: Seq[String])
  case class JSONResults(val bindings: Seq[JSONRow])
  case class JSONRow(val s: JSONValue, val p: JSONValue, val o: JSONValue)
  case class JSONValue(val `type`: String, val value: Json)

  case class ResourceContext(val id: String, val items: Map[String, Seq[JSONRow]]) {
    val me = items(id)
  }
  case class RelationshipsContext(val resources: Map[String, Resource], val bindings: Seq[JSONRow])

  trait ResourceAdapter {

    def toResource: ResourceContext => Resource

    def toRelationships: RelationshipsContext => Seq[Seq[Relationship]]

    def getString(p: String)(implicit context: ResourceContext): Option[String] =
      context.me
        .collect {
          case x if x.p.value.asString == Some(p) => x.o.value.asString
        }
        .flatten
        .headOption

    def getOne2One(p: String)(implicit context: RelationshipsContext): Seq[One2One] = {
      context
        .bindings
        .filter(_.p.value.asString == Some(p))
        .map(x => (x.s.value.asString, x.o.value.asString))
        .collect {
          case (Some(x), Some(y)) =>
            One2One(context.resources(x), context.resources(y))
        }
    }

    def list()(implicit ctx: ExecutionContext): Future[(Iterable[Resource], Relationships)] = {
      val prom = Promise[(Iterable[Resource], Relationships)]()
      val request = selectReq
        .addQueryParameter("query", "select * {?s a <something> ; ?p ?o}")
      dispatch.Http(request OK as.String) onComplete {
        case Success(content) =>
          val bindings = decode[JSONResponse](content).right.get.results.bindings
          val items = bindings.groupBy(_.s.value.asString.get)
          val resources: Map[String, Resource] = items.map { x =>
            val context = ResourceContext(x._1, items)
            (x._1, toResource(context))
          }
          val context = RelationshipsContext(resources, bindings)
          val relationships = Relationships(toRelationships(context).flatten:_*)
          prom.complete(Try((resources.values, relationships)))
        case Failure(exception) =>
          println(exception)
          prom.failure(exception)
      }
      prom.future
    }

  }

}
