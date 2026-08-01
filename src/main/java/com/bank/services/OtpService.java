package com.bank.services;

import com.bank.dtos.requestDtos.OtpRequest;
import com.bank.entities.Otp;
import com.bank.exceptions.InvalidRequestException;
import com.bank.exceptions.UnauthorizedException;
import com.bank.repositories.OtpRepository;
import com.bank.utils.AppUtil;
import com.bank.enums.OtpPurpose;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OtpService {
    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;


    public String createOtp(OtpRequest request) {
        checkCooldown(request.getEmail(), request.getPurpose());
        String otpCode = AppUtil.generateOtp();
            Otp otp = Otp.builder()
                    .email(request.getEmail())
                    .purpose(request.getPurpose())
                    .otp(passwordEncoder.encode(otpCode))
                    .used(false)
                    .expiresAt(LocalDateTime.now().plusMinutes(10))
                    .build();
            otpRepository.save(otp);
        createCooldown(request.getEmail(), request.getPurpose());
        return otpCode;
    }

    public String resendOtp(OtpRequest request) {
        Optional<Otp> existingOtp = otpRepository.findByEmailAndPurpose(request.getEmail(), request.getPurpose());
        if(existingOtp.isEmpty()){
            throw new UnauthorizedException("You cannot make this request");
        }
        checkCooldown(request.getEmail(), request.getPurpose());
        String otpCode = AppUtil.generateOtp();
        Otp otp = Otp.builder()
                .email(request.getEmail())
                .purpose(request.getPurpose())
                .otp(passwordEncoder.encode(otpCode))
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        otpRepository.save(otp);
        createCooldown(request.getEmail(), request.getPurpose());
        return otpCode;
    }

    private String cooldownKey(String email, OtpPurpose purpose) {
        return "otp:cooldown:" + purpose + ":" + email;
    }

    private void checkCooldown(String email, OtpPurpose purpose) {
        String key = cooldownKey(email, purpose);
        if (redisTemplate.hasKey(key)) {
            throw new InvalidRequestException("Please wait before requesting another OTP.");
        }
    }

    private void createCooldown(String email, OtpPurpose purpose) {
        String key = cooldownKey(email, purpose);
        redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(60));
    }
}
