package io.camunda.cleanup;

import io.camunda.cleanup.audit.LoggingProcessInstanceDeletionAudit;
import io.camunda.cleanup.audit.ProcessInstanceDeletionAudit;
import io.camunda.client.CamundaClient;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
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
  public ProcessInstanceCleanerConfiguration orphanKillerConfiguration(
      final CamundaClient camundaClient,
      final Executor orphanKillerExecutor,
      final ProcessInstanceDeletionAudit processInstanceDeletionAudit) {
    return new ProcessInstanceCleanerConfiguration(
        camundaClient,
        orphanKillerExecutor,
        properties.retentionPolicy().plus(properties.retentionBuffer()),
        properties.killOrphans(),
        processInstanceDeletionAudit);
  }

  @Bean
  public Executor orphanKillerExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }

  @Bean
  public ProcessInstanceDeletionAudit processInstanceDeletionAudit() {
    return switch (properties.audit().type()) {
      case logging -> new LoggingProcessInstanceDeletionAudit();
    };
  }
}
