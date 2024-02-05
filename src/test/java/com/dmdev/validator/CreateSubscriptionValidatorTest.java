package com.dmdev.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CreateSubscriptionValidatorTest {

  private final CreateSubscriptionValidator validator = CreateSubscriptionValidator.getInstance();

  @Test
  void successfulValidation() {
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .userId(1)
        .name("someName")
        .provider(Provider.APPLE.name())
        .expirationDate(Instant.now().plusSeconds(100))
        .build();

    var validationResult = validator.validate(dto);

    assertFalse(validationResult.hasErrors());
  }

  @Test
  void invalidID() {
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .name("someName")
        .provider(Provider.APPLE.name())
        .expirationDate(Instant.now().plusSeconds(100))
        .build();

    var validationResult = validator.validate(dto);
    Map<Integer, String> errorCodes = validationResult.getErrors().stream().collect(
        Collectors.toMap(Error::getCode, Error::getMessage)
    );

    assertThat(errorCodes).hasSize(1);
    assertThat(errorCodes)
        .contains(entry(100, "userId is invalid"));
  }

  @Test
  void invalidName() {
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .userId(1)
        .provider(Provider.APPLE.name())
        .expirationDate(Instant.now().plusSeconds(100))
        .build();

    var validationResult = validator.validate(dto);
    Map<Integer, String> errorCodes = validationResult.getErrors().stream().collect(
        Collectors.toMap(Error::getCode, Error::getMessage)
    );

    assertThat(errorCodes).hasSize(1);
    assertThat(errorCodes)
        .contains(entry(101, "name is invalid"));
  }

  @Test
  void invalidProvider() {
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .userId(1)
        .name("someName")
        .provider("someProvider")
        .expirationDate(Instant.now().plusSeconds(100))
        .build();

    var validationResult = validator.validate(dto);
    Map<Integer, String> errorCodes = validationResult.getErrors().stream().collect(
        Collectors.toMap(Error::getCode, Error::getMessage)
    );

    assertThat(errorCodes).hasSize(1);
    assertThat(errorCodes)
        .contains(entry(102, "provider is invalid"));
  }

  @Test
  void invalidExpirationDate() {
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .userId(1)
        .name("someName")
        .provider(Provider.APPLE.name())
        .expirationDate(Instant.now().minusSeconds(100))
        .build();

    var validationResult = validator.validate(dto);
    Map<Integer, String> errorCodes = validationResult.getErrors().stream().collect(
        Collectors.toMap(Error::getCode, Error::getMessage)
    );

    assertThat(errorCodes).hasSize(1);
    assertThat(errorCodes)
        .contains(entry(103, "expirationDate is invalid"));
  }

  @Test
  void invalidExpirationDateProviderName() {
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .userId(1)
        .build();

    var validationResult = validator.validate(dto);
    Map<Integer, String> errorCodes = validationResult.getErrors().stream().collect(
        Collectors.toMap(Error::getCode, Error::getMessage)
    );

    assertThat(errorCodes).hasSize(3);
    assertThat(errorCodes)
        .contains(entry(103, "expirationDate is invalid"))
        .contains(entry(102, "provider is invalid"))
        .contains(entry(101, "name is invalid"));
  }
}
