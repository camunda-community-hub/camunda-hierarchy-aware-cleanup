package io.camunda.cleanup.task;

import io.camunda.client.api.search.response.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record CompletedAndOutDatedRootProcessInstanceHandlerTask(ProcessInstance processInstance)
    implements Task {
  private static final Logger LOG =
      LoggerFactory.getLogger(CompletedAndOutDatedRootProcessInstanceHandlerTask.class);

  @Override
  public void run(TaskContext context) {
    LOG.debug("Handling root process instance {}", processInstance.getProcessInstanceKey());
    context.submit(
        new OrphanProcessInstanceSearchTask(
            processInstance.getProcessInstanceKey(), p -> p.limit(100)));
    context.submit(new DeleteProcessInstanceTask(processInstance));
  }
}
