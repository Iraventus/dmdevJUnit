package com.dmdev.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import java.time.Instant;
import java.time.Period;
import org.junit.jupiter.api.Test;

class CreateSubscriptionMapperTest {

  private final CreateSubscriptionMapper mapper = CreateSubscriptionMapper.getInstance();

  @Test
  void map() {
    Instant date = Instant.now().plus(Period.ofDays(30));
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .userId(1)
        .name("someName")
        .provider(Provider.APPLE.name())
        .expirationDate(date)
        .build();
    Subscription actualResult = mapper.map(dto);

    Subscription expectedResult = Subscription.builder()
        .userId(1)
        .name("someName")
        .provider(Provider.APPLE)
        .expirationDate(date)
        .status(Status.ACTIVE)
        .build();

    assertThat(actualResult).isEqualTo(expectedResult);
  }
}
