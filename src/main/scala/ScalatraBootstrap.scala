import com.datastax.homespotify.conf.CassandraConfig._
import com.datastax.homespotify.rest._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new Exercise1, "/exercise1")
    context.mount(new Exercise2, "/exercise2")
    context.mount(new Exercise3, "/exercise3")
    context.mount(new Exercise4, "/exercise4")
    context.mount(new Exercise5, "/exercise5")

  }
}
