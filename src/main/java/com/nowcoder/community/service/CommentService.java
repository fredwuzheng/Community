package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

import static com.nowcoder.community.util.CommunityConstant.ENTITY_TYPE_POST;

@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentsByEntity(entityType,entityId,offset,limit);
    }
    public int findCommentCount(int entityType, int entityId ){
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if(comment == null){
            throw new IllegalArgumentException("comment cannot be empty");
        }
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);
        if(comment.getEntityType()== ENTITY_TYPE_POST){
           int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
           discussPostService.updateCommentCount(comment.getEntityId(),count);
        }
        return rows;
    }
}
