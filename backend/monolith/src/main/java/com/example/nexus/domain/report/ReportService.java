package com.example.nexus.domain.report;

import com.example.nexus.common.exception.BaseException;
import com.example.nexus.domain.pos.PosOrdersItemRepository;
import com.example.nexus.domain.pos.PosPayRepository;
import com.example.nexus.domain.report.model.Report;
import com.example.nexus.domain.report.model.ReportDto;
import com.example.nexus.domain.report.tools.AiFinanceTools;
import com.example.nexus.domain.report.tools.AiNewsTools;
import com.example.nexus.domain.report.tools.AiOpsTools;
import com.example.nexus.domain.report.tools.AiOrderTools;
import com.example.nexus.domain.report.tools.AiStatsTools;
import com.example.nexus.domain.report.tools.AiStoreTools;
import com.example.nexus.domain.user.UserRepository;
import com.example.nexus.domain.user.model.User;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.example.nexus.common.model.BaseResponseStatus.NOT_FOUND_USER;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final PosPayRepository posPayRepository;
    private final PosOrdersItemRepository posOrdersItemRepository;
    private final UserRepository userRepository;

    private final ChatClient chatClient;
    private final S3Client s3Client;
    private final AiStatsTools aiStatsTools;   // 매출/메뉴/매장 데이터 조회 도구
    private final AiOrderTools aiOrderTools;   // 발주 데이터 조회 도구
    private final AiOpsTools aiOpsTools;       // 재고/폐기/배송(공급망·운영) 데이터 조회 도구
    private final AiFinanceTools aiFinanceTools; // 본사 청구/정산 데이터 조회 도구
    private final AiStoreTools aiStoreTools;     // 가맹점 수/입폐점 추이 데이터 조회 도구
    private final AiNewsTools aiNewsTools;       // 뉴스 요약 데이터 조회 도구

    @Value("classpath:aiReport/prompts/report-template.md")
    private Resource reportTemplate;   // 보고서 요청용(보고서 양식 포함, 김)

    @Value("classpath:aiReport/prompts/chat-template.md")
    private Resource chatTemplate;     // 일반 채팅용(짧음, 토큰 절감)

    @Value("${spring.cloud.aws.s3.report-bucket}")
    private String reportBucket;

    @Value("${spring.cloud.aws.s3.enabled:true}")
    private boolean isS3Enabled;


    // 0. 생성자 주입
    public ReportService(ReportRepository reportRepository,
                         PosPayRepository posPayRepository,
                         PosOrdersItemRepository posOrdersItemRepository,
                         UserRepository userRepository,
                         ChatClient.Builder chatClientBuilder,
                         ChatMemory chatMemory,
                         AiStatsTools aiStatsTools,
                         AiOrderTools aiOrderTools,
                         AiOpsTools aiOpsTools,
                         AiFinanceTools aiFinanceTools,
                         AiStoreTools aiStoreTools,
                         AiNewsTools aiNewsTools,
                         S3Client s3Client) {
        this.reportRepository = reportRepository;
        this.posPayRepository = posPayRepository;
        this.posOrdersItemRepository = posOrdersItemRepository;
        this.userRepository = userRepository;
        this.s3Client = s3Client;
        this.aiStatsTools = aiStatsTools;
        this.aiOrderTools = aiOrderTools;
        this.aiOpsTools = aiOpsTools;
        this.aiFinanceTools = aiFinanceTools;
        this.aiStoreTools = aiStoreTools;
        this.aiNewsTools = aiNewsTools;
        // 대화 기억 어드바이저를 기본 등록 → 같은 conversationId의 이전 대화를 자동 재주입
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    // 1. 메인 로직 (트랜잭션 분리 및 안전한 예외 처리)
    public String createAndSaveReport(Long userIdx, String userMessage, String sessionId) {
        User loginUser = userRepository.findById(userIdx)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));

        // 세션ID가 있으면 그것을, 없으면 사용자별 기본 대화 ID를 기준으로 사용
        String baseId = (sessionId != null && !sessionId.isBlank())
                ? sessionId : "user-" + userIdx;

        // 보고서 의도(키워드)면 ':report' 통으로 분리 → 거대 보고서가 채팅 메모리를 오염시키지 않게
        String lower = userMessage.toLowerCase();
        boolean reportIntent = userMessage.contains("보고서") || userMessage.contains("리포트") || lower.contains("report");
        String conversationId = reportIntent ? baseId + ":report" : baseId;

        // AI 답변 받기 (대화 컨텍스트 유지 + reportIntent로 프롬프트 분기)
        String aiResponse = handleChatbotRequest(userMessage, conversationId, reportIntent);

        // A안: 보고서는 '명시적 요청(reportIntent)' + 'AI가 [REPORT] 판단' 둘 다일 때만 생성.
        // 키워드가 없거나 AI가 [CHAT]이면 PDF를 만들지 않고 채팅 답변으로 반환 → 의도치 않은 보고서 생성 차단
        boolean wantsReport = reportIntent && !aiResponse.contains("[CHAT]");
        if (!wantsReport) {
            return aiResponse.replaceAll("\\[CHAT\\]|\\[REPORT\\]|\\[TITLE:.*?\\]", "").trim();
        }

        // 제목 추출 로직
        String title = "AI 분석 보고서";
        if (aiResponse.contains("[TITLE:")) {
            int start = aiResponse.indexOf("[TITLE:") + 7;
            int end = aiResponse.indexOf("]", start);
            if (end != -1) {
                title = aiResponse.substring(start, end).trim();
            }
        }

        String aiMarkdown = aiResponse.replaceAll("\\[REPORT\\]|\\[TITLE:.*?\\]", "").trim();

        // 임시 PDF 파일 생성
        File tempFile = generatePdfFromAiResponse(aiMarkdown);

        try {
            // S3 업로드 및 DB 저장을 수동 롤백 로직으로 안전하게 처리
            return saveReportResult(loginUser, title, tempFile);
        } catch (Exception e) {
            throw new RuntimeException("보고서 생성 및 저장 실패: " + e.getMessage(), e);
        } finally {
            // 디스크 용량 확보를 위해 임시 파일은 무조건 삭제
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    // 2. DB 저장 및 S3 업로드 (수동 롤백 방식 - 트랜잭션 에러 완벽 해결)
    public String saveReportResult(User user, String title, File tempFile) {
        String s3Key = "reports/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
                + "/" + UUID.randomUUID() + ".pdf";

        // 2. 환경별 스위치 검사 (개발 편의성)
        if (!isS3Enabled) {
            System.out.println("[DEV MODE] S3 업로드를 건너뛰고 가짜 경로로 DB에 저장합니다.");
            // S3 업로드 없이 바로 DB 저장 로직으로 넘어감
            reportRepository.save(Report.builder()
                    .title(title)
                    .filePath("dev-mode-dummy/" + s3Key) // 가짜 경로 저장
                    .user(user)
                    .build());
            return title + " 보고서 생성이 완료되었습니다! (DEV MODE)";
        }

        // 1. S3에 PDF 파일 먼저 업로드
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(reportBucket)
                        .key(s3Key)
                        .contentType("application/pdf")
                        .build(),
                RequestBody.fromFile(tempFile));

        // 2. DB 저장 시도 및 수동 롤백
        try {
            reportRepository.save(Report.builder().title(title).filePath(s3Key).user(user).build());
            return title + " 보고서 생성이 완료되었습니다!";
        } catch (Exception e) {
            // DB 저장 실패 시: S3에 방금 올린 파일 즉시 삭제 (롤백)
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(reportBucket).key(s3Key).build());
            throw new RuntimeException("DB 저장 중 오류가 발생하여 S3 업로드 파일을 삭제(롤백)했습니다.", e);
        }
    }

    // 3. AI에게 프롬프트 전송 (대화 기억 + 데이터 조회 도구)
    public String handleChatbotRequest(String userMessage, String conversationId, boolean reportIntent) {
        try {
            // 보고서 요청이면 전체 양식 포함 프롬프트, 일반 질문이면 짧은 채팅 프롬프트(토큰 절감)
            Resource tpl = reportIntent ? reportTemplate : chatTemplate;
            // 상대 표현("이번 달/이전" 등) 해석을 위해 오늘 날짜 주입
            String systemText = tpl.getContentAsString(java.nio.charset.StandardCharsets.UTF_8)
                    .replace("{{today}}", java.time.LocalDate.now().toString());

            // system: 규칙·오늘 날짜 / user: 이번 질문 / tools: AI가 필요 시 호출하는 데이터 조회 도구
            // advisors: 같은 conversationId의 최근 10턴을 컨텍스트로 재주입
            return chatClient.prompt()
                    .system(systemText)
                    .user(userMessage)
                    .tools(aiStatsTools, aiOrderTools, aiOpsTools, aiFinanceTools, aiStoreTools, aiNewsTools)
                    .advisors(a -> a
                            .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                            .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                    .call()
                    .content();

        } catch (java.io.IOException e) {
            throw new RuntimeException("프롬프트 템플릿 파일을 읽는 중 오류가 발생했습니다.", e);
        }
    }

    // 4. DB 수치 집계 (최근 30일)
    public ReportDto.ReportDataSummaryDto getRecentSummary() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate  = now.minusDays(30);

        Long totalSales = posPayRepository.sumSalesByDateBetween(startDate, now);
        if (totalSales == null) totalSales = 0L;

        List<String> topProducts = posOrdersItemRepository.findTopSellingMenus(startDate, now, PageRequest.of(0, 5));

        return ReportDto.ReportDataSummaryDto.builder()
                .totalSales(totalSales)
                .topProducts(topProducts)
                .build();
    }

    // 5. 마크다운 -> HTML -> PDF 변환 (JAR 폰트 호환성 및 나눔고딕 적용)
    private File generatePdfFromAiResponse(String aiMarkdown) {
        try {
            String htmlContent = convertMarkdownToHtml(aiMarkdown);

            // 보고서 전용 나눔고딕 CSS 적용
            String fullHtml = "<html><head><style>" +
                    "body { font-family: 'NanumGothic', sans-serif; font-size: 12px; line-height: 1.6; padding: 30px; color: #333; }" +
                    "h1 { font-size: 18px; color: #F37321; border-bottom: 2px solid #F37321; padding-bottom: 8px; margin-bottom: 20px; }" +
                    "h2 { font-size: 14px; color: #222; margin-top: 25px; margin-bottom: 10px; border-bottom: 1px solid #eee; padding-bottom: 5px; }" +
                    "h3 { font-size: 13px; color: #444; margin-top: 15px; margin-bottom: 5px; }" +
                    "p, li { font-size: 12px; color: #555; }" +
                    "blockquote { background: #f9f9f9; border-left: 4px solid #F37321; margin: 10px 0; padding: 12px 15px; color: #666; font-weight: bold; }" +
                    "table { width: 100%; border-collapse: collapse; margin-top: 10px; margin-bottom: 20px; }" +
                    "th, td { border: 1px solid #ddd; padding: 10px; text-align: left; font-size: 11px; }" +
                    "th { background-color: #F37321; color: white; font-weight: bold; }" +
                    "ul { margin-top: 5px; padding-left: 20px; }" +
                    "</style></head><body>" +
                    htmlContent +
                    "</body></html>";

            // 시스템 임시 폴더 사용 (서버 배포 시 권한 문제 방지)
            String tempDir = System.getProperty("java.io.tmpdir");
            File tempFile = new File(tempDir, "temp_report_" + UUID.randomUUID() + ".pdf");

            // 리소스 폰트 로드 (나눔고딕 경로 유지)
            Resource fontResource = new ClassPathResource("aiReport/fonts/NanumGothic.ttf");

            try (OutputStream os = new FileOutputStream(tempFile)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(fullHtml, null);

                // JAR 패키징 대응: File 객체 대신 Stream 방식으로 폰트 주입
                builder.useFont(() -> {
                    try {
                        return fontResource.getInputStream();
                    } catch (Exception ex) {
                        throw new RuntimeException("폰트 파일을 읽을 수 없습니다.", ex);
                    }
                }, "NanumGothic"); // 🌟 나눔고딕으로 변경

                builder.toStream(os);
                builder.run();
            }

            return tempFile;

        } catch (Exception e) {
            throw new RuntimeException("임시 PDF 파일 생성 중 오류 발생: " + e.getMessage(), e);
        }
    }

    // 6. 마크다운 변환기
    private String convertMarkdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    // 7. 사용자별 보고서 리스트 조회
    @Transactional(readOnly = true)
    public ReportDto.ReportPageRes reportList(Long userIdx, int page, int size) {
        User user = userRepository.findById(userIdx)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Report> result = reportRepository.findByUserOrderByCreatedAtDesc(user, pageRequest);

        return ReportDto.ReportPageRes.from(result);
    }
}