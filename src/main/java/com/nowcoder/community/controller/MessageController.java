package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.catalina.Host;
import org.apache.kafka.common.network.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {
    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    @RequestMapping(path = "/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        page.setLimit(5);
        page.setPath("/letter/list");
        User user = hostHolder.getUsers();
        int userId = user.getId();
        page.setRows(messageService.findConversationsCount(userId));
        List<Message> conversationList = messageService.findConversations(userId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> conversations = new ArrayList<>();
        if(conversationList != null){
            for(Message message: conversationList){
                Map<String,Object> map = new HashMap<>();
                String conversationId = message.getConversationId();
                int unreadCount = messageService.findLetterUnreadCount(userId, conversationId);
                int letterCount = messageService.findLetterCount(conversationId);
                map.put("conversation",message);
                map.put("letterCount",letterCount);
                map.put("unreadCount",unreadCount);

                int targetId = userId == message.getFromId()? message.getToId() : message.getFromId();
                map.put("target",userService.findUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);

        //unread message
        int letterUnreadCount = messageService.findLetterUnreadCount(userId,null);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);

        model.addAttribute("letterUnreadCount",letterUnreadCount);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/letter";
    }

    @RequestMapping(path = "/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page,Model model){
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        List<Message> letterList = messageService.findLetters(conversationId,page.getOffset(),page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        if(letterList !=null){
            for(Message message : letterList){
                Map<String,Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
        model.addAttribute("target",getLetterTarget(conversationId));
//        model.addAttribute("currentUser", userService.findUserById(hostHolder.getUsers().getId()));

        List<Integer> ids = getLetterIds(letterList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";

    }
    private User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int id0= Integer.parseInt(ids[0]);
        int id1= Integer.parseInt(ids[1]);
        if(hostHolder.getUsers().getId() == id0){
            return userService.findUserById(id1);
        }
        return userService.findUserById(id0);
    }

    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids = new ArrayList<>();
        if(letterList != null){
            for(Message message : letterList){
                if(message.getToId() == hostHolder.getUsers().getId() && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }
        return  ids;

    }

    @RequestMapping(path = "letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content){
        User targetUser = userService.findUserByName(toName);
        if(targetUser == null){
            return CommunityUtil.getJSONString(1,"user does not exits");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUsers().getId());
        message.setToId(targetUser.getId());
        if(message.getFromId() < message.getToId()){
            message.setConversationId(message.getFromId()+ "_" + message.getToId());
        }
        else{
            message.setConversationId(message.getToId()+ "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/notice/list",method = RequestMethod.GET)
    public String getNoticeList(Model model){
        User user = hostHolder.getUsers();
//        List<String> TOPIC_LIST = Arrays.asList(
//                TOPIC_COMMENT,
//                TOPIC_LIKE,
//                TOPIC_FOLLOW
//        );
//        List<String> TOPIC_NAME = Arrays.asList(
//                "commentNotice",
//                "likeNotice",
//                "followNotice"
//        );

        //query comment notice
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        Map<String,Object> messageVo = new HashMap<>();
        if(message != null){
            messageVo.put("message",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));
            int count = messageService.findNoticeCount(user.getId(),TOPIC_COMMENT);
            messageVo.put("count",count);
            int unread = messageService.findNoticeUnreadCount(user.getId(),TOPIC_COMMENT);
            messageVo.put("unread",unread);
            model.addAttribute("commentNotice",messageVo);
        }
//        model.addAttribute("commentNotice",messageVo);

        //query like notice
        message = messageService.findLatestNotice(user.getId(),TOPIC_LIKE);
        messageVo = new HashMap<>();
        if(message != null){
            messageVo.put("message",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));
            int count = messageService.findNoticeCount(user.getId(),TOPIC_LIKE);
            messageVo.put("count",count);
            int unread = messageService.findNoticeUnreadCount(user.getId(),TOPIC_LIKE);
            messageVo.put("unread",unread);
            model.addAttribute("likeNotice",messageVo);

        }
//        model.addAttribute("likeNotice",messageVo);

        //query follow notice
        message = messageService.findLatestNotice(user.getId(),TOPIC_FOLLOW);
        messageVo = new HashMap<>();
        if(message != null){
            messageVo.put("message",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));
            int count = messageService.findNoticeCount(user.getId(),TOPIC_FOLLOW);
            messageVo.put("count",count);
            int unread = messageService.findNoticeUnreadCount(user.getId(),TOPIC_FOLLOW);
            messageVo.put("unread",unread);
            model.addAttribute("followNotice",messageVo);

        }
//        model.addAttribute("followNotice",messageVo);
//        //for loop
//        for(int i = 0; i < TOPIC_LIST.size();i ++ ){
//            message = messageService.findLatestNotice(user.getId(),TOPIC_LIST.get(i));
//            messageVo = new HashMap<>();
//            if(message != null){
//                messageVo.put("message",,message);
//                String content = HtmlUtils.htmlUnescape(message.getContent());
//                Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
//
//                messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
//                messageVo.put("entityType",data.get("entityType"));
//                messageVo.put("entityId",data.get("entityId"));
//                messageVo.put("postId",data.get("postId"));
//                int count = messageService.findNoticeCount(user.getId(),TOPIC_LIST.get(i));
//                messageVo.put("count",count);
//                int unread = messageService.findNoticeUnreadCount(user.getId(),TOPIC_LIST.get(i));
//                messageVo.put("unread",unread);
//            }
//            model.addAttribute(TOPIC_NAME.get(i),messageVo);
//        }
        //query unread total unread message count
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        //query unread total unread notice count
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        return "/site/notice";
    }
    @RequestMapping(path = "/notice/detail/{topic}",method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic,Page page,Model model){
        User user = hostHolder.getUsers();
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList =messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String,Object>> noticeVo = new ArrayList<>();
        if(noticeList != null){
            for(Message notice:noticeList){
                Map<String,Object> map = new HashMap<>();
                map.put("notice",notice);
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user",userService.findUserById((Integer) data.get("userId")));
                map.put("entityType",data.get("entityType"));
                map.put("entityId",data.get("entityId"));
                map.put("postId",data.get("postId"));
                map.put("fromUser",userService.findUserById(notice.getFromId()));
                noticeVo.add(map);
            }
        }
        model.addAttribute("notices",noticeVo);

        List<Integer> ids = getLetterIds(noticeList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/notice-detail";
    }
}
