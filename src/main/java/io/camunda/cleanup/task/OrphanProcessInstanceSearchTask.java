package io.camunda.cleanup.task;

import io.camunda.client.api.search.request.SearchRequestPage;
import io.camunda.client.api.search.response.ProcessInstance;
import io.camunda.client.api.search.response.SearchResponse;
import java.util.function.Consumer;

public record OrphanProcessInstanceSearchTask(long parentProcessInstanceKey, Consumer<SearchRequestPage> page)
    implements Task {

  @Override
  public void run(TaskContext context) {
    context
        .camundaClient()
        .searchProcessInstance(
            f -> f.parentProcessInstanceKey(parentProcessInstanceKey),
            page,
            r -> handleSearchResponse(r, context::submit)
        );
  }

  private void handleSearchResponse(SearchResponse<ProcessInstance> searchResponse, Consumer<Task> submitter) {
    if (!searchResponse
        .items()
        .isEmpty()) {
      submitter.accept(new OrphanProcessInstanceSearchTask(
          parentProcessInstanceKey,
          p -> p
              .limit(100)
              .after(searchResponse
                  .page()
                  .endCursor())
      ));
      searchResponse
          .items()
          .forEach(processInstance -> {
            submitter.accept(new OrphanProcessInstanceSearchTask(
                processInstance.getProcessInstanceKey(),
                p -> p.limit(100)
            ));
            // executor.execute(() ->
            // findDecisionInstances(processInstance.getProcessInstanceKey(), p ->
            // p.limit(100)));
            submitter.accept(new DeleteProcessInstanceTask(processInstance));
          });
    }
  }
}
