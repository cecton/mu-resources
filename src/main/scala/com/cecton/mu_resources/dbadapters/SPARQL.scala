package com.cecton.mu_resources.dbadapters

import dispatch._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

import com.cecton.mu_resources.resource.Resource

case class SPARQL(val selectReq: Req) {

  case class JSONResponse(val head: JSONResultHead, val results: JSONResults)
  case class JSONResultHead(val vars: Seq[String])
  case class JSONResults(val bindings: Seq[JSONRow])
  case class JSONRow(val s: JSONValue, val p: JSONValue, val o: JSONValue)
  case class JSONValue(val `type`: String, val value: Json)

  trait ResourceAdapter {

    def toResource(x: Seq[JSONRow]): Resource

    def list()(implicit ctx: ExecutionContext): Future[Iterable[Resource]] = {
      val prom = Promise[Iterable[Resource]]()
      val request = selectReq
        .addQueryParameter("query", "select * {?s a <something> ; ?p ?o}")
      dispatch.Http(request OK as.String) onComplete {
        case Success(content) =>
          val bindings = decode[JSONResponse](content).right.get.results.bindings
          prom.complete(Try(bindings.groupBy(_.s.value.asString.get).values.map(toResource)))
        case Failure(exception) =>
          println(exception)
          prom.failure(exception)
      }
      prom.future
    }

  }

}
