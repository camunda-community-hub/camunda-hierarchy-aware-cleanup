package io.camunda.cleanup.task;

import io.camunda.cleanup.CamundaClientFacade;
import io.camunda.cleanup.audit.ProcessInstanceDeletionAudit;

public interface TaskContext {
  void submit(Task task);

  CamundaClientFacade camundaClient();

  ProcessInstanceDeletionAudit deletionAudit();
}
