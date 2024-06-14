package com.nowcoder.community.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA ="kaptcha";
    private static final String PREFIX_TICKET="ticket";

    //a actual like
    //like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT +entityType + SPLIT + entityId;
    }
    //a user like
    //like:user:userId -> set(userId)
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT +userId;
    }

    //a user follow entity(user,post,...)
    //followee:userId:entityType -> Zset(entityId,now)
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE + SPLIT +userId + SPLIT + entityType;
    }

    //an entity(user,post,...)'s follower
    //follower:entityType:entityId -> Zset(userId,now)
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_FOLLOWER + SPLIT +entityType + SPLIT + entityId;
    }

    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA +SPLIT + owner;
    }

    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }
}
