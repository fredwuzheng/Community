package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();
        // check empty value
        if( user == null ){
            throw new IllegalArgumentException("User cannot be null");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","User name cannot be empty");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("usernameMsg","User email cannot be empty");
            return map;
        } if(StringUtils.isBlank(user.getPassword())) {
            map.put("usernameMsg", "User password cannot be empty");
            return map;
        }
        //check if user name exits
        User u = userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg", "User name already exits");
            return map;
        }

        //check if user email exits
         u = userMapper.selectByName(user.getEmail());
        if(u != null){
            map.put("usernameMsg", "User email already exits");
            return map;
        }
        //register user
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        //activate email
        org.thymeleaf.context.Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        System.out.println(content);
        mailClient.sendMail(user.getEmail(), "Activate Account", content);
        return map;
    }

    public int activation(int userId, String activationCode){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return ACTIVACTION_REPEAT;
        }
        else if(user.getActivationCode().equals(activationCode)) {
            userMapper.updateStatus(userId, ACTIVACTION_SUCCESS);
            return ACTIVACTION_SUCCESS;
        }
        else{
            return ACTIVACTION_FAILURE;
        }
    }

    public Map<String,Object> login(String username,String password,int expiredSeconds){
        Map<String,Object> map = new HashMap<>();
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg", "user name cannot be empty");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg", "password cannot be empty");
            return map;
        }
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg", "account does not exist");
            return map;
        }
        if(user.getStatus() == 0){
            map.put("usernameMsg", "account is not active");
            return map;
        }
        String passwordSalt  = CommunityUtil.md5(password + user.getSalt());
        if(!user.getPassword().equals(passwordSalt)){
            map.put("passwordMsg", "password does not match");
            return map;
        }

        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);
        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket,1);
    }

    public LoginTicket findLoginTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);
    }

    public int updateHeader(int userId,String headerUrl){
        return userMapper.updateHeader(userId,headerUrl);
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }
}
