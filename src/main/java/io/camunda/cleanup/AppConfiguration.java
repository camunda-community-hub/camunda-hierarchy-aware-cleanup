package io.camunda.cleanup;

import io.camunda.cleanup.audit.LoggingProcessInstanceDeletionAudit;
import io.camunda.cleanup.audit.ProcessInstanceDeletionAudit;
import io.camunda.cleanup.task.TaskContext;
import io.camunda.cleanup.task.TaskContextImpl;
import io.camunda.client.CamundaClient;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class AppConfiguration {
  private final AppProperties properties;

  public AppConfiguration(final AppProperties properties) {
    this.properties = properties;
  }

  @Bean
  public ProcessInstanceCleanerConfiguration orphanKillerConfiguration(TaskContext taskContext) {
    return new ProcessInstanceCleanerConfiguration(
        properties.retentionPolicy().plus(properties.retentionBuffer()),
        properties.killOrphans(),
        taskContext);
  }

  @Bean
  public TaskContext taskContext(
      Executor orphanKillerExecutor,
      CamundaClientFacade camundaClientFacade,
      ProcessInstanceDeletionAudit processInstanceDeletionAudit) {
    return new TaskContextImpl(
        orphanKillerExecutor, camundaClientFacade, processInstanceDeletionAudit);
  }

  @Bean
  public CamundaClientFacade camundaClientFacade(
      CamundaClient camundaClient, Executor orphanKillerExecutor) {
    return new CamundaClientFacadeImpl(
        camundaClient, HttpClients.createSystem(), orphanKillerExecutor);
  }

  @Bean
  public Executor orphanKillerExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }

  @Bean
  public ProcessInstanceDeletionAudit processInstanceDeletionAudit() {
    if (properties.audit() == null) {
      return new LoggingProcessInstanceDeletionAudit();
    }
    return switch (properties.audit().type()) {
      case logging -> new LoggingProcessInstanceDeletionAudit();
    };
  }
}
