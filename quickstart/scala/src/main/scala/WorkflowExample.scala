import io.littlehorse.sdk.common.proto.VariableType
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl

object WorkflowExample {
  def spec(): WorkflowImpl = {
    new WorkflowImpl(
      "example-basic",
      wf => {
        val theName = wf.addVariable("input-name", VariableType.STR).searchable()
        wf.execute("greet", theName)
      }
    )
  }
}
