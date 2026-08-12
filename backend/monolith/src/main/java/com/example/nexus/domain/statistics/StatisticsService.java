package com.example.nexus.domain.statistics;

import com.example.nexus.common.enums.PosPayMethod;
import com.example.nexus.domain.statistics.model.StatisticsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final StatisticsRepository statisticsRepository;

    public StatisticsDto.TodaySalesRes getTodaySales() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        long total = statisticsRepository.sumTodaySales(start, end);
        return StatisticsDto.TodaySalesRes.builder().todaySales(total).build();
    }

    public StatisticsDto.RankingRes getTopStores() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        List<Object[]> rows = statisticsRepository.findTopStores(start, end, PageRequest.of(0, 5));
        List<StatisticsDto.RankingItem> items = rows.stream()
                .map(r -> StatisticsDto.RankingItem.builder()
                        .idx((Long) r[0])
                        .name((String) r[1])
                        .score((Long) r[2])
                        .build())
                .toList();
        return StatisticsDto.RankingRes.builder().rankings(items).build();
    }

    public StatisticsDto.RankingRes getTopMenus() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        List<Object[]> rows = statisticsRepository.findTopMenus(start, end, PageRequest.of(0, 5));
        List<StatisticsDto.RankingItem> items = rows.stream()
                .map(r -> StatisticsDto.RankingItem.builder()
                        .idx((Long) r[0])
                        .name((String) r[1])
                        .score((Long) r[2])
                        .build())
                .toList();
        return StatisticsDto.RankingRes.builder().rankings(items).build();
    }

    public StatisticsDto.HourlySalesRes getHourlySales() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        List<Object[]> rows = statisticsRepository.findHourlySales(start, end);
        List<StatisticsDto.HourlySalesItem> items = rows.stream()
                .map(r -> StatisticsDto.HourlySalesItem.builder()
                        .hour((Integer) r[0])
                        .sales((Long) r[1])
                        .build())
                .toList();
        return StatisticsDto.HourlySalesRes.builder().hourlyData(items).build();
    }

    public StatisticsDto.RatioRes getCategoryRatio() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        List<Object[]> rows = statisticsRepository.findSalesByCategory(start, end);
        List<StatisticsDto.RatioItem> items = rows.stream()
                .map(r -> StatisticsDto.RatioItem.builder()
                        .name((String) r[0])
                        .amount((Long) r[1])
                        .build())
                .toList();
        return StatisticsDto.RatioRes.builder().ratios(items).build();
    }

    public StatisticsDto.RatioRes getPaymentMethodRatio() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        List<Object[]> rows = statisticsRepository.findSalesByPayMethod(start, end);
        List<StatisticsDto.RatioItem> items = rows.stream()
                .map(r -> StatisticsDto.RatioItem.builder()
                        .name(((PosPayMethod) r[0]).name())
                        .amount((Long) r[1])
                        .build())
                .toList();
        return StatisticsDto.RatioRes.builder().ratios(items).build();
    }
}
