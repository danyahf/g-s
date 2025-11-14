package com.danya.token;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID token;
    private Long userId;

    private LocalDateTime expiresAt;

    public RefreshToken(Long userId, Duration ttl) {
        this.userId = userId;
        this.expiresAt = LocalDateTime.now().plus(ttl);
    }
}
