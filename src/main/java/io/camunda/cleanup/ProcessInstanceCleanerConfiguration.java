package io.camunda.cleanup;

import io.camunda.cleanup.audit.ProcessInstanceDeletionAudit;
import io.camunda.client.CamundaClient;
import java.time.Duration;
import java.util.concurrent.Executor;

public record ProcessInstanceCleanerConfiguration(
    CamundaClient camundaClient,
    Executor executor,
    Duration relaxedRetentionPolicy,
    boolean killOrphans,
    ProcessInstanceDeletionAudit deletionAudit) {}
