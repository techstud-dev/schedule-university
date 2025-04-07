package com.techstud.schedule_university.auth.service.impl;

import com.techstud.schedule_university.auth.config.TokenProperties;
import com.techstud.schedule_university.auth.dto.request.LoginDTO;
import com.techstud.schedule_university.auth.dto.response.SuccessAuthenticationDTO;
import com.techstud.schedule_university.auth.entity.RefreshToken;
import com.techstud.schedule_university.auth.entity.User;
import com.techstud.schedule_university.auth.exception.BadCredentialsException;
import com.techstud.schedule_university.auth.exception.UserNotFoundException;
import com.techstud.schedule_university.auth.repository.UserRepository;
import com.techstud.schedule_university.auth.security.TokenService;
import com.techstud.schedule_university.auth.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {
    @Qualifier("BeanTokenPropertiesBug")
    private final TokenProperties properties;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder;

    public LoginServiceImpl(@Qualifier("BeanTokenPropertiesBug") TokenProperties properties,
                            UserRepository userRepository, TokenService tokenService,
                            BCryptPasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public SuccessAuthenticationDTO processLogin(LoginDTO loginDto) throws Exception {
        User user = findUserByIdentificationField(loginDto.identificationField());
        validatePassword(loginDto.password(), user.getPassword());

        SuccessAuthenticationDTO successAuth = tokenService.generateTokens(user);

        user.setRefreshToken(new RefreshToken(successAuth.refreshToken(),
                Instant.now().plus(properties.getRefreshTokenExpiration(),
                ChronoUnit.SECONDS)));
        userRepository.save(user);

        log.info("User {} logged in successfully", user.getUsername());
        return new SuccessAuthenticationDTO(successAuth.token(), successAuth.refreshToken());
    }

    private User findUserByIdentificationField(String field) throws Exception {
        return userRepository.findByUsernameIgnoreCase(field)
                .or(() -> userRepository.findByEmailIgnoreCase(field))
                .or(() -> userRepository.findByPhoneNumber(field))
                .orElseThrow(UserNotFoundException::new);
    }

    private void validatePassword(String rawPassword, String encodedPassword) throws Exception {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BadCredentialsException("Invalid password.");
        }
    }
}
