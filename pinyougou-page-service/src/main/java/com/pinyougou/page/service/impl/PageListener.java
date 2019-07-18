package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * 消息监听类
 */
@Component
public class PageListener implements MessageListener{

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            String text = textMessage.getText();
            System.out.println("监听到消息:"+text);
            Boolean boo = itemPageService.genItemHtml(Long.parseLong(text));
            System.out.println("页面生成结果是:"+boo);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
