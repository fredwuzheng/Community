package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUsers();
        if(user!= null && modelAndView != null){
            int userId = user.getId();
            int letterUnreadCount = messageService.findLetterUnreadCount(userId, null);
            int noticeUnreadCount = messageService.findNoticeUnreadCount(userId,null);
            int totalUnread =letterUnreadCount + noticeUnreadCount;
            modelAndView.addObject("allUnreadCount",totalUnread);
        }
    }
}
