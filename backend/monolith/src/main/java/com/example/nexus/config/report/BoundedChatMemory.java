package com.example.nexus.config.report;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// 인메모리 대화 저장소(메모리 누수 방지): 대화 수 상한(LRU) + 대화당 메시지 수 상한
public class BoundedChatMemory implements ChatMemory {

    private final int maxConversations;            // 보관할 최대 대화(세션) 수
    private final int maxMessagesPerConversation;  // 대화당 보관할 최대 메시지 수
    private final Map<String, List<Message>> store;

    public BoundedChatMemory(int maxConversations, int maxMessagesPerConversation) {
        this.maxConversations = maxConversations;
        this.maxMessagesPerConversation = maxMessagesPerConversation;
        // access-order LinkedHashMap → 가장 오래 안 쓴 대화부터 자동 제거(LRU)
        this.store = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, List<Message>> eldest) {
                return size() > maxConversations;
            }
        });
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        synchronized (store) {
            List<Message> list = store.computeIfAbsent(conversationId, k -> new ArrayList<>());
            list.addAll(messages);
            // 대화당 메시지 상한 초과 시 오래된 것부터 제거
            int over = list.size() - maxMessagesPerConversation;
            if (over > 0) {
                list.subList(0, over).clear();
            }
        }
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        synchronized (store) {
            List<Message> all = store.get(conversationId);
            if (all == null || all.isEmpty()) {
                return List.of();
            }
            int from = Math.max(0, all.size() - lastN);
            return new ArrayList<>(all.subList(from, all.size()));
        }
    }

    @Override
    public void clear(String conversationId) {
        store.remove(conversationId);
    }
}
