package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSetting(){
        return "/site/setting";
    }

    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error", "did not upload a image");
            return "/site/setting";
        }
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error", "file format is not supported");
            return "/site/setting";
        }
        fileName = CommunityUtil.generateUUID() +suffix;
        File dest = new File(uploadPath + "/" + fileName);
        try{
            headerImage.transferTo(dest);
        }catch (IOException e){
           logger.error("upload file faild: " + e.getMessage());
           throw new RuntimeException("upload file faild, server error",e);
        }
        User user = hostHolder.getUsers();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //server path
        fileName = uploadPath + "/"+ fileName;
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        response.setContentType("image/" + suffix);
        try {
            OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(fileName);
            {
                byte[] buffer = new byte[1024];
                int b = 0;
                while ((b = fis.read(buffer)) != -1) {
                    os.write(buffer,0, b);
                }
            }
        } catch (IOException e) {
            logger.error("load image faild: " + e.getMessage());

        }


    }



}
