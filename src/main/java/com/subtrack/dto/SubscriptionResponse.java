package com.subtrack.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private Long id;
    private String serviceName;
    private BigDecimal cost;
    private String billingCycle;
    private String category;
    private LocalDate startDate;
    private LocalDate nextBillingDate;
    private Boolean isTrial;
    private LocalDate trialEndDate;
    private String notes;
    private Boolean isActive;
    private String status;
    private long daysUntilDue;
}
