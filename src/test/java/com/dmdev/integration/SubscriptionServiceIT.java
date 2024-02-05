package com.dmdev.integration;

import static java.time.Clock.systemDefaultZone;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.exception.SubscriptionException;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.service.SubscriptionService;
import com.dmdev.validator.CreateSubscriptionValidator;
import java.time.Instant;
import java.time.Period;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SubscriptionServiceIT extends IntegrationTestBase {

  private SubscriptionDao subscriptionDao;
  private SubscriptionService subscriptionService;
  private CreateSubscriptionMapper createSubscriptionMapper;

  @BeforeEach
  void init() {
    createSubscriptionMapper = CreateSubscriptionMapper.getInstance();
    subscriptionDao = SubscriptionDao.getInstance();
    subscriptionService = new SubscriptionService(subscriptionDao,
        createSubscriptionMapper,
        CreateSubscriptionValidator.getInstance(), systemDefaultZone());
  }

  @Test
  void upsertExistingEntity() {
    CreateSubscriptionDto dto = getSubscriptionDTO();
    Subscription subscriptionBeforeUpdate = createSubscriptionMapper.map(dto);
    subscriptionBeforeUpdate.setExpirationDate(Instant.now().plus(Period.ofDays(20)));
    subscriptionBeforeUpdate.setStatus(Status.CANCELED);

    subscriptionDao.insert(subscriptionBeforeUpdate);
    Subscription updatedSubscription = subscriptionService.upsert(dto);

    assertThat(updatedSubscription.getExpirationDate()).isEqualTo(dto.getExpirationDate());
    assertThat(updatedSubscription.getStatus()).isEqualTo(Status.ACTIVE);
  }

  @Test
  void upsertNotExistingEntity() {
    CreateSubscriptionDto dto = getSubscriptionDTO();

    Subscription updatedSubscription = subscriptionService.upsert(dto);

    assertNotNull(updatedSubscription.getId());
    assertThat(updatedSubscription.getStatus()).isEqualTo(Status.ACTIVE);
  }

  @Test
  void canselForActiveSub() {
    Subscription subscription = subscriptionDao.insert(getSubscription(Status.ACTIVE));

    subscriptionService.cancel(subscription.getId());
    Optional<Subscription> canceledSub = subscriptionDao.findById(subscription.getId());

    assertThat(canceledSub).isPresent();
    assertThat(canceledSub.get().getStatus()).isEqualTo(Status.CANCELED);
  }

  @Test
  void canselForNotActiveSub() {
    Subscription subscription = subscriptionDao.insert(getSubscription(Status.EXPIRED));

    assertEquals(assertThrows(SubscriptionException.class,
            () -> subscriptionService.cancel(subscription.getId())).getMessage(),
        String.format("Only active subscription %d can be canceled", subscription.getId()));
  }

  @Test
  void expireForExpiredSub() {
    Subscription subscription = subscriptionDao.insert(getSubscription(Status.EXPIRED));

    assertEquals(assertThrows(SubscriptionException.class,
            () -> subscriptionService.expire(subscription.getId())).getMessage(),
        String.format("Subscription %d has already expired", subscription.getId()));
  }

  @Test
  void expireForActiveSub() {
    Subscription subscription = subscriptionDao.insert(getSubscription(Status.ACTIVE));

    subscriptionService.expire(subscription.getId());
    Optional<Subscription> expiredSub = subscriptionDao.findById(subscription.getId());

    assertThat(expiredSub).isPresent();
    assertThat(expiredSub.get().getStatus()).isEqualTo(Status.EXPIRED);
    assertTrue(expiredSub.get().getExpirationDate().isBefore(Instant.now(systemDefaultZone())));
    assertNotEquals(expiredSub.get().getExpirationDate(), subscription.getExpirationDate());
  }

  private Subscription getSubscription(Status status) {
    return Subscription.builder()
        .userId(1)
        .name("someOne1")
        .provider(Provider.APPLE)
        .expirationDate(Instant.now().plus(Period.ofDays(30)))
        .status(status)
        .build();
  }

  private CreateSubscriptionDto getSubscriptionDTO() {
    return CreateSubscriptionDto.builder()
        .userId(1)
        .name("someOne1")
        .provider(Provider.APPLE.name())
        .expirationDate(Instant.now().plus(Period.ofDays(30)))
        .build();
  }
}
