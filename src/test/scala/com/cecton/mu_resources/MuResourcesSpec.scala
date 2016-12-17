package com.cecton.mu_resources

import org.scalatra.test.specs2._

// For more on Specs2, see http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html
class MuResourcesSpec extends ScalatraSpec { def is =
  "GET / on MuResources"                     ^
    "should return status 200"                  ! root200^
                                                end

  addServlet(classOf[MuResources], "/*")

  def root200 = get("/") {
    status must_== 200
  }
}
