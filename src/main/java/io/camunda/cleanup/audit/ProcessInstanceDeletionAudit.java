package io.camunda.cleanup.audit;

import java.time.OffsetDateTime;

public interface ProcessInstanceDeletionAudit {
  void pushPending(DeletedProcessInstance deletedProcessInstance);

  void pushSuccess(DeletedProcessInstance deletedProcessInstance);

  record DeletedProcessInstance(String processInstanceKey, OffsetDateTime endDate, OffsetDateTime deletionDate) {}
}
