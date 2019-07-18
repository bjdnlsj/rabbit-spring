package com.bjdnlsj.rabbitspring.config;


import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.ConsumerTagStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@ComponentScan({"com.bjdnlsj.**.*"})
public class RabbitMQConfig {

	@Bean
	public ConnectionFactory connectionFactory(){
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setAddresses("www.bjdnlsj.com:5672");
		connectionFactory.setUsername("admin");
		connectionFactory.setPassword("admin");
		connectionFactory.setVirtualHost("/");
		return connectionFactory;
	}
	
	@Bean
	public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
		RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
		rabbitAdmin.setAutoStartup(true);
		return rabbitAdmin;
	}

	@Bean
	public TopicExchange exchange001(){
		return new TopicExchange("topic001",true,false);
	}

	@Bean
	public Queue queue001() {
		return new Queue("queue001", true); //队列持久
	}

	@Bean
	Binding binding001(){
		return BindingBuilder.bind(queue001()).to(exchange001()).with("spring.*");
	}

	@Bean
	public TopicExchange exchange002() {
		return new TopicExchange("topic002", true, false);
	}

	@Bean
	public Queue queue002() {
		return new Queue("queue002", true); //队列持久
	}

	@Bean
	public Binding binding002() {
		return BindingBuilder.bind(queue002()).to(exchange002()).with("rabbit.*");
	}

	@Bean
	public Queue queue003() {
		return new Queue("queue003", true); //队列持久
	}

	@Bean
	public Binding binding003() {
		return BindingBuilder.bind(queue003()).to(exchange001()).with("mq.*");
	}

	@Bean
	public Queue queue_image() {
		return new Queue("image_queue", true); //队列持久
	}

	@Bean
	public Queue queue_pdf() {
		return new Queue("pdf_queue", true); //队列持久
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		return rabbitTemplate;
	}

	@Bean
	public SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory){

		// 创建一个消息容器对象
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
		// 设置实际的队列
		container.setQueues(queue001(),queue002(),queue003(),queue_image(),queue_pdf());
		//设置消费者数量
		container.setConcurrentConsumers(1);
		//设置最大的消费者数量
		container.setMaxConcurrentConsumers(5);
		// 是否重回队列
		container.setDefaultRequeueRejected(false);
		// 牵收模式
		container.setAcknowledgeMode(AcknowledgeMode.AUTO);
		container.setExposeListenerChannel(true);

		container.setConsumerTagStrategy(new ConsumerTagStrategy() {
			@Override
			public String createConsumerTag(String s) {
				return s+"_"+ UUID.randomUUID().toString();
			}
		});
		container.setMessageListener(new ChannelAwareMessageListener() {
			@Override
			public void onMessage(Message message, Channel channel) throws Exception {
				String msg = String.valueOf(message.getBody());
				System.err.println("---------------------消费者："+msg);
			}
		});
		return  container;
	}

}
