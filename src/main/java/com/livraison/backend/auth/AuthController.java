package com.livraison.backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ---- Mobile / legacy (no OTP) ----

    @PostMapping("/register/client")
    public ResponseEntity<AuthResponse> registerClient(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerClient(request));
    }

    @PostMapping("/register/voyageur")
    public ResponseEntity<AuthResponse> registerVoyageur(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerVoyageur(request));
    }

    // ---- Web registration (requires OTP) ----

    @PostMapping("/register/web/client")
    public ResponseEntity<RegisterResponse> registerWebClient(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerWebClient(request));
    }

    @PostMapping("/register/web/voyageur")
    public ResponseEntity<RegisterResponse> registerWebVoyageur(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerWebVoyageur(request));
    }

    // ---- OTP verification ----

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Void> resendOtp(@RequestBody OtpResendRequest request) {
        authService.resendOtp(request);
        return ResponseEntity.ok().build();
    }

    // ---- Common ----

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleAuth(@RequestBody GoogleRequest request) {
        return ResponseEntity.ok(authService.googleAuth(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
