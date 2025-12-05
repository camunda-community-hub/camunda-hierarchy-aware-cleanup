package io.camunda.cleanup.task;

import io.camunda.cleanup.CamundaClientFacade;
import io.camunda.cleanup.audit.ProcessInstanceDeletionAudit;
import io.camunda.client.api.search.response.ProcessInstance;
import io.camunda.client.api.search.response.SearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskTest {
  @Mock
  ProcessInstance processInstance;
  @Mock
  TaskContext taskContext;
  @Mock
  CamundaClientFacade camundaClientFacade;
  @Mock
  ProcessInstanceDeletionAudit deletionAudit;
  @Mock
  SearchResponse<ProcessInstance> searchResponse;
  @Captor
  ArgumentCaptor<Task> taskCaptor;
  @Captor
  ArgumentCaptor<Consumer<SearchResponse<ProcessInstance>>> searchResponseCaptor;

  @BeforeEach
  void setup() {
    lenient()
        .when(taskContext.camundaClient())
        .thenReturn(camundaClientFacade);
    lenient()
        .when(processInstance.getParentProcessInstanceKey())
        .thenReturn(123L);
    lenient()
        .when(processInstance.getProcessInstanceKey())
        .thenReturn(1234L);
    lenient()
        .when(taskContext.deletionAudit())
        .thenReturn(deletionAudit);
  }

  @Nested
  class OrphanDetection {
    @Test
    void shouldHandleOrphan() {
      when(camundaClientFacade.processInstanceExists("123")).thenReturn(false);
      OrphanDetectionTask task = new OrphanDetectionTask(processInstance);
      task.run(taskContext);
      verify(processInstance, times(2)).getParentProcessInstanceKey();
      verify(camundaClientFacade).processInstanceExists("123");
      verify(taskContext).submit(new OrphanProcessInstanceSearchTask(123L, any()));
    }

    @Test
    void shouldNotHandleNonOrphan() {
      when(camundaClientFacade.processInstanceExists("123")).thenReturn(true);
      OrphanDetectionTask task = new OrphanDetectionTask(processInstance);
      task.run(taskContext);
      verify(processInstance).getParentProcessInstanceKey();
      verify(camundaClientFacade).processInstanceExists("123");
      verify(taskContext, never()).submit(new OrphanProcessInstanceSearchTask(123L, any()));
    }
  }

  @Nested
  class CompletedAndOutdatedRootProcessInstanceHandler {
    @Test
    void shouldSubmitFollowUpTasks() {
      CompletedAndOutDatedRootProcessInstanceHandlerTask task = new CompletedAndOutDatedRootProcessInstanceHandlerTask(
          processInstance);
      task.run(taskContext);
      verify(taskContext, times(2)).submit(taskCaptor.capture());
      List<Task> allValues = taskCaptor.getAllValues();
      assertThat(allValues).hasSize(2);
      assertThat(allValues.get(0))
          .isInstanceOf(OrphanProcessInstanceSearchTask.class)
          .extracting(OrphanProcessInstanceSearchTask.class::cast)
          .extracting(OrphanProcessInstanceSearchTask::parentProcessInstanceKey)
          .isEqualTo(1234L);
      assertThat(allValues.get(1)).isEqualTo(new DeleteProcessInstanceTask(processInstance));
    }
  }

  @Nested
  class DeleteProcessInstance {
    @Test
    void shouldDeleteProcessInstance() {
      DeleteProcessInstanceTask task = new DeleteProcessInstanceTask(processInstance);
      task.run(taskContext);
      verify(camundaClientFacade).deleteProcessInstance("1234");
      verify(deletionAudit).pushPending(any());
      verify(deletionAudit).pushSuccess(any());
    }
  }

  @Nested
  class OrphanProcessInstanceSearch {
    @Test
    void shouldSearchNextRoundAndSearchChildrenAndDelete() {
      when(searchResponse.items()).thenReturn(List.of(processInstance));
      doNothing()
          .when(camundaClientFacade)
          .searchProcessInstance(any(), any(), searchResponseCaptor.capture());
      OrphanProcessInstanceSearchTask task = new OrphanProcessInstanceSearchTask(135L, p -> p.limit(100));
      task.run(taskContext);
      searchResponseCaptor
          .getValue()
          .accept(searchResponse);
      verify(taskContext, times(3)).submit(taskCaptor.capture());
      List<Task> captorTasks = taskCaptor.getAllValues();
      assertThat(captorTasks).hasSize(3);
      assertThat(captorTasks.get(0))
          .isInstanceOf(OrphanProcessInstanceSearchTask.class)
          .extracting(OrphanProcessInstanceSearchTask.class::cast)
          .extracting(OrphanProcessInstanceSearchTask::parentProcessInstanceKey)
          .isEqualTo(135L);
      assertThat(captorTasks.get(1))
          .isInstanceOf(OrphanProcessInstanceSearchTask.class)
          .extracting(OrphanProcessInstanceSearchTask.class::cast)
          .extracting(OrphanProcessInstanceSearchTask::parentProcessInstanceKey)
          .isEqualTo(1234L);
      assertThat(captorTasks.get(2)).isEqualTo(new DeleteProcessInstanceTask(processInstance));
    }
  }
}
