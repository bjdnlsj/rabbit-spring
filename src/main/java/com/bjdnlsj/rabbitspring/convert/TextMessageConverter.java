package com.bjdnlsj.rabbitspring.convert;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

public class TextMessageConverter implements MessageConverter {

	/**
	 * @MethodName toMessage
	 * @Description 转换为消息message对象
	 * @Param object 
	 * @Param messageProperties 
	 * @Return org.springframework.amqp.core.Message
	 * @Author 宋景民<songjingmin@zuoyoutech.com>
	 * @Date 2019/7/22 10:53
	 */
	@Override
	public Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
		return new Message(object.toString().getBytes(), messageProperties);
	}


	/**
	 * @MethodName toMessage
	 * @Description message对象转换成String
	 * @Param object
	 * @Param messageProperties
	 * @Return org.springframework.amqp.core.Message
	 * @Author 宋景民<songjingmin@zuoyoutech.com>
	 * @Date 2019/7/22 10:53
	 */
	@Override
	public Object fromMessage(Message message) throws MessageConversionException {
		String contentType = message.getMessageProperties().getContentType();
		if(null != contentType && contentType.contains("text")) {
			return new String(message.getBody());
		}
		return message.getBody();
	}

}
