package com.cecton.mu_resources.resource

import io.circe.generic.auto._
import io.circe.syntax._
import org.scalatest.{ FunSpec, GivenWhenThen, BeforeAndAfterAll }
import org.scalatest.Matchers._

class ResourceSpec extends FunSpec with GivenWhenThen with BeforeAndAfterAll {

  case class A() extends Resource
  case class B() extends Resource
  case class C() extends Resource
  case class D() extends Resource

  describe("Resource") {
    it("allows creating case classes") {
      A() shouldBe a [A]
      A().asJson.noSpaces should equal ("{}")
    }
  }

  describe("Relationships") {
    it("generates all relations automatically") {
      val a = A()
      val b = B()
      val c = C()
      val d = D()
      val relationships = Relationships(
        One2One(a, b), Many2One(b, c), One2Many(c, d), Many2Many(d, a))
      relationships.map(a).toSet should be (Set(One2One(a, b), Many2Many(a, d)))
      relationships.map(b).toSet should be (Set(One2One(b, a), Many2One(b, c)))
      relationships.map(c).toSet should be (Set(One2Many(c, b), One2Many(c, d)))
      relationships.map(d).toSet should be (
        Set(Many2Many(d, a), Many2One(d, c), Many2Many(d, a)))
    }
  }

}
