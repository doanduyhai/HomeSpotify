import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet}
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object JettyLauncher { // this is my entry object as specified in sbt project definition
def main(args: Array[String]) {
  val port = if(System.getenv("PORT") != null) System.getenv("PORT").toInt else 9000

  val server = new Server(port)
  val context = new WebAppContext()
  context setContextPath "/"
  val resourceBase = getClass.getClassLoader.getResource("webapp").toExternalForm
  context.setResourceBase(resourceBase)
  context.addEventListener(new ScalatraListener)
  context.addServlet(classOf[DefaultServlet], "/*")

  server.setHandler(context)

  server.start
  server.join
}
}