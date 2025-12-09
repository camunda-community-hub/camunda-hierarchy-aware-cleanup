package io.camunda.cleanup;

import io.camunda.cleanup.task.CompletedAndOutDatedRootProcessInstanceHandlerTask;
import io.camunda.cleanup.task.OrphanDetectionTask;
import io.camunda.cleanup.task.TaskContext;
import io.camunda.client.api.search.enums.ProcessInstanceState;
import io.camunda.client.api.search.filter.ProcessInstanceFilter;
import io.camunda.client.api.search.request.SearchRequestPage;
import io.camunda.client.api.search.response.ProcessInstance;
import io.camunda.client.api.search.response.SearchResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class ProcessInstanceCleaner {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessInstanceCleaner.class);
  private final Duration relaxedRetentionPolicy;
  private final boolean killOrphans;
  private final AtomicReference<String> parentsEndCursor = new AtomicReference<>(null);
  private final AtomicReference<String> orphansEndCursor = new AtomicReference<>(null);
  private final TaskContext taskContext;

  public ProcessInstanceCleaner(final ProcessInstanceCleanerConfiguration configuration) {
    relaxedRetentionPolicy = configuration.relaxedRetentionPolicy();
    killOrphans = configuration.killOrphans();
    taskContext = configuration.taskContext();
  }

  @Scheduled(fixedDelay = 10000L)
  public void clean() {
    cleanParents();
    if (killOrphans) {
      cleanOrphans();
    }
  }

  private void cleanParents() {
    LOG.debug("Killing parents process started");
    if (parentsEndCursor.get() != null) {
      parentsEndCursor.set(findParents(p -> p.after(parentsEndCursor.get()).limit(100)));
    } else {
      parentsEndCursor.set(findParents(p -> p.limit(100)));
    }
    LOG.debug("Killing parents process finished");
  }

  private void cleanOrphans() {
    LOG.debug("Killing orphan process started");
    if (orphansEndCursor.get() != null) {
      orphansEndCursor.set(findOrphans(p -> p.after(orphansEndCursor.get()).limit(100)));
    } else {
      orphansEndCursor.set(findOrphans(p -> p.limit(100)));
    }
    LOG.debug("Killing orphan process finished");
  }

  private String findParents(final Consumer<SearchRequestPage> page) {
    // search for process instances that have a parent, are completed or canceled and already older
    // than expected
    final SearchResponse<ProcessInstance> searchResponse =
        taskContext
            .camundaClient()
            .searchProcessInstance(this::completedAndOutdatedRootProcessInstance, page);
    if (searchResponse.items().isEmpty()) {
      return null;
    } else {
      searchResponse.items().forEach(this::handleCompletedAndOutdatedRootProcessInstance);
      return searchResponse.page().endCursor();
    }
  }

  private void completedAndOutdatedRootProcessInstance(
      final ProcessInstanceFilter processInstanceFilter) {
    completedAndOutdatedProcessInstance(processInstanceFilter, false);
  }

  private void completedAndOutdatedChildProcessInstance(
      final ProcessInstanceFilter processInstanceFilter) {
    completedAndOutdatedProcessInstance(processInstanceFilter, true);
  }

  private void completedAndOutdatedProcessInstance(
      final ProcessInstanceFilter processInstanceFilter, boolean hasParent) {
    processInstanceFilter
        .parentProcessInstanceKey(l -> l.exists(hasParent))
        .state(s -> s.in(ProcessInstanceState.COMPLETED, ProcessInstanceState.TERMINATED))
        .endDate(d -> d.lt(OffsetDateTime.now().minus(relaxedRetentionPolicy)));
  }

  private void handleCompletedAndOutdatedRootProcessInstance(
      final ProcessInstance processInstance) {
    taskContext.submit(new CompletedAndOutDatedRootProcessInstanceHandlerTask(processInstance));
  }

  private String findOrphans(final Consumer<SearchRequestPage> page) {
    // search for process instances that have a parent, are completed or canceled and already older
    // than expected
    final SearchResponse<ProcessInstance> searchResponse =
        taskContext
            .camundaClient()
            .searchProcessInstance(this::completedAndOutdatedChildProcessInstance, page);
    if (searchResponse.items().isEmpty()) {
      return null;
    } else {
      searchResponse.items().forEach(this::handleCompletedAndOutdatedChildProcessInstance);
      return searchResponse.page().endCursor();
    }
  }

  private void handleCompletedAndOutdatedChildProcessInstance(
      final ProcessInstance processInstance) {
    taskContext.submit(new OrphanDetectionTask(processInstance));
  }
}
