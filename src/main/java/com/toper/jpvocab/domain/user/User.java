package com.toper.jpvocab.domain.user;

import com.toper.jpvocab.domain.word.JlptLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_username", columnNames = "username")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // 약관 동의는 회원가입 필수 항목이라 항상 true여야 하지만(UserService에서 검증),
    // 엔티티 레벨에서도 언제 동의했는지 기록을 남겨둔다.
    @Column(name = "terms_agreed", nullable = false)
    @Builder.Default
    private boolean termsAgreed = false;

    @Column(name = "terms_agreed_at")
    private LocalDateTime termsAgreedAt;

    // 추천인. 가입 시 입력한 아이디로 조회해서 연결하고, 없으면 그냥 null로 둔다(가입 자체를 막지 않음).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_id")
    private User referrer;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_level", length = 10)
    private JlptLevel targetLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_level", length = 10)
    private JlptLevel currentLevel;

    @Column(name = "daily_goal_count")
    private Integer dailyGoalCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "referral_source", length = 20)
    private ReferralSource referralSource;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void changePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
    }

    @jakarta.persistence.PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
