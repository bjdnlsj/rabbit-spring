package com.bjdnlsj.rabbitspring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

	@Test
	public void contextLoads() {
	}
	
	@Autowired
	private RabbitAdmin rabbitAdmin;
	
	@Test
	public void testAdmin() throws Exception {
		// 添加exchange
		rabbitAdmin.declareExchange(new DirectExchange("test.direct", false, false));
		rabbitAdmin.declareExchange(new TopicExchange("test.topic", false, false));
		rabbitAdmin.declareExchange(new FanoutExchange("test.fanout", false, false));

		//创建队列queque
		rabbitAdmin.declareQueue(new Queue("test.direct.queue",false));
		rabbitAdmin.declareQueue(new Queue("test.topic.queue",false));
		rabbitAdmin.declareQueue(new Queue("test.fanout.queue",false));

		// 绑定 两种方式
		rabbitAdmin.declareBinding(new Binding("test.direct.queue", Binding.DestinationType.QUEUE, "test.direct", "direct",new HashMap<>()));
		rabbitAdmin.declareBinding(
				BindingBuilder
				.bind(new Queue("test.topic.queue",false))
				.to(new TopicExchange("test.topic",false,false))
				.with("user.#"));
		rabbitAdmin.declareBinding(BindingBuilder
				.bind(new Queue("test.fanout.queue",false))
				.to(new FanoutExchange("test.fanout",false,false))); // fanout类型不走路由键

		// 清空队列
		rabbitAdmin.purgeQueue("test.topic.queue",false);
	}


	@Autowired
	public RabbitTemplate rabbitTemplate;

	@Test
	public void testSendMessage() throws Exception{
		// 创建消息
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.getHeaders().put("desc", "信息描述..");
		messageProperties.getHeaders().put("type", "自定义消息类型..");
		Message message = new Message("HELLO RabbitMQ".getBytes(),messageProperties);

		rabbitTemplate.convertAndSend("topic001", "spring.amqp", message, new MessagePostProcessor() {
					@Override
					public Message postProcessMessage(Message message) throws AmqpException {
						System.out.println("____________添加额外配置_________________");
						message.getMessageProperties().getHeaders().put("desc","额外信息描述");
						message.getMessageProperties().getHeaders().put("sttr","额外属性");
						return message;
					}
				}
		);
	}
	@Test
	public void testSendMessage2() throws Exception{
		// 创建消息
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.getHeaders().put("desc", "信息描述..");
		messageProperties.getHeaders().put("type", "自定义消息类型..");
		Message message = new Message("HELLO RabbitMQ123123".getBytes(),messageProperties);
		rabbitTemplate.convertAndSend("topic001", "spring.amqp", "hello object message send!");
		rabbitTemplate.convertAndSend("topic002", "rabbit.abc", "hello object message send!");
		rabbitTemplate.send("topic002", "rabbit.abc", message);
	}
	

}
