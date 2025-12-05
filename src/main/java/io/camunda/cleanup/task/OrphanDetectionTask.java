package io.camunda.cleanup.task;

import io.camunda.client.api.search.response.ProcessInstance;

public record OrphanDetectionTask(ProcessInstance processInstance) implements Task {

  @Override
  public void run(TaskContext context) {
    if (!context
        .camundaClient()
        .processInstanceExists(String.valueOf(processInstance.getParentProcessInstanceKey()))) {
      context.submit(
          new OrphanProcessInstanceSearchTask(
              processInstance.getParentProcessInstanceKey(), p -> p.limit(100)));
    }
  }
}
