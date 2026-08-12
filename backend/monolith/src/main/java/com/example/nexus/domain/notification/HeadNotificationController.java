package com.example.nexus.domain.notification;

import com.example.nexus.common.enums.NotificationType;
import com.example.nexus.common.model.BaseResponse;
import com.example.nexus.domain.notification.model.HeadNotificationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "본사 알림", description = "본사 사용자 대상 알림 — SSE 실시간 수신 + 폴링 조회")
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class HeadNotificationController {

    private final HeadNotificationService headNotificationService;
    private final SseEmitterService sseEmitterService;

    /**
     * 알림 목록 조회
     */
    @Operation(
            summary = "본사 알림 목록 조회",
            description = "Slice 페이징. type/isRead 필터 (선택). 둘 다 없으면 전체 조회."
    )
    @GetMapping("/list")
    public ResponseEntity<BaseResponse> getNotifications(
            @Parameter(description = "알림 타입 필터")
            @RequestParam(required = false) NotificationType type,
            @Parameter(description = "읽음 상태 필터 (true=읽음, false=미읽음)")
            @RequestParam(required = false) Boolean isRead,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        Slice<HeadNotificationDto.ListRes> result;
        if (type != null) {
            result = headNotificationService.getNotificationsByType(type, page, size);
        } else if (isRead != null) {
            result = headNotificationService.getNotificationsByReadStatus(isRead, page, size);
        } else {
            result = headNotificationService.getNotifications(page, size);
        }
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 미읽음 알림 개수 조회
     */
    @Operation(
            summary = "본사 미읽음 알림 개수",
            description = "본사 사용자의 미읽음 알림 개수 (헤더 뱃지용)."
    )
    @GetMapping("/unread/count")
    public ResponseEntity<BaseResponse> getUnreadCount() {
        HeadNotificationDto.UnreadCountRes result = headNotificationService.getUnreadCount();
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 단건 읽음 처리
     */
    @Operation(
            summary = "본사 알림 단건 읽음",
            description = "특정 알림을 읽음 처리."
    )
    @PutMapping("/{notificationIdx}/read")
    public ResponseEntity<BaseResponse> markAsRead(
            @Parameter(
                    description = "알림 고유 ID",
                    examples = {
                            @ExampleObject(name = "정상 (존재하는 알림)", value = "1"),
                            @ExampleObject(name = "NOT_FOUND_NOTIFICATION (없는 알림)", value = "99999")
                    }
            )
            @PathVariable Long notificationIdx
    ) {
        headNotificationService.markAsRead(notificationIdx);
        return ResponseEntity.ok(BaseResponse.success("success"));
    }

    /**
     * 전체 읽음 처리
     */
    @Operation(
            summary = "본사 알림 전체 읽음",
            description = "본사 사용자의 모든 알림 일괄 읽음 처리."
    )
    @PutMapping("/read/all")
    public ResponseEntity<BaseResponse> markAllAsRead() {
        headNotificationService.markAllAsRead();
        return ResponseEntity.ok(BaseResponse.success("success"));
    }

    /**
     * SSE 구독 (실시간 알림 수신)
     */
    @Operation(
            summary = "본사 SSE 구독",
            description = "Server-Sent Events 연결. 새 알림 발생 시 자동 푸시."
    )
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return sseEmitterService.subscribe();
    }
}
