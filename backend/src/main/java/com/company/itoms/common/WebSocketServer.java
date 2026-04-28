package com.company.itoms.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.company.itoms.entity.SysMessageEntity;
import com.company.itoms.service.ISysMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/{userId}")
@Component
@Slf4j
public class WebSocketServer {

    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    private static ISysMessageService sysMessageService;
    private static ObjectMapper objectMapper;

    @Autowired
    public void setSysMessageService(ISysMessageService sysMessageService) {
        WebSocketServer.sysMessageService = sysMessageService;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        WebSocketServer.objectMapper = objectMapper;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        sessionMap.put(userId, session);
        log.info("WebSocket: user {} connected, total connections: {}", userId, sessionMap.size());
        
        // Fetch unread messages
        try {
            Long uid = Long.parseLong(userId);
            if (sysMessageService != null) {
                QueryWrapper<SysMessageEntity> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("receiver_id", uid).eq("is_read", 0);
                List<SysMessageEntity> unreadList = sysMessageService.list(queryWrapper);
                if (unreadList != null && !unreadList.isEmpty() && objectMapper != null) {
                    for (SysMessageEntity msg : unreadList) {
                        session.getBasicRemote().sendText(objectMapper.writeValueAsString(msg));
                        // Mark as read after sending
                        msg.setIsRead(1);
                        sysMessageService.updateById(msg);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch/send unread messages for user {}", userId, e);
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        sessionMap.remove(userId);
        log.info("WebSocket: user {} disconnected, total connections: {}", userId, sessionMap.size());
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        log.info("WebSocket: received message from user {}: {}", userId, message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket error", error);
    }

    public void sendMessage(String userId, String message) {
        Session session = sessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("WebSocket: send message failed", e);
            }
        }
    }
    
    // New method for saving and sending notification
    public void sendNotification(String userId, String title, String content, String type) {
        try {
            Long uid = Long.parseLong(userId);
            SysMessageEntity msg = new SysMessageEntity();
            msg.setReceiverId(uid);
            msg.setTitle(title);
            msg.setContent(content);
            msg.setType(type);
            msg.setIsRead(0);
            
            if (sysMessageService != null) {
                sysMessageService.save(msg);
            }
            
            Session session = sessionMap.get(userId);
            if (session != null && session.isOpen() && objectMapper != null) {
                session.getBasicRemote().sendText(objectMapper.writeValueAsString(msg));
                // Mark as read if sent successfully
                if (sysMessageService != null) {
                    msg.setIsRead(1);
                    sysMessageService.updateById(msg);
                }
            }
        } catch (Exception e) {
            log.error("Failed to send notification to user {}", userId, e);
        }
    }

    public void broadcast(String message) {
        for (Session session : sessionMap.values()) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.error("WebSocket: broadcast message failed", e);
                }
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebSocketServer.class);

}
