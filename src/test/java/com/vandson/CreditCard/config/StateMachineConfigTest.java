package com.vandson.CreditCard.config;

import com.vandson.CreditCard.domain.PaymentEvent;
import com.vandson.CreditCard.domain.PaymentState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StateMachineConfigTest {

    @Autowired
    StateMachineFactory<PaymentState, PaymentEvent> factory;

    @Test
    void testNewStateMachine(){
        StateMachine<PaymentState, PaymentEvent> stateMachine = factory.getStateMachine(UUID.randomUUID());

        stateMachine.startReactively().subscribe();

        System.out.println(stateMachine.getState().toString());

        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(PaymentEvent.PRE_AUTHORIZE).build())).doOnComplete(
                () -> {
                    System.out.println("Event handling complete - " + stateMachine.getState().getId().toString());
                }
        ).subscribe();


        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED).build())).doOnComplete(
                () -> {
                    System.out.println("Event handling complete - " + stateMachine.getState().getId().toString());
                }
        ).subscribe();

        stateMachine.stopReactively().subscribe();

        stateMachine.startReactively().subscribe();
        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINED).build())).doOnComplete(
                () -> {
                    System.out.println("Event handling complete " + stateMachine.getState().getId().toString());
                }
        ).subscribe();

    }
}