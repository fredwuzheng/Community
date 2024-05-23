package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "***";

    //root node
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    private void init(){
        try(
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            ){
            String keyword;
            while((keyword = reader.readLine())!=null){
                this.addKeyWord(keyword);
            }
        }catch (IOException e){
            logger.error("load sensitiveword file faild " + e.getMessage());

        }
    }

    private void addKeyWord( String keyword){
        TrieNode tempNode = rootNode;
        for(int i = 0; i < keyword.length(); i++){
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if(subNode == null){
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            tempNode = subNode;
            if(i == keyword.length() - 1){
                tempNode.setKeywordEnd(true);
            }
        }
    }
    /* filter sensitive keyword
    * param: text
    * return filtered text
    */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        //ptr 1
        TrieNode tempNode = rootNode;
        //ptr 2
        int begin = 0;
        //ptr 3
        int position = 0;
        StringBuilder sb = new StringBuilder();
        while(position < text.length()){
            char c  = text.charAt(position);
            //skip symbol
            if(isSymbol(c)){
                if(tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            tempNode = tempNode.getSubNode(c);
            if(tempNode == null){
                //word start with begin is not a sensitive word
                sb.append(text.charAt(begin));
                position = ++ begin;
                tempNode = rootNode;
            }
            else if(tempNode.isKeywordEnd()){

                //found sensitive word, replace it
                sb.append(REPLACEMENT);
                begin = ++position;
                tempNode = rootNode;
            }
            else{
                //check next character
                position++;
            }
        }
        // put the last word in the result
        sb.append(text.substring(begin));
        return sb.toString();
    }
    private boolean isSymbol(Character c){
        // (c < 0x2E80 || c > 0x9FFF) is east asian character
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    private class TrieNode{
        private boolean isKeywordEnd = false;

        //sub node (key is sub character, value is sub node)
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd(){
            return isKeywordEnd;
        }
        public void setKeywordEnd(boolean keywordEnd){
            isKeywordEnd = keywordEnd;
        }
        //add sub node
        public void addSubNode(Character c, TrieNode node){
            subNodes.put(c,node);
        }

        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }

    }

}
