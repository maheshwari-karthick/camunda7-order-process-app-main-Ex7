package com.camunda.training.payment;

import com.camunda.training.services.CreditCardService;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ExternalTaskSubscription("creditCardCharging")
public class CreditCardHandler implements ExternalTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CreditCardHandler.class);

    @Autowired
    public CreditCardService service;

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        LOG.info("handle topic {} for task id {}", externalTask.getTopicName(), externalTask.getId());

        String cardNumber = externalTask.getVariable("cardNumber");
        String cvc = externalTask.getVariable("CVC");
        String expiryDate = externalTask.getVariable("expiryDate");
        Double openAmount = externalTask.getVariable("openAmount");

        service.chargeAmount(cardNumber, cvc, expiryDate, openAmount);

        externalTaskService.complete(externalTask);

    }

}
