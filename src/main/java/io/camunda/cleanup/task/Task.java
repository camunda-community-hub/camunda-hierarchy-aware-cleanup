package io.camunda.cleanup.task;

public interface Task {
  void run(TaskContext context);
}
