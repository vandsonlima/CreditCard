package com.vandson.CreditCard.Service;

import com.vandson.CreditCard.domain.Payment;
import com.vandson.CreditCard.domain.PaymentEvent;
import com.vandson.CreditCard.domain.PaymentState;
import com.vandson.CreditCard.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService{
        public static final String PAYMENT_ID_HEADER = "payment_id";

        private final PaymentRepository paymentRepository;

        private final StateMachineFactory<PaymentState, PaymentEvent> factory;

        private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;

        @Override
        public Payment newPayment(Payment payment) {
                payment.setState(PaymentState.NEW);
                return paymentRepository.save(payment);
        }

        @Transactional
        @Override
        public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
                StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);
                sendEvent(paymentId, stateMachine, PaymentEvent.PRE_AUTH_APPROVED);
                return null;
        }

        @Override
        public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
                StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);
                sendEvent(paymentId, stateMachine, PaymentEvent.AUTH_APPROVED);
                return null;
        }

        @Override
        public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
                StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);
                sendEvent(paymentId, stateMachine, PaymentEvent.AUTH_DECLINED);
                return null;
        }

        private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> stateMachine, PaymentEvent paymentEvent){
                Message<PaymentEvent> message = MessageBuilder.withPayload(paymentEvent)
                        .setHeader(PAYMENT_ID_HEADER, paymentId)
                        .build();

                stateMachine.sendEvent(Mono.just(message)).subscribe();
        }

        private StateMachine<PaymentState, PaymentEvent> build(Long paymentId){
                Payment payment = paymentRepository.getById(paymentId);

                StateMachine<PaymentState, PaymentEvent> sm = factory.getStateMachine(Long.toString(paymentId));

                sm.stopReactively().subscribe();

                sm.getStateMachineAccessor().doWithAllRegions(
                        sma -> {
                                sma.addStateMachineInterceptor(paymentStateChangeInterceptor);
                                sma.resetStateMachineReactively(new DefaultStateMachineContext<>(payment.getState(), null, null, null ));
                        }
                );
                sm.startReactively().subscribe();
                return sm;
        }
}
