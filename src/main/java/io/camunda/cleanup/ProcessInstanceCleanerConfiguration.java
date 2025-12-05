package io.camunda.cleanup;

import io.camunda.cleanup.task.TaskContext;
import java.time.Duration;

public record ProcessInstanceCleanerConfiguration(
    Duration relaxedRetentionPolicy, boolean killOrphans, TaskContext taskContext) {}
