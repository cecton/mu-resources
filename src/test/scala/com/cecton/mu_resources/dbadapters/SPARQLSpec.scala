package com.cecton.mu_resources.dbadapters

import dispatch._
import org.scalatest.{ FunSpec, GivenWhenThen, BeforeAndAfterAll }
import org.scalatest.AsyncFlatSpec
import org.scalatest.Matchers._
import scala.util.Try

import com.cecton.mu_resources.resource.Resource

class SPARQLSpec extends AsyncFlatSpec {

  val server = {

    import unfiltered.netty
    import unfiltered.response._
    import unfiltered.request._

    object Query extends Params.Extract("query", Params.first)

    netty.Http.anylocal.handler(netty.async.Planify {

      case req @ Path("/") & Params(Query(query)) =>
        req.respond(PlainTextContent ~> ResponseString(
          """{
          |  "head": { "vars": [ "s" , "p" , "o" ]
          |  } ,
          |  "results": {
          |    "bindings": [
          |      {
          |        "s": { "type": "uri" , "value": "http://example.org/book/book6" } ,
          |        "p": { "type": "uri" , "value": "http://example.org/book/title" } ,
          |        "o": { "type": "literal" , "value": "Harry Potter and the Half-Blood Prince" }
          |      } ,
          |      {
          |        "s": { "type": "uri" , "value": "http://example.org/book/book5" } ,
          |        "p": { "type": "uri" , "value": "http://example.org/book/title" } ,
          |        "o": { "type": "literal" , "value": "Harry Potter and the Order of the Phoenix" }
          |      } ,
          |      {
          |        "s": { "type": "uri" , "value": "http://example.org/book/book4" } ,
          |        "p": { "type": "uri" , "value": "http://example.org/book/title" } ,
          |        "o": { "type": "literal" , "value": "Harry Potter and the Goblet of Fire" }
          |      } ,
          |      {
          |        "s": { "type": "uri" , "value": "http://example.org/book/book3" } ,
          |        "p": { "type": "uri" , "value": "http://example.org/book/title" } ,
          |        "o": { "type": "literal" , "value": "Harry Potter and the Prisoner Of Azkaban" }
          |      } ,
          |      {
          |        "s": { "type": "uri" , "value": "http://example.org/book/book2" } ,
          |        "p": { "type": "uri" , "value": "http://example.org/book/title" } ,
          |        "o": { "type": "literal" , "value": "Harry Potter and the Chamber of Secrets" }
          |      } ,
          |      {
          |        "s": { "type": "uri" , "value": "http://example.org/book/book1" } ,
          |        "p": { "type": "uri" , "value": "http://example.org/book/title" } ,
          |        "o": { "type": "literal" , "value": "Harry Potter and the Philosopher's Stone" }
          |      }
          |    ]
          |  }
          |}""".stripMargin))

    }).start()

  }

  lazy val localhost: Req = host("127.0.0.1", server.port)

  behavior of "ResourceAdapter"

  val adapter = SPARQL(localhost)

  case class Book(val title: Option[String]) extends Resource
  case object ResourceBook extends adapter.ResourceAdapter {
    def toResource = { implicit rows =>
      Book(getString("http://example.org/book/title"))
    }
  }

  it should "list existing resources" in {
    val res = ResourceBook.list
    res map { r => assert(r.size == 6) }
  }

}
