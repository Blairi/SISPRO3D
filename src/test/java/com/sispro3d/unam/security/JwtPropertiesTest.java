package com.sispro3d.unam.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class JwtPropertiesTest {

    @Autowired
    private JwtProperties properties;

    @Test
    void bindsSigningSecretFromTestProfile() {
        assertThat(properties.getSecret()).isNotBlank();
    }

    @Test
    void signingSecretIsLongEnoughForHs256() {
        assertThat(properties.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8))
                .hasSizeGreaterThanOrEqualTo(32);
    }

    @Test
    void bindsLifetimesAndIssuer() {
        assertThat(properties.getAccessTtl()).isEqualTo(Duration.ofMinutes(30));
        assertThat(properties.getRefreshTtl()).isEqualTo(Duration.ofDays(7));
        assertThat(properties.getIssuer()).isEqualTo("sispro3d-test");
    }
}