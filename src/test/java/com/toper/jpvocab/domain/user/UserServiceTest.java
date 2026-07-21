package com.toper.jpvocab.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.toper.jpvocab.common.exception.UserNotFoundByEmailException;
import com.toper.jpvocab.common.exception.UsernameAlreadyExistsException;
import com.toper.jpvocab.domain.word.JlptLevel;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailVerificationService emailVerificationService;
    @Mock
    private MockEmailService mockEmailService;

    @InjectMocks
    private UserService userService;

    private static void setId(User user, Long id) throws Exception {
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
    }

    private SignupRequest baseSignupRequest(String username, String referrerUsername) {
        return new SignupRequest(
                username, "password1234", username + "@test.com",
                null, null, true, referrerUsername,
                JlptLevel.N3, JlptLevel.N5, 10, ReferralSource.SEARCH);
    }

    @Test
    @DisplayName("정상 회원가입: 비밀번호는 인코딩되어 저장되고, 인증 메일 발송이 호출된다")
    void signup_success_encodesPasswordAndSendsVerificationEmail() throws Exception {
        SignupRequest request = baseSignupRequest("newuser", null);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password1234")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            setId(saved, 1L);
            return saved;
        });

        SignupResponse response = userService.signup(request);

        assertThat(response.username()).isEqualTo("newuser");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("ENCODED");
        verify(emailVerificationService, times(1)).sendVerificationEmail(any(User.class));
    }

    @Test
    @DisplayName("이미 존재하는 아이디로 가입하면 UsernameAlreadyExistsException, 저장은 시도되지 않는다")
    void signup_duplicateUsername_throws() {
        SignupRequest request = baseSignupRequest("existing", null);
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(UsernameAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
        verify(emailVerificationService, never()).sendVerificationEmail(any());
    }

    @Test
    @DisplayName("존재하는 추천인 아이디를 입력하면 referrer가 연결된다")
    void signup_withValidReferrer_linksReferrer() throws Exception {
        User referrer = User.builder().username("referrer_user").build();
        setId(referrer, 5L);

        SignupRequest request = baseSignupRequest("newuser", "referrer_user");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.findByUsername("referrer_user")).thenReturn(Optional.of(referrer));
        when(passwordEncoder.encode(anyString())).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.signup(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getReferrer()).isEqualTo(referrer);
    }

    @Test
    @DisplayName("존재하지 않는 추천인 아이디를 입력해도 가입은 성공하고, referrer는 null로 저장된다")
    void signup_withUnknownReferrer_stillSucceedsWithNullReferrer() {
        SignupRequest request = baseSignupRequest("newuser", "ghost_user");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.findByUsername("ghost_user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SignupResponse response = userService.signup(request);

        assertThat(response.username()).isEqualTo("newuser");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getReferrer()).isNull();
    }

    @Test
    @DisplayName("가입된 이메일로 아이디 찾기 성공")
    void findUsernameByEmail_found() {
        User user = User.builder().username("found_user").email("found@test.com").build();
        when(userRepository.findByEmail("found@test.com")).thenReturn(Optional.of(user));

        FindUsernameResponse response = userService.findUsernameByEmail(new FindUsernameRequest("found@test.com"));

        assertThat(response.username()).isEqualTo("found_user");
        verify(mockEmailService, times(1)).sendUsernameReminder("found@test.com", "found_user");
    }

    @Test
    @DisplayName("가입되지 않은 이메일로 아이디 찾기 시도하면 UserNotFoundByEmailException")
    void findUsernameByEmail_notFound_throws() {
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUsernameByEmail(new FindUsernameRequest("nobody@test.com")))
                .isInstanceOf(UserNotFoundByEmailException.class);

        verify(mockEmailService, never()).sendUsernameReminder(anyString(), anyString());
    }
}
