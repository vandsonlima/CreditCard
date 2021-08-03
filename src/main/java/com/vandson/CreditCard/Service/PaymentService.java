package com.vandson.CreditCard.Service;

import com.vandson.CreditCard.domain.Payment;
import com.vandson.CreditCard.domain.PaymentEvent;
import com.vandson.CreditCard.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

public interface PaymentService {

    Payment newPayment(Payment payment);

    StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId);
}
