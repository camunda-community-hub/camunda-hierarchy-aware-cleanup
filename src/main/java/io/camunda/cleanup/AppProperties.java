package io.camunda.cleanup;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Cleanup App properties
 *
 * @param retentionPolicy The retention policy to be applied.
 * @param retentionBuffer The buffer that is added to the retention policy.
 * @param killOrphans Whether orphans are also detected and deleted.
 * @param audit Auditing settings
 */
@ConfigurationProperties("camunda.cleanup")
public record AppProperties(
    @DefaultValue("P30D") Duration retentionPolicy,
    @DefaultValue("P1D") Duration retentionBuffer,
    @DefaultValue("true") boolean killOrphans,
    Audit audit) {
  /**
   * Auditing properties
   *
   * @param type the type of auditing performed
   */
  public record Audit(@DefaultValue("logging") Type type) {
    public enum Type {
      logging
    }
  }
}
