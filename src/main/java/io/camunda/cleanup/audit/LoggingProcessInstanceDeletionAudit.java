package io.camunda.cleanup.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingProcessInstanceDeletionAudit implements ProcessInstanceDeletionAudit {
  private static final Logger LOG = LoggerFactory.getLogger(LoggingProcessInstanceDeletionAudit.class);

  @Override
  public void pushPending(DeletedProcessInstance deletedProcessInstance) {
    LOG.info("Deleting process instance: {}", deletedProcessInstance);
  }

  @Override
  public void pushSuccess(DeletedProcessInstance deletedProcessInstance) {
    LOG.info("Deleted process instance: {}", deletedProcessInstance);
  }
}
