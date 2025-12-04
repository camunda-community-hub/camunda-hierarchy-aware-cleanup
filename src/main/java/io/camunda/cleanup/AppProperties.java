package io.camunda.cleanup;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties("camunda.cleanup")
public record AppProperties(@DefaultValue("P30D") Duration retentionPolicy,
                            @DefaultValue("P1D") Duration retentionBuffer, @DefaultValue("true") boolean killOrphans,
                            Audit audit) {
  public record Audit(@DefaultValue("logging") Type type) {
    public enum Type {logging}
  }
}
