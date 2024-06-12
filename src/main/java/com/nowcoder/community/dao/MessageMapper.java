package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    //search current user's conversation list, for each conversation return the latest message
    List<Message> selectConversation(int userId, int offset, int limit);

    //get current user's conversation count
    int selectConversationCount(int userId);

    //get specific conversation messages list
    List<Message> selectLetters(String conversationId,int offset, int limit );

    //get specific conversation messages count
    int selectLetterCount(String conversationId);

    //get unread messages count
    int selectLetterUnreadCount(int userId, String conversationId);

    //new message
    int insertMessage(Message message);

    // update message status(read/unread)
    int updateStatus(List<Integer> ids, int status);










}
