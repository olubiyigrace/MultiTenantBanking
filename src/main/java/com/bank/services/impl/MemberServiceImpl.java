package com.bank.services.impl;

import com.bank.auth.repository.UserRepository;
import com.bank.entities.SavingsAccount;
import com.bank.enums.SavingsAccountType;
import com.bank.exceptions.InvalidRequestException;
import com.bank.repositories.SavingsRepository;
import com.bank.responses.PageResponse;
import com.bank.config.InstitutionContext;
import com.bank.entities.Institution;
import com.bank.entities.MemberProfile;
import com.bank.entities.User;
import com.bank.enums.ProfileStatus;
import com.bank.mapper.MemberMapper;
import com.bank.repositories.MemberRepository;
import com.bank.requests.MemberRequest;
import com.bank.responses.MemberResponse;
import com.bank.services.MemberService;
import com.sun.jdi.request.DuplicateRequestException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final UserRepository userRepository;
    private final SavingsRepository savingsRepository;


    @Transactional
    @Override
    public void createMember(MemberRequest memberRequest) {
        final String institutionId = InstitutionContext.getCurrentInstitution();

       Optional<MemberProfile> existingMember = memberRepository.findByBvn(memberRequest.getBvn());
        if(existingMember.isPresent()){
            log.debug("Member already exists");
            throw new DuplicateRequestException("Member already exists");
        }
        Optional<User> existingUser = userRepository.findByEmail(memberRequest.getRegisterUserRequest().getEmail());
        if(existingUser.isPresent()){
            log.debug("User already exists");
            throw new DuplicateRequestException("User already exists");
        }

        MemberProfile newMember = memberMapper.toEntity(memberRequest);
        newMember.setMemberNumber(generateMemberNumber());

        Institution institution = Institution.builder().id(institutionId).build();
        newMember.setInstitution(institution);
        newMember.getSavingsAccount().setInstitution(institution);
        newMember.getSavingsAccount().setMember(MemberProfile.builder().id(newMember.getId()).build());
        newMember.getUser().setInstitution(institution);

        newMember.getSavingsAccount().setAccountNumber(generateAccountNumber());
        newMember.getSavingsAccount().setInterestRatePercent(BigDecimal.valueOf(0.01));
        SavingsAccount newSavingsAccount = savingsRepository.save(newMember.getSavingsAccount());
        newMember.setSavingsAccount(newSavingsAccount);
        User newUser = userRepository.save(newMember.getUser());
        newMember.setUser(newUser);

        memberRepository.save(newMember);
    }

    private String generateAccountNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder accountNumber = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            accountNumber.append(random.nextInt(10));
        }
        return accountNumber.toString();
    }

    @Override
    public PageResponse<MemberResponse> getAllMembers(ProfileStatus profileStatus, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<MemberProfile> memberProfiles;
        if (profileStatus != null) {
            memberProfiles = memberRepository.findByProfileStatus(profileStatus, pageRequest);
        } else {
            memberProfiles = memberRepository.findAll(pageRequest);
        }
        Page<MemberResponse> memberResponses = memberProfiles.map(memberMapper::toResponse);
        return PageResponse.of(memberResponses);
    }

    private String generateMemberNumber() {
        MemberProfile lastMember = memberRepository.findTopByOrderByCreatedAtDesc().orElse(null);
        if (lastMember == null) {
            return "M0000000000001";
        }
        String lastMemberNumber = lastMember.getMemberNumber();
        long numericPart = Long.parseLong(lastMemberNumber.substring(1));
        long nextNumber = numericPart + 1;
        return String.format("M%013d", nextNumber);
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with the username '" + username + "' not found"));
    }
}



















//
//
//    @Override
//    public void updateUser(String id, RegisterUserRequest registerUserRequest) {
//        final String institutionId = InstitutionContext.getCurrentInstitution();
//        log.info("Updating user for institution: {}", institutionId);
//        final User user = userRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("User with the id '" + id + "' does not exist"));
//        if(!user.getInstitutionId().equals(institutionId)){
//            log.debug("User with the id '{}' does not belong to{}", id, institutionId);
//            throw new InvalidRequestException("User with the id '" + id + "' does not belong to " + institutionId);
//        }
//        if (registerUserRequest.getUserAccountType() == UserAccountType.SUPER_ADMIN){
//            log.debug("Account type is required");
//            throw new InvalidRequestException("Account type is required");
//        }
//        user.setUserAccountType(registerUserRequest.getUserAccountType());
//        userRepository.save(user);
//        log.info("User updated successfully!");
//    }
//
//    @Override
//    public UserResponse getSingleUser(String id) {
//        final String institutionId = InstitutionContext.getCurrentInstitution();
//        log.info("Getting a user for institution: {}", institutionId);
//        final User user = userRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("User with the id '" + id + "' does not exist"));
//        if(!user.getInstitutionId().equals(institutionId)){
//            log.debug("User with the id '{}' does not belong to{}", id, institutionId);
//            throw new InvalidRequestException("User with the id '" + id + "' does not belong to " + institutionId);
//        }
//        return userMapper.toResponse(user);
//    }
//
//    @Override
//    public PageResponse<UserResponse> getAllUsers(int page, int size) {
//        final String institutionId = InstitutionContext.getCurrentInstitution();
//        log.info("Getting all user for institution: {}", institutionId);
//        final PageRequest pageRequest = PageRequest.of(page, size);
//        final Page<User> userPage = userRepository.findAllByInstitution_Id(institutionId, pageRequest);
//        final Page<UserResponse> userResponse = userPage.map(userMapper::toResponse);
//        return PageResponse.of(userResponse);
//    }
//
//    @Override
//    public void deleteUser(String id) {
//        final String institutionId = InstitutionContext.getCurrentInstitution();
//        log.info("Deleting user for institution: {}", institutionId);
//        final User user = userRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("User with the id '" + id + "' does not exist"));
//        if(!user.getInstitutionId().equals(institutionId)){
//            log.debug("User with the id '{}' does not belong to{}", id, institutionId);
//            throw new InvalidRequestException("User with the id '" + id + "' does not belong to " + institutionId);
//        }
//        userRepository.deleteById(id);
//        log.info("User deleted successfully");
//    }
//
//    @Override
//    public void enableUser(String userId) {
//        final String institutionId = InstitutionContext.getCurrentInstitution();
//        log.info("Enabling user for institution: {}", institutionId);
//        final User user = userRepository.findById(userId)
//                .orElseThrow(() -> new EntityNotFoundException("User with the id '" + userId + "' does not exist"));
//        if(!user.getInstitutionId().equals(institutionId)){
//            log.debug("User with the id '{}' does not belong to{}", userId, institutionId);
//            throw new InvalidRequestException("User with the id '" + userId + "' does not belong to " + institutionId);
//        }
//        user.setEnabled(true);
//        userRepository.save(user);
//        log.info("User enabled successfully");
//    }
//
//    @Override
//    public void disableUser(String userId) {
//        final String institutionId = InstitutionContext.getCurrentInstitution();
//        log.info("Enabling user for institution: {}", institutionId);
//        final User user = userRepository.findById(userId)
//                .orElseThrow(() -> new EntityNotFoundException("User with the id '" + userId + "' does not exist"));
//        if(!user.getInstitutionId().equals(institutionId)){
//            log.debug("User with the id '{}' does not belong to{}", userId, institutionId);
//            throw new InvalidRequestException("User with the id '" + userId + "' does not belong to " + institutionId);
//        }
//        user.setEnabled(false);
//        userRepository.save(user);
//        log.info("User disabled successfully");
//    }
//

