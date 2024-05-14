package com.nowcoder.community.controller;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {
    @Autowired
    private UserService userService;
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
}
