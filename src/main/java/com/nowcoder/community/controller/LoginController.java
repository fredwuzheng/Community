package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private Producer kaptchaProducer;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String,Object> map = userService.register(user);
        if(map == null || map.isEmpty()){
            model.addAttribute("msg", "register sucessfull, please check your activation code in your email");
            model.addAttribute("target", "/index");

            return "/site/operate-result";
        }
        else{
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    @RequestMapping(path = "/activation/{userId}/{activationCode}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, String activationCode){
        int result = userService.activation(userId,activationCode);
        if(result == ACTIVACTION_SUCCESS){
            model.addAttribute("msg", "Activation successfull, you can use your account now");
            model.addAttribute("target", "/login");
        }
        else if(result == ACTIVACTION_REPEAT){
            model.addAttribute("msg", "Activation repeated, your activation code already being used");
            model.addAttribute("target", "/index");
        }
        else{
            model.addAttribute("msg", "Activation failure, your activation code is wrong");
            model.addAttribute("target", "/index");
        }
        return "site/operate-result";
    }
    @RequestMapping(path="/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response,HttpSession session){
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        //save kaptcha code in session
        session.setAttribute("kaptcha", text);
        //display image in website
        response.setContentType("image/png");
        try{
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        }catch(IOException e){
            logger.error("error with loading kaptcha:"+ e.getMessage());
        }

    }

    @RequestMapping(path ="/login", method = RequestMethod.POST)
    public String login(String userName, String password, String code, boolean rememberMe,
                        Model model, HttpSession session, HttpServletResponse respone){
        //check code
        String kaptcha = (String) session.getAttribute("kapttcha");
        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","verification code is incorrect");
            return "/site/login";
        }
        //check password
        int expiredSeconds = rememberMe? REMEMBER_EXPIRED_SECONDS: DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map = userService.login(userName,password,expiredSeconds);
        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            respone.addCookie(cookie);
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));

            return "redirect:/index";
        }
        else{
            return "/site/login";
        }

    }
}
