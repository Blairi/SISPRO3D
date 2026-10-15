package com.sispro3d.unam.report.service.impl;

import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.quote.domain.QuoteStatus;
import com.sispro3d.unam.quote.dto.QuoteResponse;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.report.dto.ActivitySummary;
import com.sispro3d.unam.report.dto.ExpertPerformance;
import com.sispro3d.unam.report.dto.MostRequestedService;
import com.sispro3d.unam.report.dto.ReportDateRange;
import com.sispro3d.unam.report.service.AdminReportService;
import com.sispro3d.unam.review.dto.ReviewResponse;
import com.sispro3d.unam.review.service.ReviewService;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.dto.AccountResponse;
import com.sispro3d.unam.user.service.AccountService;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import com.sispro3d.unam.workorder.dto.WorkOrderResponse;
import com.sispro3d.unam.workorder.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminReportServiceImpl implements AdminReportService {

    @Autowired
    private OfferedServiceService offeredServiceService;
    @Autowired
    private QuoteService quoteService;
    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private AccountService accountService;

    @Override
    public List<MostRequestedService> mostRequestedServices(ReportDateRange range) {
        List<OfferedServiceResponse> services = servicesInRange(range);
        List<QuoteResponse> quotes = quotesInRange(range);
        List<WorkOrderResponse> orders = ordersInRange(range);

        Map<Long, Long> quotesByService = quotes.stream()
                .filter(q -> q.getOfferedServiceId() != null)
                .collect(Collectors.groupingBy(QuoteResponse::getOfferedServiceId, Collectors.counting()));
        Map<Long, Long> ordersByService = orders.stream()
                .filter(o -> o.getOfferedServiceId() != null)
                .collect(Collectors.groupingBy(WorkOrderResponse::getOfferedServiceId, Collectors.counting()));

        return services.stream()
                .map(service -> new MostRequestedService(
                        service.getId(),
                        service.getTitle(),
                        service.getCategoryName(),
                        service.getExpertName(),
                        quotesByService.getOrDefault(service.getId(), 0L),
                        ordersByService.getOrDefault(service.getId(), 0L)))
                .filter(row -> row.quoteCount() > 0)
                .sorted(Comparator.comparingLong(MostRequestedService::quoteCount).reversed()
                        .thenComparing(MostRequestedService::title, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    @Override
    public List<ExpertPerformance> expertPerformance(ReportDateRange range) {
        List<OfferedServiceResponse> services = servicesInRange(range);
        List<QuoteResponse> quotes = quotesInRange(range);
        List<WorkOrderResponse> orders = ordersInRange(range);
        List<ReviewResponse> reviews = reviewsInRange(range);

        Map<Long, Long> expertByServiceId = services.stream()
                .filter(s -> s.getExpertId() != null)
                .collect(Collectors.toMap(OfferedServiceResponse::getId, OfferedServiceResponse::getExpertId));

        Map<Long, List<OfferedServiceResponse>> servicesByExpert = services.stream()
                .filter(s -> s.getExpertId() != null)
                .collect(Collectors.groupingBy(OfferedServiceResponse::getExpertId));

        Map<Long, Long> answeredQuotes = quotes.stream()
                .filter(q -> q.getOfferedServiceId() != null && q.getTotalAmount() != null)
                .map(q -> expertByServiceId.get(q.getOfferedServiceId()))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Map<Long, Map<String, Long>> ordersByExpert = new LinkedHashMap<>();
        for (WorkOrderResponse order : orders) {
            if (order.getOfferedServiceId() == null) {
                continue;
            }
            Long expertId = expertByServiceId.get(order.getOfferedServiceId());
            if (expertId == null) {
                continue;
            }
            Map<String, Long> byStatus = ordersByExpert.computeIfAbsent(expertId, k -> emptyStatusCounts());
            String status = order.getStatus() != null ? order.getStatus().name() : null;
            if (status != null && byStatus.containsKey(status)) {
                byStatus.merge(status, 1L, Long::sum);
            }
        }

        Map<Long, List<Integer>> ratingsByExpert = new LinkedHashMap<>();
        for (ReviewResponse review : reviews) {
            if (review.getOfferedServiceId() == null) {
                continue;
            }
            Long expertId = expertByServiceId.get(review.getOfferedServiceId());
            if (expertId == null || review.getRating() == null) {
                continue;
            }
            ratingsByExpert.computeIfAbsent(expertId, k -> new ArrayList<>()).add(review.getRating());
        }

        return accountService.findByRole(Role.EXPERT).stream()
                .filter(account -> range.includes(account.getCreatedAt()))
                .filter(account -> servicesByExpert.containsKey(account.getIdUser()))
                .map(account -> {
                    List<Integer> ratings = ratingsByExpert.getOrDefault(account.getIdUser(), List.of());
                    Map<String, Long> byStatus = ordersByExpert.getOrDefault(account.getIdUser(), emptyStatusCounts());
                    return new ExpertPerformance(
                            account.getIdUser(),
                            fullName(account),
                            servicesByExpert.get(account.getIdUser()).size(),
                            answeredQuotes.getOrDefault(account.getIdUser(), 0L),
                            byStatus.getOrDefault(WorkOrderStatus.COMPLETED.name(), 0L),
                            byStatus,
                            ratings.isEmpty() ? null : roundToOneDecimal(ratings),
                            ratings.size());
                })
                .sorted(Comparator.comparing(ExpertPerformance::expertName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    @Override
    public ActivitySummary activitySummary(ReportDateRange range) {
        return new ActivitySummary(
                countBy(accountService.findAll().stream()
                                .filter(a -> range.includes(a.getCreatedAt()))
                                .toList(),
                        a -> a.getRole() != null ? a.getRole().name() : null,
                        names(Role.values())),
                countBy(servicesInRange(range),
                        s -> s.getStatus() != null ? s.getStatus().name() : null,
                        names(ServiceStatus.values())),
                countBy(quotesInRange(range),
                        q -> q.getStatus() != null ? q.getStatus().name() : null,
                        names(QuoteStatus.values())),
                countBy(ordersInRange(range),
                        o -> o.getStatus() != null ? o.getStatus().name() : null,
                        names(WorkOrderStatus.values())),
                reviewsInRange(range).size());
    }

    private List<OfferedServiceResponse> servicesInRange(ReportDateRange range) {
        return offeredServiceService.findAll().stream()
                .filter(s -> range.includes(s.getCreatedAt()))
                .toList();
    }

    private List<QuoteResponse> quotesInRange(ReportDateRange range) {
        return quoteService.findAll().stream()
                .filter(q -> range.includes(q.getCreatedAt()))
                .toList();
    }

    private List<WorkOrderResponse> ordersInRange(ReportDateRange range) {
        return workOrderService.findAll().stream()
                .filter(o -> range.includes(o.getCreatedAt()))
                .toList();
    }

    private List<ReviewResponse> reviewsInRange(ReportDateRange range) {
        return reviewService.findAll().stream()
                .filter(r -> range.includes(r.getCreatedAt()))
                .toList();
    }

    private Map<String, Long> emptyStatusCounts() {
        return countBy(List.of(), item -> null, names(WorkOrderStatus.values()));
    }

    private static String[] names(Enum<?>[] values) {
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].name();
        }
        return names;
    }

    private static <T> Map<String, Long> countBy(List<T> items, Function<T, String> keyFn, String[] keys) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String key : keys) {
            counts.put(key, 0L);
        }
        for (T item : items) {
            String key = keyFn.apply(item);
            if (key != null && counts.containsKey(key)) {
                counts.merge(key, 1L, Long::sum);
            }
        }
        return counts;
    }

    private static String fullName(AccountResponse account) {
        if (account.getLastName() == null || account.getLastName().isBlank()) {
            return account.getName();
        }
        return account.getName() + " " + account.getLastName();
    }

    private static double roundToOneDecimal(List<Integer> ratings) {
        double average = ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        return Math.round(average * 10.0) / 10.0;
    }
}
