package com.vandson.CreditCard.Service;

import com.vandson.CreditCard.domain.Payment;
import com.vandson.CreditCard.domain.PaymentEvent;
import com.vandson.CreditCard.domain.PaymentState;
import com.vandson.CreditCard.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    PaymentService paymentService;
    private Payment payment;
    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("12.99")).build();
    }

    @Transactional
    @Test
    void preAuth() {

        Payment savedPayment = paymentService.newPayment(payment);

        StateMachine<PaymentState, PaymentEvent> stateMachine = paymentService.preAuth(savedPayment.getId());
        var preauthed  = paymentRepository.getById(savedPayment.getId());

        System.out.println(stateMachine.getState().getId());
        System.out.println(preauthed.getState());
    }
}