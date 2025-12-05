package io.camunda.cleanup.task;

import io.camunda.cleanup.audit.ProcessInstanceDeletionAudit;
import io.camunda.cleanup.audit.ProcessInstanceDeletionAudit.DeletedProcessInstance;
import io.camunda.client.api.search.response.ProcessInstance;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record DeleteProcessInstanceTask(ProcessInstance processInstance) implements Task {
  private static final Logger LOG = LoggerFactory.getLogger(DeleteProcessInstanceTask.class);

  @Override
  public void run(TaskContext context) {
    ProcessInstanceDeletionAudit deletionAudit = context.deletionAudit();
    String processInstanceKey = String.valueOf(processInstance.getProcessInstanceKey());
    try {
      DeletedProcessInstance deletedProcessInstance =
          new DeletedProcessInstance(
              processInstanceKey, processInstance.getEndDate(), OffsetDateTime.now());
      deletionAudit.pushPending(deletedProcessInstance);
      context.camundaClient().deleteProcessInstance(processInstanceKey);
      deletionAudit.pushSuccess(deletedProcessInstance);
      LOG.debug("Deleted process instance {}", processInstanceKey);
    } catch (final Exception e) {
      LOG.warn("Could not delete process instance {}", processInstanceKey, e);
    }
  }
}
