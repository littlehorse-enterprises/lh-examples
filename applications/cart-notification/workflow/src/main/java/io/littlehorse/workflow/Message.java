package io.littlehorse.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Message {
  public String message;
  public String wfRunId;
}
