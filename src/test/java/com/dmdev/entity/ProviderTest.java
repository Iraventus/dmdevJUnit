package com.dmdev.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ProviderTest {

  @Test
  void findByNameWithCorrectName() {
    assertThat(Provider.findByName("google")).isEqualTo(Provider.GOOGLE);
  }

  @Test
  void findByNameWithIncorrectName() {
    assertThrows(NoSuchElementException.class,() -> Provider.findByName("goog"));
  }

  @Test
  void findByNameOptWithCorrectName() {
    Optional<Provider> provider = Provider.findByNameOpt("google");
    assertThat(provider).isPresent();
    assertThat(provider.get()).isEqualTo(Provider.GOOGLE);
  }

  @Test
  void findByNameOptWithIncorrectName() {
    Optional<Provider> provider = Provider.findByNameOpt("goog");
    assertThat(provider).isEmpty();
  }
}
