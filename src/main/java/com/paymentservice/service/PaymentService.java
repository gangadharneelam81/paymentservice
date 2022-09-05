package com.paymentservice.service;

import static com.paymentservice.config.ActiveMQConfig.ORDER_EVENT_QUEUE;
import static com.paymentservice.config.ActiveMQConfig.FAILED_EVENT_QUEUE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orderservice.entity.Order;
import com.paymentservice.entity.Payment;
import com.paymentservice.repository.PaymentRepository;

@Component
public class PaymentService {
	@Autowired
	PaymentRepository paymentRepository;
	
	@Autowired
	JmsTemplate jmsTemplate;
	
	private static Logger log = LoggerFactory.getLogger(PaymentService.class);
	@JmsListener(destination=ORDER_EVENT_QUEUE)
	public void receiveOrderData(@Payload Order order) {
		try {
			Thread.sleep(5000);
		} catch(Exception ex) {
			log.error(ex.getMessage());
		}
		log.info("order received "+order.getOrderId());
		
		//invoke payment gateway service
		//if payment gateway service response is success
		//insert in the payment table
		boolean paymentGatewaySuccessResp = false;
		if(paymentGatewaySuccessResp)  {
			Payment payment = new Payment();
			payment.setOrderId(order.getOrderId());
			payment.setPaymentId("P100");
			payment.setPaymentDate(new java.sql.Date(new java.util.Date().getTime()));
			paymentRepository.save(payment);
		} else {
			//send failed event to exception queue
			order.setOrderStatus("payment failed");
			jmsTemplate.convertAndSend(FAILED_EVENT_QUEUE, order);
		}
		
	}

}
