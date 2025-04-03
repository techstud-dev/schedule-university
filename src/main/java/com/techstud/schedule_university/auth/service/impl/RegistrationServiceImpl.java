package com.techstud.schedule_university.auth.service.impl;

import com.techstud.schedule_university.auth.config.JwtProperties;
import com.techstud.schedule_university.auth.dto.request.RegisterDTO;
import com.techstud.schedule_university.auth.dto.response.SuccessAuthenticationDTO;
import com.techstud.schedule_university.auth.entity.RefreshToken;
import com.techstud.schedule_university.auth.entity.Role;
import com.techstud.schedule_university.auth.entity.University;
import com.techstud.schedule_university.auth.entity.User;
import com.techstud.schedule_university.auth.exception.UserExistsException;
import com.techstud.schedule_university.auth.repository.RoleRepository;
import com.techstud.schedule_university.auth.repository.UniversityRepository;
import com.techstud.schedule_university.auth.repository.UserRepository;
import com.techstud.schedule_university.auth.security.TokenService;
import com.techstud.schedule_university.auth.service.RegistrationService;
import com.techstud.schedule_university.auth.service.UserFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Slf4j
@Service
public class RegistrationServiceImpl implements RegistrationService {

    @Qualifier("JwtProperties")
    private final JwtProperties properties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserFactory userFactory;
    private final TokenService tokenService;
    private final UniversityRepository universityRepository;

    public RegistrationServiceImpl(@Qualifier("JwtProperties") JwtProperties properties, UserRepository userRepository,
                                   RoleRepository roleRepository, UserFactory userFactory,
                                   TokenService tokenService, UniversityRepository universityRepository) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userFactory = userFactory;
        this.tokenService = tokenService;
        this.universityRepository = universityRepository;
    }

    @Override
    @Transactional
    public SuccessAuthenticationDTO processRegister(RegisterDTO registerDto) throws Exception {
        validateUserUniqueness(registerDto);

        University university = resolveUniversity(registerDto.university());
        Role userRole = resolveRole();

        User newUser = createAndSaveUser(registerDto, university, userRole);

        String accessToken = tokenService.generateToken(newUser);
        String refreshToken = tokenService.generateRefreshToken(newUser);

        newUser.setRefreshToken(new RefreshToken(refreshToken, Instant.now().plus(properties.getRefreshTokenExpiration(),
                ChronoUnit.SECONDS)));
        userRepository.save(newUser);

        log.info("User {} registered successfully", newUser.getUsername());
        return new SuccessAuthenticationDTO(accessToken, refreshToken);
    }

    protected University resolveUniversity(String universityName) {
        return universityRepository.findByName(universityName)
                .orElseGet(() -> universityRepository.save(new University(universityName)));
    }

    protected Role resolveRole() {
        return roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(new Role("USER")));
    }

    private User createAndSaveUser(RegisterDTO dto, University university, Role role) throws Exception {
        User newUser = userFactory.createUser(
                dto.username(),
                dto.fullName(),
                dto.password(),
                dto.email(),
                dto.phoneNumber(),
                dto.groupNumber()
        );
        newUser.setUniversity(university);
        newUser.setRoles(Set.of(role));
        return newUser;
    }

    private void validateUserUniqueness(RegisterDTO registerDto) throws Exception {
        if (userRepository.existsByUniqueFields(
                registerDto.username(),
                registerDto.email(),
                registerDto.phoneNumber())) {
            throw new UserExistsException();
        }
    }
}
