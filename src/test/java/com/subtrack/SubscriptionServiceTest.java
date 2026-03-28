package com.subtrack;

import com.subtrack.dto.SubscriptionRequest;
import com.subtrack.dto.SubscriptionResponse;
import com.subtrack.model.User;
import com.subtrack.repository.SubscriptionRepository;
import com.subtrack.repository.UserRepository;
import com.subtrack.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SubscriptionServiceTest {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Test
    public void testAddSubscription() {
        User user = User.builder()
                .firstName("JUnit")
                .lastName("Tester")
                .email("junit" + System.currentTimeMillis()
                        + "@test.com")
                .password("password123")
                .build();
        userRepository.save(user);

        SubscriptionRequest request = SubscriptionRequest.builder()
                .serviceName("Netflix")
                .cost(new BigDecimal("16.99"))
                .billingCycle("MONTHLY")
                .category("Entertainment")
                .startDate(LocalDate.of(2026, 1, 1))
                .isTrial(false)
                .build();

        SubscriptionResponse response =
                subscriptionService.addSubscription(
                        user.getId(), request);

        assertNotNull(response);
        assertEquals("Netflix", response.getServiceName());
        assertEquals(new BigDecimal("16.99"),
                response.getCost());
        assertEquals("Entertainment",
                response.getCategory());
        assertTrue(response.getIsActive());
        assertNotNull(response.getStatus());
    }
    @Test
    public void testBurnRateCalculation() {
        User user = User.builder()
                .firstName("Burn")
                .lastName("Test")
                .email("burn" + System.currentTimeMillis()
                        + "@test.com")
                .password("password123")
                .build();
        userRepository.save(user);

        SubscriptionRequest netflix = SubscriptionRequest.builder()
                .serviceName("Netflix")
                .cost(new BigDecimal("16.99"))
                .billingCycle("MONTHLY")
                .category("Entertainment")
                .startDate(LocalDate.of(2026, 1, 1))
                .isTrial(false)
                .build();

        SubscriptionRequest spotify = SubscriptionRequest.builder()
                .serviceName("Spotify")
                .cost(new BigDecimal("11.99"))
                .billingCycle("MONTHLY")
                .category("Entertainment")
                .startDate(LocalDate.of(2026, 1, 1))
                .isTrial(false)
                .build();

        subscriptionService.addSubscription(
                user.getId(), netflix);
        subscriptionService.addSubscription(
                user.getId(), spotify);

        BigDecimal burnRate =
                subscriptionService
                        .calculateMonthlyBurnRate(user.getId());

        assertEquals(new BigDecimal("28.98"), burnRate);
    }
    @Test
    public void testCancelSubscription() {
        User user = User.builder()
                .firstName("Cancel")
                .lastName("Test")
                .email("cancel" + System.currentTimeMillis()
                        + "@test.com")
                .password("password123")
                .build();
        userRepository.save(user);

        SubscriptionRequest request = SubscriptionRequest.builder()
                .serviceName("Hulu")
                .cost(new BigDecimal("7.99"))
                .billingCycle("MONTHLY")
                .category("Entertainment")
                .startDate(LocalDate.of(2026, 1, 1))
                .isTrial(false)
                .build();

        SubscriptionResponse added =
                subscriptionService.addSubscription(
                        user.getId(), request);

        assertTrue(added.getIsActive());

        SubscriptionResponse cancelled =
                subscriptionService.cancelSubscription(
                        added.getId());

        assertFalse(cancelled.getIsActive());
    }
    @Test
    public void testTrafficLightStatus() {
        User user = User.builder()
                .firstName("Traffic")
                .lastName("Test")
                .email("traffic" + System.currentTimeMillis()
                        + "@test.com")
                .password("password123")
                .build();
        userRepository.save(user);

        SubscriptionRequest dueSoon = SubscriptionRequest.builder()
                .serviceName("Due Soon Service")
                .cost(new BigDecimal("9.99"))
                .billingCycle("MONTHLY")
                .category("Entertainment")
                .startDate(LocalDate.now().minusDays(28))
                .isTrial(false)
                .build();

        SubscriptionResponse response =
                subscriptionService.addSubscription(
                        user.getId(), dueSoon);

        assertNotNull(response.getStatus());
        assertTrue(
                response.getStatus().equals("RED")
                        || response.getStatus().equals("YELLOW")
                        || response.getStatus().equals("GREEN"));

        SubscriptionRequest trial = SubscriptionRequest.builder()
                .serviceName("Free Trial Service")
                .cost(new BigDecimal("0.00"))
                .billingCycle("MONTHLY")
                .category("Entertainment")
                .startDate(LocalDate.now())
                .isTrial(true)
                .trialEndDate(LocalDate.now().plusDays(14))
                .build();

        SubscriptionResponse trialResponse =
                subscriptionService.addSubscription(
                        user.getId(), trial);

        assertEquals("BLUE", trialResponse.getStatus());
    }
}