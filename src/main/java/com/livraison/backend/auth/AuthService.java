package com.livraison.backend.auth;

import com.livraison.backend.entity.EmailOtp;
import com.livraison.backend.entity.Role;
import com.livraison.backend.entity.User;
import com.livraison.backend.exception.BusinessException;
import com.livraison.backend.repository.EmailOtpRepository;
import com.livraison.backend.repository.UserRepository;
import com.livraison.backend.security.JwtService;
import com.livraison.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository      userRepository;
    private final PasswordEncoder      passwordEncoder;
    private final JwtService           jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailOtpRepository   otpRepository;
    private final EmailService         emailService;

    private static final int OTP_EXPIRY_MINUTES = 10;

    // =====================================================================
    // MOBILE / LEGACY — no OTP, instant access (do NOT change)
    // =====================================================================

    public AuthResponse registerClient(RegisterRequest request) {
        validateRegister(request);
        User user = buildUser(request, Role.ROLE_USER, true);
        userRepository.save(user);
        return tokenResponse(user);
    }

    public AuthResponse registerVoyageur(RegisterRequest request) {
        validateRegister(request);
        User user = buildUser(request, Role.ROLE_VOYAGEUR, true);
        userRepository.save(user);
        return tokenResponse(user);
    }

    // =====================================================================
    // WEB — registration requires OTP verification
    // =====================================================================

    public RegisterResponse registerWebClient(RegisterRequest request) {
        return createUserAndSendOtp(request, Role.ROLE_USER);
    }

    public RegisterResponse registerWebVoyageur(RegisterRequest request) {
        return createUserAndSendOtp(request, Role.ROLE_VOYAGEUR);
    }

    private RegisterResponse createUserAndSendOtp(RegisterRequest request, Role role) {
        validateRegister(request);
        String email = request.getEmail().toLowerCase().trim();

        User user = buildUser(request, role, false);  // emailVerified = false
        userRepository.save(user);

        String code = generateOtp();
        otpRepository.invalidateAllForEmail(email);
        otpRepository.save(EmailOtp.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .used(false)
                .build());

        emailService.sendOtpEmail(email, code);
        log.info("[OTP] Registration OTP sent to {}", email);

        return new RegisterResponse(true, email,
                "Un code de vérification a été envoyé à " + email);
    }

    // =====================================================================
    // VERIFY OTP
    // =====================================================================

    public AuthResponse verifyOtp(OtpVerifyRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        String code  = request.getCode().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Compte introuvable ❌"));

        if (user.isEmailVerified()) {
            // Already verified — just return token (idempotent)
            return tokenResponse(user);
        }

        EmailOtp otp = otpRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BusinessException("Aucun code trouvé. Demandez un nouveau code ❌"));

        if (otp.isUsed()) {
            throw new BusinessException("Ce code a déjà été utilisé. Demandez un nouveau code ❌");
        }
        if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            throw new BusinessException("Code expiré. Demandez un nouveau code ❌");
        }
        if (!otp.getCode().equals(code)) {
            throw new BusinessException("Code incorrect ❌");
        }

        // Mark OTP consumed
        otp.setUsed(true);
        otpRepository.save(otp);

        // Activate account
        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("[OTP] Email verified for {}", email);
        return tokenResponse(user);
    }

    // =====================================================================
    // RESEND OTP
    // =====================================================================

    public void resendOtp(OtpResendRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Compte introuvable ❌"));

        if (user.isEmailVerified()) {
            throw new BusinessException("Ce compte est déjà vérifié ❌");
        }

        String code = generateOtp();
        otpRepository.invalidateAllForEmail(email);
        otpRepository.save(EmailOtp.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .used(false)
                .build());

        emailService.sendOtpEmail(email, code);
        log.info("[OTP] OTP resent to {}", email);
    }

    // =====================================================================
    // LOGIN
    // =====================================================================

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Email ou mot de passe incorrect ❌"));

        if (!user.isEnabled()) {
            throw new BusinessException("Compte désactivé ❌");
        }

        // Enforce email verification only for web-registered accounts
        if (!user.isEmailVerified()) {
            throw new BusinessException("EMAIL_NOT_VERIFIED:" + email);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (Exception e) {
            throw new BusinessException("Email ou mot de passe incorrect ❌");
        }

        return tokenResponse(user);
    }

    // =====================================================================
    // GOOGLE AUTH — Google-verified emails skip OTP
    // =====================================================================

    public AuthResponse googleAuth(GoogleRequest request) {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new BusinessException("Email invalide ❌");
        }

        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .name(request.getName())
                            .googleId(request.getGoogleId())
                            .provider("GOOGLE")
                            .role(Role.ROLE_USER)
                            .enabled(true)
                            .emailVerified(true)  // Google already verified the email
                            .build();
                    return userRepository.save(newUser);
                });

        if (!user.isEnabled()) {
            throw new BusinessException("Compte désactivé ❌");
        }

        return tokenResponse(user);
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    private User buildUser(RegisterRequest request, Role role, boolean emailVerified) {
        return User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(true)
                .emailVerified(emailVerified)
                .build();
    }

    private AuthResponse tokenResponse(User user) {
        return new AuthResponse(jwtService.generateToken(user), user.getRole().name(), user.getEmail());
    }

    private String generateOtp() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }

    private void validateRegister(RegisterRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email déjà utilisé ❌");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BusinessException("Email invalide ❌");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BusinessException("Mot de passe trop court (min 6) ❌");
        }
    }
}
