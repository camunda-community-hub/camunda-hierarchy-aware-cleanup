package io.camunda.cleanup;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AppTest {

  @Autowired ProcessInstanceCleaner processInstanceCleaner;

  @Test
  void shouldRun() {
    assertThat(processInstanceCleaner).isNotNull();
  }
}
