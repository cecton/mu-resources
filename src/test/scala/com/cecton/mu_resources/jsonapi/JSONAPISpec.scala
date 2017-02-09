package com.cecton.mu_resources.jsonapi

import io.circe._, io.circe.generic.auto._, io.circe.syntax._
import org.scalatest.{ FunSpec, GivenWhenThen, BeforeAndAfterAll }
import org.scalatest.Matchers._
import scala.util.Try

class JSONAPISpec extends FunSpec with GivenWhenThen with BeforeAndAfterAll {

  import com.cecton.mu_resources.resource._
  import Response.{MissingResource, SingleResource, MultiResource}

  case class A(val x: String) extends Resource

  describe("Response") {

    it("renders MissingResourceResponse") {
      val response = MissingResource()
      response.asJson.noSpaces should be ("""{"data":null}""")
    }

    it("renders SingleResourceResponse") {
      val resource = A("foo")
      val response = SingleResource(1, "a", resource.asJsonObject)
      response.asJson.noSpaces should be ("""{"data":{"id":1,"type":"a","attributes":{"x":"foo"}}}""")
    }

    it("renders MultiResourceResponse") {
      val resources = List((1, "a", A("foo")), (2, "a", A("bar")))
      val response = MultiResource(resources.map(x => (x._1, x._2, x._3.asJsonObject)))
      response.asJson.noSpaces should be ("""{"data":[{"id":1,"type":"a","attributes":{"x":"foo"}},{"id":2,"type":"a","attributes":{"x":"bar"}}]}""")
    }

    it("renders links") {
      val response1 = MissingResource()
      val response2 = response1.withLinks(Map("foo" -> "bar").asJsonObject)
      response2.asJson.noSpaces should equal ("""{"links":{"foo":"bar"},"data":null}""")
      response1.asJson.noSpaces should not equal ("""{"links":{"foo":"bar"},"data":null}""")
    }

  }

}
