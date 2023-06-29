package com.github.xdnw.gpt;

import gg.acai.chatgpt.ChatGPT;
import gg.acai.chatgpt.ChatGPTBuilder;
import gg.acai.chatgpt.Conversation;
import gg.acai.chatgpt.Response;
import gg.acai.chatgpt.exception.ParsedExceptionEntry;

public class ChatGPTWebHandler implements IGPTHandler {

    private ChatGPT instance;
    private String token;
    private String cf_clearance_here;
    private String user_agent_here;

    public ChatGPTWebHandler() {
    }

    public void login(String token, String cf_clearance_here, String user_agent_here) {
        this.token = token;
        this.cf_clearance_here = cf_clearance_here;
        this.user_agent_here = user_agent_here;

        if (token == null) throw new IllegalStateException("Token not set!");
        if (cf_clearance_here == null) throw new IllegalStateException("cf_clearance_here not set!");
        if (user_agent_here == null) throw new IllegalStateException("user_agent_here not set!");

        ChatGPTBuilder builder = ChatGPT.newBuilder();
        if (!cf_clearance_here.isEmpty()) builder.cfClearance(cf_clearance_here); // required to bypass Cloudflare: get from cookies

        this.instance = builder
            .sessionToken(token) // required field: get from cookies
            .userAgent(user_agent_here) // required to bypass Cloudflare: google 'what is my user agent'
            .addExceptionAttribute(new ParsedExceptionEntry("exception keyword", Exception.class)) // optional: adds an exception attribute
            .connectTimeout(60L) // optional: specify custom connection timeout limit
            .readTimeout(30L) // optional: specify custom read timeout limit
            .writeTimeout(30L) // optional: specify custom write timeout limit
            .build(); // builds the ChatGPT client
    }

    @Override
    public String getResponse(String prompt, int tokens, float temperature, String... stop) {
        if (instance == null) throw new IllegalStateException("Not logged in!");
        Conversation conversation = instance.createConversation();
        Response msg = conversation.sendMessage(prompt);
        return msg.getMessage();
    }
    
}
