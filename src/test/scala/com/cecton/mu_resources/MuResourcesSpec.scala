package com.cecton.mu_resources

import org.scalatest.FunSuiteLike
import org.scalatra.test.scalatest._

// For more on Specs2, see http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html
class MuResourcesSpec extends ScalatraSuite with FunSuiteLike {
  addServlet(classOf[MuResources], "/*")

  test("GET / on MuResources") {
    get("/") {
      status should equal(200)
    }
  }
}
