package com.bank.requests;

import com.bank.auth.requests.RegisterUserRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class MemberRequest {
    @NotNull(message = "BVN cannot be null")
    @NotBlank(message = "BVN is required")
    @Pattern(regexp = "^[0-9]{11}$", message = "BVN must be exactly 11 digits")
    @Column(updatable = false)
    private String bvn;

    @NotBlank(message = "Address should not be empty")
    private String address;

    @NotBlank(message = "Employment status should not be empty")
    private String employmentStatus;

    @NotNull(message = "Date of Birth cannot be null")
    private BigDecimal monthlyIncome;

    @NotBlank(message = "Name should not be empty")
    @Pattern(regexp = "^[A-Za-z]+(?:[-\\s][A-Za-z]+)*(?:\\s[A-Za-z]+(?:[-\\s][A-Za-z]+)*)?\\s[A-Za-z]+(?:[-\\s][A-Za-z]+)*$",
            message = "Please enter first name, middle name(Optional), and last name separated by spaces")
    @Column(updatable = false)
    private String nextOfKinName;

    @NotBlank(message = "Phone should not be empty")
    @Column(updatable = false)
    @Pattern(regexp = "^\\+234(70|80|81|90|91)\\d{8}$", message = "Enter a valid phone number and ensure it starts with +234")
    private String nextOfKinPhone;

    @NotNull(message = "Date of Birth cannot be null")
    @Past(message = "Date of Birth must be in the past")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(updatable = false)
    private LocalDate dateOfBirth;

    @NotNull(message = "User cannot be null")
    @Column(updatable = false)
    private RegisterUserRequest registerUserRequest;

    @NotNull(message = "Savings account cannot be null")
    @Column(updatable = false)
    private SavingsAccountRequest savingsAccountRequest;
}
