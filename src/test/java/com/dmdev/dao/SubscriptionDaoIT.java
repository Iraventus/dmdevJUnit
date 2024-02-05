package com.dmdev.dao;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import java.time.Instant;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SubscriptionDaoIT extends IntegrationTestBase {

  private final SubscriptionDao subscriptionDao = SubscriptionDao.getInstance();

  @Test
  void findAll() {
    Subscription subscription1 = subscriptionDao.insert(getSubscription("someOne1", 1));
    Subscription subscription2 = subscriptionDao.insert(getSubscription("someOne2", 2));
    Subscription subscription3 = subscriptionDao.insert(getSubscription("someOne3", 3));

    List<Subscription> actualResult = subscriptionDao.findAll();

    List<Integer> subscriptionIds = actualResult.stream()
        .map(Subscription::getId)
        .toList();

    assertThat(actualResult).hasSize(3);
    assertThat(subscriptionIds).contains(subscription1.getId(), subscription2.getId(),
        subscription3.getId());
  }

  @Test
  void findById() {
    Subscription subscription = subscriptionDao.insert(getSubscription("someOne1", 1));

    Optional<Subscription> actualResult = subscriptionDao.findById(subscription.getId());
    assertThat(actualResult).isPresent();
    assertThat(actualResult.get()).isEqualTo(subscription);
  }

  @Test
  void deleteExistingEntity() {
    Subscription subscription = subscriptionDao.insert(getSubscription("someOne1", 1));

    boolean actualResult = subscriptionDao.delete(subscription.getId());

    assertTrue(actualResult);
  }

  @Test
  void deleteNotExistingEntity() {
    Subscription subscription = subscriptionDao.insert(getSubscription("someOne1", 1));

    boolean actualResult = subscriptionDao.delete(subscription.getId() + 1);

    assertFalse(actualResult);
  }

  @Test
  void update() {
    Subscription subscription = subscriptionDao.insert(getSubscription("someOne1", 1));
    subscription.setName("someOne2");
    subscription.setProvider(Provider.GOOGLE);

    subscriptionDao.update(subscription);

    Subscription updatedSubscription = subscriptionDao.findById(subscription.getId()).get();
    assertThat(updatedSubscription).isEqualTo(subscription);
  }

  @Test
  void insert() {
    Subscription subscription = getSubscription("someOne1", 1);

    Subscription actualResult = subscriptionDao.insert(subscription);

    assertNotNull(actualResult.getId());
  }

  @Test
  void findByUserId() {
    Subscription subscription1 = subscriptionDao.insert(getSubscription("someOne1", 1));
    Subscription subscription2 = subscriptionDao.insert(getSubscription("someOne2", 1));
    Subscription subscription3 = subscriptionDao.insert(getSubscription("someOne3", 2));

    Optional<List<Subscription>> actualResult = ofNullable(
        subscriptionDao.findByUserId(1));

    assertThat(actualResult).isPresent();
    assertThat(actualResult.get()).hasSize(2);
    assertThat(actualResult.get()).contains(subscription1, subscription2);
    assertThat(actualResult.get()).doesNotContain(subscription3);
  }

  @Test
  void shouldNotFindByUserId() {
    Optional<List<Subscription>> actualResult = ofNullable(
        subscriptionDao.findByUserId(1));

    assertThat(actualResult).isPresent(); //actual result containing value: []
    assertThat(actualResult.get()).isEmpty();
  }

  private Subscription getSubscription(String name, int userID) {
    return Subscription.builder()
        .userId(userID)
        .name(name)
        .provider(Provider.APPLE)
        .expirationDate(Instant.now().plus(Period.ofDays(30)))
        .status(Status.ACTIVE)
        .build();
  }
}
