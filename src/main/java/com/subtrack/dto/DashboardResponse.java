package com.subtrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private BigDecimal monthlyBurnRate;
    private int activeSubscriptionCount;
    private int freeTrialCount;
    private int paymentsDueThisWeek;
    private BigDecimal savedFromCancelled;
    private String userName;
}