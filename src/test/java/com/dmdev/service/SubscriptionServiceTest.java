package com.dmdev.service;

import static java.time.Clock.systemDefaultZone;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.exception.SubscriptionException;
import com.dmdev.exception.ValidationException;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import com.dmdev.validator.Error;
import com.dmdev.validator.ValidationResult;
import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

  @Mock
  private final Clock clock = systemDefaultZone();
  @Mock
  private SubscriptionDao subscriptionDao;
  @Mock
  private CreateSubscriptionMapper createSubscriptionMapper;
  @Mock
  private CreateSubscriptionValidator createSubscriptionValidator;
  @InjectMocks
  private SubscriptionService subscriptionService;

  @Test
  void successfulUpsert() {
    CreateSubscriptionDto dto = getSubscriptionDTO();
    Subscription subscription = getSubscription(Status.CANCELED);
    List<Subscription> list = List.of(subscription);

    doReturn(new ValidationResult()).when(createSubscriptionValidator).validate(dto);
    doReturn(list).when(subscriptionDao).findByUserId(dto.getUserId());
    doReturn(subscription).when(subscriptionDao).upsert(list.get(0));

    Subscription actualResult = subscriptionService.upsert(dto);

    assertThat(actualResult.getExpirationDate()).isEqualTo(dto.getExpirationDate());
    assertThat(actualResult.getStatus()).isEqualTo(Status.ACTIVE);
  }

  @Test
  void upsertWithThrow() {
    CreateSubscriptionDto dto = getSubscriptionDTO();
    ValidationResult validationResult = new ValidationResult();
    validationResult.add(Error.of(123, "someError"));

    doReturn(validationResult).when(createSubscriptionValidator).validate(dto);

    assertThrows(ValidationException.class, () -> subscriptionService.upsert(dto));
    verifyNoInteractions(subscriptionDao, createSubscriptionMapper);
  }

  @Test
  void successfulCansel() {
    Optional<Subscription> subscription = Optional.ofNullable(getSubscription(Status.ACTIVE));

    doReturn(subscription).when(subscriptionDao).findById(subscription.get().getId());
    subscriptionService.cancel(subscription.get().getId());

    assertThat(subscription.get().getStatus()).isEqualTo(Status.CANCELED);
    verify(subscriptionDao).update(subscription.get());
  }

  @Test
  void unsuccessfulCansel() {
    Optional<Subscription> subscription = Optional.ofNullable(getSubscription(Status.EXPIRED));

    doReturn(subscription).when(subscriptionDao).findById(subscription.get().getId());

    assertThrows(SubscriptionException.class,
        () -> subscriptionService.cancel(subscription.get().getId()));
  }

  @Test
  void successfulExpire() {
    Optional<Subscription> subscription = Optional.ofNullable(getSubscription(Status.ACTIVE));

    doReturn(subscription).when(subscriptionDao).findById(subscription.get().getId());
    subscriptionService.expire(subscription.get().getId());

    assertThat(subscription.get().getStatus()).isEqualTo(Status.EXPIRED);
  }

  @Test
  void unsuccessfulExpire() {
    Optional<Subscription> subscription = Optional.ofNullable(getSubscription(Status.EXPIRED));

    doReturn(subscription).when(subscriptionDao).findById(subscription.get().getId());

    assertThrows(SubscriptionException.class,
        () -> subscriptionService.expire(subscription.get().getId()));
  }

  private Subscription getSubscription(Status status) {
    return Subscription.builder()
        .userId(1)
        .name("someOne1")
        .provider(Provider.APPLE)
        .expirationDate(Instant.now().plus(Period.ofDays(20)))
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
