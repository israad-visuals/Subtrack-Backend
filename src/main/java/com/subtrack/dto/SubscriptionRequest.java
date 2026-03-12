package com.subtrack.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class SubscriptionRequest {
    @NotBlank(message = "Service name is required")
    private String serviceName;
    @NotNull(message = "Cost is required")
    @Positive(message = "Cost must be greater than zero")
    private BigDecimal cost;
    @NotBlank(message = "Billing cycle is required")
    private String billingCycle;
    @NotBlank(message = "Category is required")
    private String category;
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    @NotNull(message = "Is trial must be specified")
    private Boolean isTrial;
    private LocalDate trialEndDate;
    private String notes;
}