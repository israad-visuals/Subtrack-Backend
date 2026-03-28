package com.subtrack.controller;

import com.subtrack.dto.SubscriptionRequest;
import com.subtrack.dto.SubscriptionResponse;
import com.subtrack.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping("/{userId}")
    public ResponseEntity<SubscriptionResponse> add(
            @PathVariable Long userId,
            @Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subscriptionService
                        .addSubscription(userId, request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<SubscriptionResponse>> getAll(
            @PathVariable Long userId) {
        return ResponseEntity.ok(subscriptionService
                .getActiveSubscriptions(userId));
    }

    @PutMapping("/{subscriptionId}")
    public ResponseEntity<SubscriptionResponse> update(
            @PathVariable Long subscriptionId,
            @Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService
                .updateSubscription(subscriptionId, request));
    }

    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long subscriptionId) {
        subscriptionService.deleteSubscription(
                subscriptionId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{subscriptionId}/cancel")
    public ResponseEntity<SubscriptionResponse> cancel(
            @PathVariable Long subscriptionId) {
        return ResponseEntity.ok(subscriptionService
                .cancelSubscription(subscriptionId));
    }
    @GetMapping("/{userId}/cancelled")
    public ResponseEntity<List<SubscriptionResponse>> getCancelled(
            @PathVariable Long userId) {
        return ResponseEntity.ok(subscriptionService
                .getCancelledSubscriptions(userId));
    }
}
