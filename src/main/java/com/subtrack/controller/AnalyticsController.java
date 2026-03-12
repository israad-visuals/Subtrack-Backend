package com.subtrack.controller;

import com.subtrack.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final SubscriptionService subscriptionService;

    @GetMapping("/burn-rate/{userId}")
    public ResponseEntity<BigDecimal> getBurnRate(
            @PathVariable Long userId) {
        return ResponseEntity.ok(subscriptionService
                .calculateMonthlyBurnRate(userId));
    }
}
