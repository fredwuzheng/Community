package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
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
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

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


}
