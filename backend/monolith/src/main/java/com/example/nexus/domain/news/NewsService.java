package com.example.nexus.domain.news;

import com.example.nexus.common.enums.NewsCollectCategory;
import com.example.nexus.common.enums.NewsCollectTarget;
import com.example.nexus.common.exception.BaseException;
import com.example.nexus.common.model.BaseResponseStatus;
import com.example.nexus.domain.news.model.News;
import com.example.nexus.domain.news.model.NewsDto;
import com.example.nexus.domain.store.StoreRepository;
import com.example.nexus.domain.store.model.Store;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final String BRAND = "더벤티";

    private final NewsRepository newsRepository;
    private final StoreRepository storeRepository;
    private final ChatClient.Builder chatClientBuilder;
    private final Optional<ToolCallbackProvider> toolCallbackProvider;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<NewsDto.SummaryListItem> listSummaries(LocalDate date, Long storeIdx) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        List<News> rows = storeIdx == null
                ? newsRepository.findByTargetAndStoreIsNullAndSummaryDateBetweenOrderBySummaryDateDesc(
                        NewsCollectTarget.HQ, start, end)
                : newsRepository.findByTargetAndStore_IdxAndSummaryDateBetweenOrderBySummaryDateDesc(
                        NewsCollectTarget.STORE, storeIdx, start, end);
        return rows.stream().map(this::toListItem).toList();
    }

    @Transactional(readOnly = true)
    public NewsDto.SummaryDetail getSummary(Long idx) {
        News news = newsRepository.findById(idx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_DATA));
        return toDetail(news);
    }

    public NewsDto.CollectRunResult runDailyPipeline() {
        if (toolCallbackProvider.isEmpty()) {
            log.warn("MCP ToolCallbackProvider를 찾을 수 없습니다. 뉴스 수집을 건너뜁니다.");
            return NewsDto.CollectRunResult.builder()
                    .hqInserted(0)
                    .storeInserted(0)
                    .summariesInserted(0)
                    .message("MCP 서버가 설정되지 않아 뉴스 수집을 건너뜁니다.")
                    .build();
        }

        LocalDate today = LocalDate.now(KST);
        ChatClient chatClient = chatClientBuilder.build();

        int hqCount = runForScope(today, null, chatClient);
        int storeCount = 0;

        for (Store store : storeRepository.findAll()) {
            if (!store.isDeleted()) {
                storeCount += runForScope(today, store, chatClient);
            }
        }

        int total = hqCount + storeCount;
        return NewsDto.CollectRunResult.builder()
                .hqInserted(hqCount)
                .storeInserted(storeCount)
                .summariesInserted(total)
                .message(String.format("요약 %d건 생성 완료 (본사: %d, 매장: %d)", total, hqCount, storeCount))
                .build();
    }

    private int runForScope(LocalDate date, Store storeOrNull, ChatClient chatClient) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        NewsCollectTarget target = storeOrNull == null ? NewsCollectTarget.HQ : NewsCollectTarget.STORE;

        // 당일 요약이 이미 있으면 건너뜀
        List<News> existing = storeOrNull == null
                ? newsRepository.findByTargetAndStoreIsNullAndSummaryDateBetweenOrderBySummaryDateDesc(target, start, end)
                : newsRepository.findByTargetAndStore_IdxAndSummaryDateBetweenOrderBySummaryDateDesc(
                        target, storeOrNull.getIdx(), start, end);
        if (!existing.isEmpty()) {
            return 0;
        }

        String prompt = buildPrompt(storeOrNull);

        String aiResponse;
        try {
            aiResponse = chatClient.prompt()
                    .user(prompt)
                    .tools(toolCallbackProvider.get().getToolCallbacks())
                    .call()
                    .content();
        } catch (Exception e) {
            log.warn("뉴스 요약 AI 호출 실패 (scope={}): {}",
                    storeOrNull != null ? storeOrNull.getStoreName() : "HQ", e.getMessage());
            return 0;
        }

        return saveNewsSummaries(aiResponse, storeOrNull, target);
    }

    private String buildPrompt(Store storeOrNull) {
        boolean isHq = storeOrNull == null;
        String region = isHq ? null : extractRegion(storeOrNull.getAddress());
        String neighborhood = isHq ? null : extractNeighborhood(storeOrNull.getAddress());

        String keywords = isHq
                ? String.format(
                        "- '오늘 날씨'\n- '전국 날씨'\n- '%s'\n- '%s 카페'\n- '커피 프랜차이즈 뉴스'\n- '카페 업계 동향'",
                        BRAND, BRAND)
                : String.format(
                        "- '오늘 날씨'\n- '%s 날씨'\n- '%s 날씨'\n- '%s 행사'\n- '%s 축제'\n- '%s 교통'\n- '%s'\n- '커피 프랜차이즈 뉴스'\n- '카페 업계 동향'",
                        region, neighborhood, region, region, region, BRAND);

        String scopeDescription = isHq
                ? BRAND + " 카페 프랜차이즈 본사"
                : storeOrNull.getStoreName() + " (" + storeOrNull.getAddress() + ")";

        return String.format("""
                당신은 카페 프랜차이즈 점주를 돕는 뉴스 분석 전문가입니다.
                search_naver_news 도구를 사용하여 아래 키워드로 뉴스를 검색하세요.
                검색된 결과 중 매장 운영에 도움이 될 이슈를 요약해 주세요.

                [대상] %s

                [검색 키워드 - 순서대로 검색]
                %s

                [출력 규칙]
                - 날씨/기상 예보는 반드시 1개 포함 (category: WEATHER) — 검색 결과가 부족해도 날씨는 꼭 포함
                - 지역 행사/축제/공연이 있으면 포함 (category: LOCAL_EVENT)
                - 교통/도로 혼잡 정보가 있으면 포함 (category: TRAFFIC)
                - 안전/재난 위험이 있으면 포함 (category: RISK)
                - 연예·정치 스캔들은 제외
                - 최소 1개, 최대 5개 반환 (날씨 항목은 항상 포함)

                반드시 아래 JSON만 출력하세요. 다른 텍스트 없이 JSON만:
                {"items":[{"title":"한 줄 제목","summary":"2~4문장 요약","category":"WEATHER|LOCAL_EVENT|TRAFFIC|RISK","url":"원문 URL"}]}
                """, scopeDescription, keywords);
    }

    private int saveNewsSummaries(String aiResponse, Store storeOrNull, NewsCollectTarget target) {
        try {
            String json = extractJson(aiResponse);
            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root.path("items");

            if (!items.isArray() || items.isEmpty()) {
                log.info("AI 판단: 선별된 뉴스 없음 (scope={})", storeOrNull != null ? storeOrNull.getStoreName() : "HQ");
                return 0;
            }

            LocalDateTime now = LocalDateTime.now(KST);
            List<News> toSave = new ArrayList<>();

            for (JsonNode item : items) {
                String title = item.path("title").asText("").trim();
                String summary = item.path("summary").asText("").trim();
                String url = item.path("url").asText("").trim();
                NewsCollectCategory category = parseCategory(item.path("category").asText("LOCAL_EVENT"));

                if (!StringUtils.hasText(summary)) continue;

                News news = new News();
                news.setSummaryDate(now);
                news.setTarget(target);
                news.setSummaryTitle(StringUtils.hasText(title) ? title : "일일 이슈 요약");
                news.setSummaryContents(summary);
                news.setUrl(StringUtils.hasText(url) ? url : "-");
                news.setCategory(category);
                news.setStore(storeOrNull);
                toSave.add(news);
            }

            newsRepository.saveAll(toSave);
            log.info("뉴스 요약 저장 완료: scope={}, count={}",
                    storeOrNull != null ? storeOrNull.getStoreName() : "HQ", toSave.size());
            return toSave.size();

        } catch (Exception e) {
            log.error("뉴스 요약 JSON 파싱 실패: {}", aiResponse, e);
            return 0;
        }
    }

    private static String extractJson(String response) {
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start == -1 || end == -1 || end <= start) {
            return "{\"items\":[]}";
        }
        return response.substring(start, end + 1);
    }

    private static NewsCollectCategory parseCategory(String raw) {
        if (!StringUtils.hasText(raw)) return NewsCollectCategory.LOCAL_EVENT;
        try {
            return NewsCollectCategory.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return NewsCollectCategory.LOCAL_EVENT;
        }
    }

    private static String extractRegion(String address) {
        if (!StringUtils.hasText(address)) return "";
        String[] parts = address.trim().split("\\s+");
        // 경기도/충청남도 등 도(道) 지역: 도 prefix 제거 후 시+구 반환
        if (parts.length >= 3 && parts[0].endsWith("도")) {
            return parts[1] + " " + parts[2];
        }
        return parts.length >= 2 ? parts[0] + " " + parts[1] : parts[0];
    }

    private static String extractNeighborhood(String address) {
        if (!StringUtils.hasText(address)) return "";
        String[] parts = address.trim().split("\\s+");
        for (String part : parts) {
            if (part.endsWith("동") || part.endsWith("읍") || part.endsWith("면")
                    || part.endsWith("로") || part.endsWith("길")) {
                return part;
            }
        }
        return extractRegion(address);
    }

    private NewsDto.SummaryListItem toListItem(News news) {
        String contents = news.getSummaryContents() != null ? news.getSummaryContents() : "";
        String preview = contents.length() > 160 ? contents.substring(0, 160) + "…" : contents;
        return NewsDto.SummaryListItem.builder()
                .idx(news.getIdx())
                .summaryDate(news.getSummaryDate())
                .target(news.getTarget())
                .storeIdx(news.getStore() != null ? news.getStore().getIdx() : null)
                .summaryTitle(news.getSummaryTitle())
                .summaryContentsPreview(preview)
                .category(news.getCategory())
                .url(news.getUrl())
                .build();
    }

    private NewsDto.SummaryDetail toDetail(News news) {
        return NewsDto.SummaryDetail.builder()
                .idx(news.getIdx())
                .summaryDate(news.getSummaryDate())
                .target(news.getTarget())
                .storeIdx(news.getStore() != null ? news.getStore().getIdx() : null)
                .summaryTitle(news.getSummaryTitle())
                .summaryContents(news.getSummaryContents())
                .category(news.getCategory())
                .url(news.getUrl())
                .build();
    }
}
