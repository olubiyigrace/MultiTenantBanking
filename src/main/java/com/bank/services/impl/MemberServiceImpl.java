package com.bank.services.impl;

import com.bank.auth.requests.RegisterUserRequest;
import com.bank.auth.response.UserResponse;
import com.bank.common.PageResponse;
import com.bank.config.InstitutionContext;
import com.bank.entities.Institution;
import com.bank.entities.MemberProfile;
import com.bank.entities.User;
import com.bank.enums.UserAccountType;
import com.bank.exceptions.InvalidRequestException;
import com.bank.mapper.MemberMapper;
import com.bank.repositories.MemberRepository;
import com.bank.requests.MemberRequest;
import com.bank.services.MemberService;
import com.sun.jdi.request.DuplicateRequestException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    @Transactional
    @Override
    public void createMember( MemberRequest memberRequest) {
        Optional<MemberProfile> member = memberRepository.findMemberProfileByBvn(memberRequest.getBvn());
        if(member.isPresent()) {
            log.debug("member profile already exists");
            throw new DuplicateRequestException("member profile already exists");
        }
        MemberProfile newMember = memberMapper.toEntity(memberRequest);
        String memberNumber = generateMemberNumber(institution);
        newMember.setInstitution(institution);
        newMember.setMemberNumber(memberNumber);

    }

    private String generateMemberNumber(Institution institution) {
        MemberProfile lastMember = memberRepository.findLastMemberByInstitution(institution.getId()).orElse(null);
        if (lastMember == null) {
            return "M0000000000001";
        }
        String lastMemberNumber = lastMember.getMemberNumber();

        int numericPart = Integer.parseInt(
                lastMemberNumber.substring(1)
        );

        int nextNumber = numericPart + 1;

        String generatedNumber = String.format(
                "M%04d",
                nextNumber
        );

        boolean exists = memberRepository
                .existsByMemberNumberAndInstitution(
                        generatedNumber,
                        institution
                );

        if (exists) {
            throw new RuntimeException(
                    "Generated member number already exists"
            );
        }

        return generatedNumber;
    }




















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
        final Page<User> userPage = userRepository.findAllByInstitution_Id(institutionId, pageRequest);
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
