# Camunda Hierarchy aware cleanup

This repository contains a project where a program is created that has the purpose to implement the upcoming feature of _hierarchy aware process instance cleanup_ for Camunda 8.

## Camunda builds this feature, why do I need this?

The feature implemented by Camunda will only cover process instances that have been created in 8.9. In order to clean up process instance data created before version 8.9, this tool has been created.

## How does it work?

This tool uses the Camunda Orchestration cluster API to query for root process instances that have been completed before the configured retention policy.

Then, it traverses the tree of these process instances and deletes every process instance from that tree.

As this is done asynchronous and stateless, it could happen that the traversal is interrupted while the root process instance has already been deleted.

To meet this issue accordingly, there is another routine that periodically scans for "orphan" process instances that have been completed before the configured retention policy, means process instances for which the parent process instance does not exist anymore.

Here, the side effect could happen that other "orphans" that had appeared in the past due to the manual deletion of their parent process instances.

## How do I configure it?

The application comes with a set of properties that can be reviewed [here](./PROPERTIES.md).

Apart from that, the connection to Camunda is configured as outlined [here](https://docs.camunda.io/docs/apis-tools/camunda-spring-boot-starter/configuration/).
