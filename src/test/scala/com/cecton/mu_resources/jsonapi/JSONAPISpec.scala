package com.cecton.mu_resources.jsonapi

import org.scalatest.{ FunSpec, GivenWhenThen, BeforeAndAfterAll }
import org.scalatest.Matchers._
import scala.util.Try

class JSONAPISpec extends FunSpec with GivenWhenThen with BeforeAndAfterAll {
  describe("Rendering") {
    it("renders link objects") {
      val js = render(Link("http://example.com/articles/1/comments", Map("count" -> 10)))
      js should equal ("""{"href":"http://example.com/articles/1/comments","meta":{"count":10}}""")
    }

    it("renders links URL") {
      val js = render(Links(self = Some(Left("http://example.com/posts"))))
      js should equal ("""{"self":"http://example.com/posts"}""")
    }

    it("renders links link object") {
      val js = render(Links(self = Some(Right(Link("http://example.com/articles/1/comments", Map("count" -> 10))))))
      js should equal ("""{"self":{"href":"http://example.com/articles/1/comments","meta":{"count":10}}}""")
    }

    it("renders links null") {
      val js = render(Links(self = Some(null)))
      js should equal ("""{"self":null}""")
    }

    it("renders a data response") {
      val js = render(DataResponse(
        data=Left(Resource("something", "1", relationships=Some(Map(
          "author" -> new RelationshipToOne(data=ResourceIdentifier("people", "9")))))),
        included=Some(List(Resource("people", "9")))))
      js should equal ("""{"data":{"type":"something","id":"1","relationships":{"author":{"data":{"type":"people","id":"9"}}}},"included":[{"type":"people","id":"9"}]}""")
    }
  }

  describe("Validation") {
    it("validates") {
      val response = DataResponse(
        data=Left(Resource("something", "1", relationships=Some(Map(
          "attr" -> new RelationshipToOne(data=ResourceIdentifier("people", "9")))))),
        included=Some(List(Resource("people", "9"))))
      Try(response.validate).isSuccess should equal (true)
    }

    it("ensures there are no extra objects included") {
      val response = DataResponse(
        data=Left(Resource("something", "1", relationships=Some(Map(
          "attr" -> new RelationshipToOne(data=ResourceIdentifier("people", "8")))))),
        included=Some(List(Resource("people", "9"))))
      Try(response.validate).isFailure should equal (true)
    }
  }
}
