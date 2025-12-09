# camunda-hierarchy-aware-cleanup-spring-boot-starter
A program to perform process instance data cleanup in hierarchies in Camunda 8.
## Table of Contents
* [**camunda.cleanup** - `io.camunda.cleanup.AppProperties`](#camunda.cleanup)
* [**camunda.cleanup.audit** - `io.camunda.cleanup.AppProperties$Audit`](#camunda.cleanup.audit)

### camunda.cleanup
**Class:** `io.camunda.cleanup.AppProperties`

|Key|Description|Default value|Environment variable |
|---|-----------|-------------|----------------------|
| kill-orphans|  Whether orphans are also detected and deleted.| true|  `CAMUNDA_CLEANUP_KILLORPHANS`|
| retention-buffer|  The buffer that is added to the retention policy.| P1D|  `CAMUNDA_CLEANUP_RETENTIONBUFFER`|
| retention-policy|  The retention policy to be applied.| P30D|  `CAMUNDA_CLEANUP_RETENTIONPOLICY`|
### camunda.cleanup.audit
**Class:** `io.camunda.cleanup.AppProperties$Audit`

|Key|Description|Default value|Environment variable |
|---|-----------|-------------|----------------------|
| type|  the type of auditing performed| logging|  `CAMUNDA_CLEANUP_AUDIT_TYPE`|



