package com.subtrack.service;
import com.subtrack.dto.SubscriptionRequest;
import com.subtrack.dto.SubscriptionResponse;
import com.subtrack.model.Subscription;
import com.subtrack.model.User;
import com.subtrack.repository.SubscriptionRepository;
import com.subtrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import com.subtrack.dto.DashboardResponse;
@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    private String calculateStatus(Subscription sub) {
        if (sub.getIsTrial()) {
            return "BLUE";
        }
        long daysUntil = ChronoUnit.DAYS.between(
                LocalDate.now(), sub.getNextBillingDate()
        );
        if (daysUntil <= 3) {
            return "RED";
        } else if (daysUntil <= 7) {
            return "YELLOW";
        } else {
            return "GREEN";
        }
    }

    private LocalDate calculateNextBillingDate(
            LocalDate startDate, String billingCycle) {
        LocalDate next = startDate;
        LocalDate today = LocalDate.now();
        while (!next.isAfter(today)) {
            switch (billingCycle.toUpperCase()) {
                case "MONTHLY":
                    next = next.plusMonths(1);
                    break;
                case "YEARLY":
                    next = next.plusYears(1);
                    break;
                case "WEEKLY":
                    next = next.plusWeeks(1);
                    break;
                case "QUARTERLY":
                    next = next.plusMonths(3);
                    break;
                default:
                    next = next.plusMonths(1);
            }
        }
        return next;
    }
    private SubscriptionResponse mapToResponse(Subscription sub) {
        long daysUntil = ChronoUnit.DAYS.between(
                LocalDate.now(), sub.getNextBillingDate()
        );
        return SubscriptionResponse.builder()
                .id(sub.getId())
                .serviceName(sub.getServiceName())
                .cost(sub.getCost())
                .billingCycle(sub.getBillingCycle())
                .category(sub.getCategory())
                .startDate(sub.getStartDate())
                .nextBillingDate(sub.getNextBillingDate())
                .isTrial(sub.getIsTrial())
                .trialEndDate(sub.getTrialEndDate())
                .notes(sub.getNotes())
                .isActive(sub.getIsActive())
                .status(sub.getStatus())
                .daysUntilDue(daysUntil)
                .build();
    }

    public SubscriptionResponse addSubscription(
            Long userId, SubscriptionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found"));
        LocalDate nextBilling = request.getIsTrial()
                ? request.getTrialEndDate()
                : calculateNextBillingDate(
                request.getStartDate(),
                request.getBillingCycle());
        Subscription subscription = Subscription.builder()
                .serviceName(request.getServiceName())
                .cost(request.getIsTrial()
                        ? BigDecimal.ZERO : request.getCost())
                .billingCycle(request.getBillingCycle())
                .category(request.getCategory())
                .startDate(request.getStartDate())
                .nextBillingDate(nextBilling)
                .isTrial(request.getIsTrial())
                .trialEndDate(request.getTrialEndDate())
                .notes(request.getNotes())
                .isActive(true)
                .status("GREEN")
                .user(user)
                .build();
        subscription.setStatus(
                calculateStatus(subscription));
        Subscription saved = subscriptionRepository
                .save(subscription);
        return mapToResponse(saved);
    }

    public List<SubscriptionResponse> getActiveSubscriptions(Long userId) {
        return subscriptionRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .peek(sub -> sub.setStatus(calculateStatus(sub)))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SubscriptionResponse updateSubscription(Long subscriptionId, SubscriptionRequest request) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        sub.setServiceName(request.getServiceName());
        sub.setCost(request.getIsTrial() ? BigDecimal.ZERO : request.getCost());
        sub.setBillingCycle(request.getBillingCycle());
        sub.setCategory(request.getCategory());
        sub.setStartDate(request.getStartDate());
        sub.setIsTrial(request.getIsTrial());
        sub.setTrialEndDate(request.getTrialEndDate());
        sub.setNotes(request.getNotes());

        LocalDate nextBilling = request.getIsTrial()
                ? request.getTrialEndDate()
                : calculateNextBillingDate(request.getStartDate(), request.getBillingCycle());
        sub.setNextBillingDate(nextBilling);
        sub.setStatus(calculateStatus(sub));

        Subscription saved = subscriptionRepository.save(sub);
        return mapToResponse(saved);
    }

    public void deleteSubscription(Long subscriptionId) {
        subscriptionRepository.deleteById(subscriptionId);
    }

    public SubscriptionResponse cancelSubscription(Long subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        sub.setIsActive(false);
        Subscription saved = subscriptionRepository.save(sub);
        return mapToResponse(saved);
    }

    public BigDecimal calculateMonthlyBurnRate(Long userId) {
        List<Subscription> active = subscriptionRepository.findByUserIdAndIsActiveTrue(userId);
        BigDecimal total = BigDecimal.ZERO;
        for (Subscription sub : active) {
            if (sub.getIsTrial()) continue;
            switch (sub.getBillingCycle().toUpperCase()) {
                case "MONTHLY":
                    total = total.add(sub.getCost());
                    break;
                case "YEARLY":
                    total = total.add(sub.getCost().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));
                    break;
                case "WEEKLY":
                    total = total.add(sub.getCost().multiply(BigDecimal.valueOf(4.33)));
                    break;
                case "QUARTERLY":
                    total = total.add(sub.getCost().divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP));
                    break;
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
    public DashboardResponse getDashboardSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found"));

        List<Subscription> active =
                subscriptionRepository
                        .findByUserIdAndIsActiveTrue(userId);

        List<Subscription> cancelled =
                subscriptionRepository
                        .findByUserIdAndIsActiveFalse(userId);

        BigDecimal burnRate =
                calculateMonthlyBurnRate(userId);

        int trialCount = (int) active.stream()
                .filter(Subscription::getIsTrial)
                .count();

        LocalDate today = LocalDate.now();
        LocalDate weekFromNow = today.plusDays(7);
        int dueThisWeek = (int) active.stream()
                .filter(sub -> !sub.getIsTrial())
                .filter(sub ->
                        !sub.getNextBillingDate()
                                .isAfter(weekFromNow)
                                && !sub.getNextBillingDate()
                                .isBefore(today))
                .count();

        BigDecimal savedAmount = cancelled.stream()
                .map(Subscription::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardResponse.builder()
                .monthlyBurnRate(burnRate)
                .activeSubscriptionCount(active.size())
                .freeTrialCount(trialCount)
                .paymentsDueThisWeek(dueThisWeek)
                .savedFromCancelled(savedAmount)
                .userName(user.getFirstName())
                .build();
    }
    public List<SubscriptionResponse> getCancelledSubscriptions(Long userId) {
        return subscriptionRepository
                .findByUserIdAndIsActiveFalse(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}