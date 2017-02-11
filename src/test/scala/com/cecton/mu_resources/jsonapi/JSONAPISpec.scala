package com.cecton.mu_resources.jsonapi

import io.circe._, io.circe.generic.auto._, io.circe.syntax._
import org.scalatest.{ FunSpec, GivenWhenThen, BeforeAndAfterAll }
import org.scalatest.Matchers._
import scala.util.Try

class JSONAPISpec extends FunSpec with GivenWhenThen with BeforeAndAfterAll {

  import com.cecton.mu_resources.resource._

  case class A(val x: String) extends Resource

  describe("Response") {

    it("renders MissingData") {
      val response = Response.Data(Json.Null)
      response.asJson.noSpaces should be ("""{"data":null}""")
    }

    it("renders SingleData") {
      val resource = A("foo")
      val response = Response.Data(Data(1.asJson, "a", resource.asJsonObject).asJson)
      response.asJson.noSpaces should be ("""{"data":{"id":1,"type":"a","attributes":{"x":"foo"}}}""")
    }

    it("renders MultiData") {
      val resources = List((1, "a", A("foo")), (2, "a", A("bar")))
      val response = Response.Data(resources.map(x => Data(x._1.asJson, x._2, x._3.asJsonObject)).asJson)
      response.asJson.noSpaces should be ("""{"data":[{"id":1,"type":"a","attributes":{"x":"foo"}},{"id":2,"type":"a","attributes":{"x":"bar"}}]}""")
    }

    it("renders links") {
      val response1 = Response.Data(Json.Null)
      val response2 = response1.withLinks(Map("foo" -> "bar").asJsonObject)
      response2.asJson.noSpaces should equal ("""{"data":null,"links":{"foo":"bar"}}""")
      response1.asJson.noSpaces should not equal ("""{"data":null",links":{"foo":"bar"}}""")
    }

    it("renders empty errors") {
      val response = Response.Errors(Seq())
      response.asJson.noSpaces should be ("""{"errors":[]}""")
    }

    it("renders errors") {
      val response = Response.Errors(Seq(Error()))
      response.asJson.noSpaces should be ("""{"errors":[{}]}""")
    }

  }

}
