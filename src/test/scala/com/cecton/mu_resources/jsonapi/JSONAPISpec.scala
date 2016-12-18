package com.cecton.mu_resources.jsonapi

import co.blocke.scalajack._
import org.scalatest.{ FunSpec, GivenWhenThen, BeforeAndAfterAll }
import org.scalatest.Matchers._
import scala.util.Try
import java.util.UUID
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class JSONAPISpec extends FunSpec with GivenWhenThen with BeforeAndAfterAll {
  describe("JSONAPI case classes tests") {
    it("renders link objects") {
      val js = render(LinkObject("http://example.com/articles/1/comments", Map("count" -> 10)))
      js should equal ("""{"href":"http://example.com/articles/1/comments","meta":{"count":10}}""")
    }

    it("renders links URL") {
      val js = render(Links(self = Some(Left("http://example.com/posts"))))
      js should equal ("""{"self":"http://example.com/posts"}""")
    }

    it("renders links link object") {
      val js2 = render(Links(self = Some(Right(LinkObject("http://example.com/articles/1/comments", Map("count" -> 10))))))
      js2 should equal ("""{"self":{"href":"http://example.com/articles/1/comments","meta":{"count":10}}}""")
    }

    it("renders links null") {
      val js2 = render(Links(self = Some(null)))
      js2 should equal ("""{"self":null}""")
    }
  }
}
