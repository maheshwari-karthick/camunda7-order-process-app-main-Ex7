package com.camunda.training;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.community.process_test_coverage.junit5.platform7.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(ProcessEngineCoverageExtension.class)
@Deployment(resources = {"Exercise5.bpmn"})
public class PaymentUnitTest {

    @Test
    public void happy_path_test_shouldChargeFromCreditCard() {
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("PaymentProcessEx5",
                withVariables("orderTotal", 45.99, "customerId", "cust30"));

        assertThat(processInstance).isWaitingAt(findId("Deduct customer credit")).externalTask().hasTopicName("creditDeduction");

        complete(externalTask(), withVariables("openAmount", 15.99, "customerCredit", 30.0));
        assertThat(processInstance).variables().contains(entry("openAmount", 15.99), entry("customerCredit", 30.0));

        assertThat(processInstance).isWaitingAt(findId("Charge credit card")).externalTask().hasTopicName("creditCardCharging");

        complete(externalTask(), withVariables("cardNumber", "1234 5678", "CVC", "123", "expiryDate", "09/24", "openAmount", 45.99));
        assertThat(processInstance).isEnded().hasPassed(findId("Payment completed"));
    }

    @Test
    public void happy_path_test_shouldDeductFromCredit() {
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("PaymentProcessEx5",
                withVariables("orderTotal", 45.99, "customerId", "cust60"));

        assertThat(processInstance).isWaitingAt(findId("Deduct customer credit")).externalTask().hasTopicName("creditDeduction");
        complete(externalTask(), withVariables("openAmount", 0.0, "customerCredit", 14.11));
        assertThat(processInstance).variables().contains(entry("openAmount", 0.0), entry("customerCredit", 14.11));

        assertThat(processInstance).isEnded().hasPassed(findId("Payment completed"));

    }

}
