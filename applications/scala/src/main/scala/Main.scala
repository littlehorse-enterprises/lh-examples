import io.littlehorse.sdk.worker.LHTaskWorker
import io.littlehorse.sdk.common.config.LHConfig
import java.nio.file.Paths
import java.util.Properties
import java.io.FileInputStream
import java.util.concurrent.CountDownLatch


@main def main(): Unit =
  val demo = WorkflowExample.spec()
  val config = new LHConfig(getConfigProps())
  val worker = new LHTaskWorker(Worker, "greet", config)
  val workflow = WorkflowExample.spec()

  worker.registerTaskDef()
  workflow.registerWfSpec(config.getBlockingStub())

  worker.start()
  val latch = new CountDownLatch(1)
  latch.await()

def getConfigProps(): Properties = {
  val props = new Properties()
  val configPath = Paths.get(System.getProperty("user.home"), ".config/littlehorse.config").toFile()
  if (configPath.exists()) {
    props.load(new FileInputStream(configPath))
  }
  props
}
