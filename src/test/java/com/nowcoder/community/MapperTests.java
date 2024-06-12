package com.nowcoder.community;

import com.nowcoder.community.dao.AlphaDao;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)

public class MapperTests {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);


    }

    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket =  new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000*60 *10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }
    @Test
    public void testSelectLoginTicket(){
        LoginTicket loginTicket =  loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
        loginTicketMapper.updateStatus("abc",1);
        loginTicket =  loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

    }
    @Test
    public void testMessageMapper(){
       List<Message> list = messageMapper.selectConversation(111,0,20);
        for(Message m : list){
            System.out.println(m);
        }
        int count2 = messageMapper.selectConversationCount(111);
        System.out.println("user111: "+ count2);

        List<Message> l = messageMapper.selectLetters("111_112",0,10);
        for(Message m : l){
            System.out.println(m);
        }
        int count1= messageMapper.selectLetterCount("111_112");
        System.out.println("111_112: "+ count1);
        int unreadCount = messageMapper.selectLetterUnreadCount(131,"111_131");
        System.out.println(unreadCount);
    }


}
