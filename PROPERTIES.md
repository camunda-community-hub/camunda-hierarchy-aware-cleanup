# Camunda hierarchy aware cleanup
A program to perform process instance data cleanup in hierarchies in Camunda 8.
## Table of Contents
* [**camunda.cleanup** - `io.camunda.cleanup.AppProperties`](#camunda.cleanup)
* [**camunda.cleanup.audit** - `io.camunda.cleanup.AppProperties$Audit`](#camunda.cleanup.audit)

### camunda.cleanup
**Class:** `io.camunda.cleanup.AppProperties`

|Key|Description|Default value|
|---|-----------|-------------|
| kill-orphans|  Whether orphans are also detected and deleted.| true|  
| retention-buffer|  The buffer that is added to the retention policy.| P1D|  
| retention-policy|  The retention policy to be applied.| P30D|  
### camunda.cleanup.audit
**Class:** `io.camunda.cleanup.AppProperties$Audit`

|Key|Description|Default value|
|---|-----------|-------------|
| type|  the type of auditing performed| logging|  


This is a generated file, generated at: **2025-12-05T09:59:50.108303**

