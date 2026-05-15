package com.bank.services.impl;

import com.bank.common.PageResponse;
import com.bank.config.InstitutionContext;
import com.bank.entities.Institution;
import com.bank.exceptions.InvalidRequestException;
import com.bank.requests.RegisterUserRequest;
import com.bank.entities.User;
import com.bank.enums.UserAccountType;
import com.bank.exceptions.DuplicateResourceException;
import com.bank.mapper.UserMapper;
import com.bank.repositories.UserRepository;
import com.bank.responses.UserResponse;
import com.bank.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public void updateUser(String id, RegisterUserRequest registerUserRequest) {
        final String institutionId = InstitutionContext.getCurrentInstitution();
        log.info("Updating user for institution: {}", institutionId);
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with the id '" + id + "' does not exist"));
        if(!user.getInstitutionId().equals(institutionId)){
            log.debug("User with the id '{}' does not belong to{}", id, institutionId);
            throw new InvalidRequestException("User with the id '" + id + "' does not belong to " + institutionId);
        }
        if (registerUserRequest.getUserAccountType() == UserAccountType.SUPER_ADMIN){
            log.debug("Account type is required");
            throw new InvalidRequestException("Account type is required");
        }
        user.setUserAccountType(registerUserRequest.getUserAccountType());
        userRepository.save(user);
        log.info("User updated successfully!");
    }

    @Override
    public UserResponse getSingleUser(String id) {
        final String institutionId = InstitutionContext.getCurrentInstitution();
        log.info("Getting a user for institution: {}", institutionId);
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with the id '" + id + "' does not exist"));
        if(!user.getInstitutionId().equals(institutionId)){
            log.debug("User with the id '{}' does not belong to{}", id, institutionId);
            throw new InvalidRequestException("User with the id '" + id + "' does not belong to " + institutionId);
        }
        return userMapper.toResponse(user);
    }

    @Override
    public PageResponse<UserResponse> getAllUsers(int page, int size) {
        final String institutionId = InstitutionContext.getCurrentInstitution();
        log.info("Getting all user for institution: {}", institutionId);
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<User> userPage = userRepository.findAllByInstitutionId(institutionId, pageRequest);
        final Page<UserResponse> userResponse = userPage.map(userMapper::toResponse);
        return PageResponse.of(userResponse);
    }

    @Override
    public void deleteUser(String id) {
        final String institutionId = InstitutionContext.getCurrentInstitution();
        log.info("Deleting user for institution: {}", institutionId);
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with the id '" + id + "' does not exist"));
        if(!user.getInstitutionId().equals(institutionId)){
            log.debug("User with the id '{}' does not belong to{}", id, institutionId);
            throw new InvalidRequestException("User with the id '" + id + "' does not belong to " + institutionId);
        }
        userRepository.deleteById(id);
        log.info("User deleted successfully");
    }

    @Override
    public void enableUser(String userId) {
        final String institutionId = InstitutionContext.getCurrentInstitution();
        log.info("Enabling user for institution: {}", institutionId);
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with the id '" + userId + "' does not exist"));
        if(!user.getInstitutionId().equals(institutionId)){
            log.debug("User with the id '{}' does not belong to{}", userId, institutionId);
            throw new InvalidRequestException("User with the id '" + userId + "' does not belong to " + institutionId);
        }
        user.setEnabled(true);
        userRepository.save(user);
        log.info("User enabled successfully");
    }

    @Override
    public void disableUser(String userId) {
        final String institutionId = InstitutionContext.getCurrentInstitution();
        log.info("Enabling user for institution: {}", institutionId);
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with the id '" + userId + "' does not exist"));
        if(!user.getInstitutionId().equals(institutionId)){
            log.debug("User with the id '{}' does not belong to{}", userId, institutionId);
            throw new InvalidRequestException("User with the id '" + userId + "' does not belong to " + institutionId);
        }
        user.setEnabled(false);
        userRepository.save(user);
        log.info("User disabled successfully");
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with the username '" + username + "' not found"));
    }
}
