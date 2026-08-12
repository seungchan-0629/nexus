package com.example.nexus.domain.notification;

import com.example.nexus.domain.notification.model.StoreNotificationDto;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class StoreSseEmitterService {

    private final Map<Long, List<SseEmitter>> emitterMap = new ConcurrentHashMap<>();

    /**
     * SSE 연결 생성 (가맹점별, 타임아웃: 30분)
     */
    public SseEmitter subscribe(Long storeIdx) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        emitterMap.computeIfAbsent(storeIdx, k -> new CopyOnWriteArrayList<>()).add(emitter);

        Runnable remove = () -> {
            List<SseEmitter> emitters = emitterMap.get(storeIdx);
            if (emitters != null) {
                emitters.remove(emitter);
                if (emitters.isEmpty()) {
                    emitterMap.remove(storeIdx);
                }
            }
        };
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(e -> remove.run());

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            remove.run();
        }

        return emitter;
    }

    /**
     * 특정 가맹점 구독자에게 알림 전송
     */
    public void send(Long storeIdx, StoreNotificationDto.ListRes notification) {
        List<SseEmitter> emitters = emitterMap.get(storeIdx);
        if (emitters == null) return;

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(notification));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}
