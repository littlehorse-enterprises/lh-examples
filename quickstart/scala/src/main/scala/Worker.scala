import io.littlehorse.sdk.worker.LHTaskMethod
import org.slf4j.LoggerFactory

object Worker {
  val logger = LoggerFactory.getLogger(getClass)
  
  @LHTaskMethod("greet")
  def greet(name: String): String = {
    logger.info(s"Executing task greet with name: $name")
    s"Hello, $name!"
  }
}
