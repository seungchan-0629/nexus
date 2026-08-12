package com.example.nexus.config.report;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    // AI 챗봇 대화 히스토리 저장소 (인메모리, 단일 인스턴스용)
    // 누수 방지: 최대 1000개 세션 × 대화당 40개 메시지까지 보관, 초과 시 오래된 것부터 자동 제거
    // (서버 재시작 시 소실/다중 인스턴스 비공유는 추후 JDBC·Redis 영속화로 해결)
    @Bean
    public ChatMemory chatMemory() {
        return new BoundedChatMemory(1000, 40);
    }
}
