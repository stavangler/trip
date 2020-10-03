import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.title
import kotlinx.html.div

fun HTML.index() {
  head {
    title("Hello from Ktor!")
  }
  body {
    div {
      +"Hello from Ktor"
    }
  }
}

fun main() {
  embeddedServer(Netty, port = 8080) {
    routing {
      get("/") {
        call.respondHtml(HttpStatusCode.OK, HTML::index)
      }
    }
  }.start(wait = true)
}
