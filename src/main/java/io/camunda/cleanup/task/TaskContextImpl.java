package io.camunda.cleanup.task;

import io.camunda.cleanup.CamundaClientFacade;
import io.camunda.cleanup.audit.ProcessInstanceDeletionAudit;
import java.util.concurrent.Executor;

public class TaskContextImpl implements TaskContext {
  private final Executor executor;
  private final CamundaClientFacade camundaClient;
  private final ProcessInstanceDeletionAudit deletionAudit;

  public TaskContextImpl(
      Executor executor,
      CamundaClientFacade camundaClient,
      ProcessInstanceDeletionAudit deletionAudit) {
    this.executor = executor;
    this.camundaClient = camundaClient;
    this.deletionAudit = deletionAudit;
  }

  @Override
  public void submit(Task task) {
    executor.execute(() -> task.run(this));
  }

  @Override
  public CamundaClientFacade camundaClient() {
    return camundaClient;
  }

  @Override
  public ProcessInstanceDeletionAudit deletionAudit() {
    return deletionAudit;
  }
}
