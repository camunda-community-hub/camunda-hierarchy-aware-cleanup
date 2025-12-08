package io.camunda.cleanup;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.search.filter.ProcessInstanceFilter;
import io.camunda.client.api.search.request.SearchRequestPage;
import io.camunda.client.api.search.response.ProcessInstance;
import io.camunda.client.api.search.response.SearchResponse;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.net.URIBuilder;

public class CamundaClientFacadeImpl implements CamundaClientFacade {
  private final CamundaClient camundaClient;
  private final HttpClient httpClient;
  private final Executor executor;

  public CamundaClientFacadeImpl(
      CamundaClient camundaClient, HttpClient httpClient, Executor executor) {
    this.camundaClient = camundaClient;
    this.httpClient = httpClient;
    this.executor = executor;
  }

  @Override
  public void deleteProcessInstance(String processInstanceKey) {
    try {
      final ClassicHttpRequest r =
          new HttpDelete(
              new URIBuilder(camundaClient.getConfiguration().getRestAddress())
                  .appendPathSegments("v1", "process-instances", processInstanceKey)
                  .build());
      camundaClient.getConfiguration().getCredentialsProvider().applyCredentials(r::setHeader);
      httpClient.execute(r, new BasicHttpClientResponseHandler());
    } catch (final Exception e) {
      throw new RuntimeException("Error while deleting process instance " + processInstanceKey, e);
    }
  }

  @Override
  public void searchProcessInstance(
      Consumer<ProcessInstanceFilter> filter,
      Consumer<SearchRequestPage> page,
      Consumer<SearchResponse<ProcessInstance>> responseHandler) {
    camundaClient
        .newProcessInstanceSearchRequest()
        .filter(filter)
        .page(page)
        .sort(s -> s.endDate().asc())
        .send()
        .thenAcceptAsync(responseHandler, executor);
  }

  @Override
  public SearchResponse<ProcessInstance> searchProcessInstance(
      Consumer<ProcessInstanceFilter> filter, Consumer<SearchRequestPage> page) {
    return camundaClient
        .newProcessInstanceSearchRequest()
        .filter(filter)
        .page(page)
        .sort(s -> s.endDate().asc())
        .execute();
  }

  @Override
  public boolean processInstanceExists(String processInstanceKey) {
    return !camundaClient
        .newProcessInstanceSearchRequest()
        .filter(f -> f.processInstanceKey(Long.parseLong(processInstanceKey)))
        .execute()
        .items()
        .isEmpty();
  }
}
