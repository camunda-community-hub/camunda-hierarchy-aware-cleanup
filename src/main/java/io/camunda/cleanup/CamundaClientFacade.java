package io.camunda.cleanup;

import io.camunda.client.api.search.filter.ProcessInstanceFilter;
import io.camunda.client.api.search.request.SearchRequestPage;
import io.camunda.client.api.search.response.ProcessInstance;
import io.camunda.client.api.search.response.SearchResponse;
import java.util.function.Consumer;

/** A simplification of the CamundaClient */
public interface CamundaClientFacade {
  /**
   * Deletes a process instance
   *
   * @param processInstanceKey the key of the process instance to delete
   */
  void deleteProcessInstance(String processInstanceKey);

  /**
   * Searches for process instances based on the given filter and page. Sorting is always ascending
   * by end date
   *
   * @param filter the filter to apply to the search request
   * @param page the paging to apply to the search request
   * @param responseHandler the handler for the response
   */
  void searchProcessInstance(
      Consumer<ProcessInstanceFilter> filter,
      Consumer<SearchRequestPage> page,
      Consumer<SearchResponse<ProcessInstance>> responseHandler);

  /**
   * Searches for process instances based on the given filter and page. Sorting is always ascending
   * by end date
   *
   * @param filter the filter to apply to the search request
   * @param page the paging to apply to the search request
   * @return the search response
   */
  SearchResponse<ProcessInstance> searchProcessInstance(
      Consumer<ProcessInstanceFilter> filter, Consumer<SearchRequestPage> page);

  /**
   * Checks whether a process instance exists
   *
   * @param processInstanceKey the key of the process instance to check
   * @return whether the process instance exists
   */
  boolean processInstanceExists(String processInstanceKey);
}
