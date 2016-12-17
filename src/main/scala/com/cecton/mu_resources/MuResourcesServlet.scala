package com.cecton.mu_resources

import org.scalatra._

class MuResourcesServlet extends MuResourcesStack {

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
      </body>
    </html>
  }

}
