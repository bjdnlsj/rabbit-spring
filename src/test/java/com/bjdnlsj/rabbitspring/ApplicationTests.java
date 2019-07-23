package com.bjdnlsj.rabbitspring;

import com.bjdnlsj.rabbitspring.entity.Order;
import com.bjdnlsj.rabbitspring.entity.Packaged;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Paths;
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
		messageProperties.setContentType("text/plain");
		messageProperties.getHeaders().put("type", "自定义消息类型..");
		Message message = new Message("HELLO RabbitMQ123123".getBytes(),messageProperties);
		rabbitTemplate.convertAndSend("topic001", "spring.amqp", "hello object message send!");
		rabbitTemplate.convertAndSend("topic002", "rabbit.abc", "hello object message send!");
		rabbitTemplate.send("topic002", "rabbit.abc", message);
	}
	@Test
	public void testSendMessage3() throws Exception{
		// 创建消息
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setContentType("text/plain");
		Message message = new Message("HELLO RabbitMQ123123".getBytes(),messageProperties);
		rabbitTemplate.convertAndSend("topic001", "spring.abc", "hello object message send!");
		rabbitTemplate.convertAndSend("topic002", "rabbit.abc", "hello object message send!");
	}
	@Test
	public void testSendJsonMessage() throws Exception {

		Order order = new Order();
		order.setId("001");
		order.setName("消息订单");
		order.setContent("描述信息");
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(order);
		System.err.println("order 4 json: " + json);

		MessageProperties messageProperties = new MessageProperties();
		//这里注意一定要修改contentType为 application/json
		messageProperties.setContentType("application/json");
		Message message = new Message(json.getBytes(), messageProperties);

		rabbitTemplate.send("topic001", "spring.order", message);
	}
	@Test
	public void testSendJavaMessage() throws Exception {

		Order order = new Order();
		order.setId("001");
		order.setName("订单消息");
		order.setContent("订单描述信息");
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(order);
		System.err.println("order 4 json: " + json);

		MessageProperties messageProperties = new MessageProperties();
		//这里注意一定要修改contentType为 application/json
		messageProperties.setContentType("application/json");
		messageProperties.getHeaders().put("__TypeId__", "order");
		Message message = new Message(json.getBytes(), messageProperties);

		rabbitTemplate.send("topic001", "spring.order", message);
	}
	@Test
	public void testSendMappingMessage() throws Exception {

		ObjectMapper mapper = new ObjectMapper();

		Order order = new Order();
		order.setId("001");
		order.setName("订单消息");
		order.setContent("订单描述信息");

		String json1 = mapper.writeValueAsString(order);
		System.err.println("order 4 json: " + json1);

		MessageProperties messageProperties1 = new MessageProperties();
		//这里注意一定要修改contentType为 application/json
		messageProperties1.setContentType("application/json");
		messageProperties1.getHeaders().put("__TypeId__", "order");
		Message message1 = new Message(json1.getBytes(), messageProperties1);
		rabbitTemplate.send("topic001", "spring.order", message1);

		Packaged pack = new Packaged();
		pack.setId("002");
		pack.setName("包裹消息");
		pack.setDescription("包裹描述信息");

		String json2 = mapper.writeValueAsString(pack);
		System.err.println("pack 4 json: " + json2);

		MessageProperties messageProperties2 = new MessageProperties();
		//这里注意一定要修改contentType为 application/json
		messageProperties2.setContentType("application/json");
		messageProperties2.getHeaders().put("__TypeId__", "packaged");
		Message message2 = new Message(json2.getBytes(), messageProperties2);
		rabbitTemplate.send("topic001", "spring.pack", message2);
	}

	@Test
	public void testSendExtConverterMessage() throws Exception {
			byte[] body = Files.readAllBytes(Paths.get("C:\\Users\\lenovo\\Desktop", "timg.png"));
			MessageProperties messageProperties = new MessageProperties();
			messageProperties.setContentType("image/png");
			messageProperties.getHeaders().put("extName", "png");
			Message message = new Message(body, messageProperties);
			rabbitTemplate.send("", "image_queue", message);

		//byte[] body = Files.readAllBytes(Paths.get("d:/002_books", "mysql.pdf"));
		//MessageProperties messageProperties = new MessageProperties();
		//messageProperties.setContentType("application/pdf");
		//Message message = new Message(body, messageProperties);
		//rabbitTemplate.send("", "pdf_queue", message);
	}
}
