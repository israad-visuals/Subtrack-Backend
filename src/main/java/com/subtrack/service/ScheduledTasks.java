package com.subtrack.service;

import com.subtrack.model.Subscription;
import com.subtrack.model.User;
import com.subtrack.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final SubscriptionRepository subscriptionRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendRenewalReminders() {

        LocalDate today = LocalDate.now();
        LocalDate threeDaysFromNow = today.plusDays(3);

        List<Subscription> dueSoon =
                subscriptionRepository
                        .findByNextBillingDateBetween(
                                today, threeDaysFromNow);

        for (Subscription sub : dueSoon) {

            if (!sub.getIsActive()) continue;

            User user = sub.getUser();

            long daysUntil = ChronoUnit.DAYS.between(
                    today, sub.getNextBillingDate());

            try {
                emailService.sendReminderEmail(
                        user.getEmail(),
                        sub.getServiceName(),
                        String.valueOf(daysUntil),
                        "$" + sub.getCost().toString()
                );
            } catch (Exception e) {
                System.out.println(
                        "Failed to send reminder to "
                                + user.getEmail()
                                + ": " + e.getMessage());
            }
        }
    }
}