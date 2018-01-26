package com.itheima.miaosha.activemq;

import com.itheima.miaosha.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Queue;

@Component

public class Producer {
    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Autowired
    private Queue queue;

    public void send(MiaoMessage miaoMessage) {
        String mm = JsonUtils.objectToJson(miaoMessage);
        this.jmsMessagingTemplate.convertAndSend(this.queue, mm);
    }
}
