package com.bank.requests;

import com.bank.enums.InstitutionType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterInstitutionRequest {
    @NotBlank(message = "Institution name should not be empty")
    @Column(updatable = false)
    private String name;

    @NotBlank(message = "Institution email should not be empty")
    @Email(message = "example@email.com")
    @Column(updatable = false)
    private String email;

    @NotBlank(message = "Institution phone should not be empty")
    @Pattern(regexp = "^\\+234(20|70|80|81|90|91)\\d{8}$", message = "Enter a valid phone number and ensure it starts with +234")
    @Column(updatable = false)
    private String phone;

    @NotBlank(message = "Institution rcNumber should not be empty")
    @Pattern(regexp = "^RC\\s?\\d{4,10}$", message = "Invalid RC number format")
    @Column(updatable = false)
    private String rcNumber;

    @NotNull(message = "Institution Type cannot be null")
    @Column(updatable = false)
    private InstitutionType institutionType;


    @NotBlank(message = "Admin name should not be empty")
    @Pattern(regexp = "^[A-Za-z]+(?:[-\\s][A-Za-z]+)*(?:\\s[A-Za-z]+(?:[-\\s][A-Za-z]+)*)?\\s[A-Za-z]+(?:[-\\s][A-Za-z]+)*$",
            message = "Please enter first name, middle name(Optional), and last name separated by spaces")
    @Column(updatable = false)
    private String adminName;

    @NotBlank(message = "Admin email should not be empty")
    @Email(message = "example@email.com")
    @Column(updatable = false)
    private String adminEmail;

    @NotBlank(message = "Admin phone should not be empty")
    @Column(updatable = false)
    @Pattern(regexp = "^\\+234(70|80|81|90|91)\\d{8}$", message = "Enter a valid phone number and ensure it starts with +234")
    private String adminPhone;

    @NotBlank(message = "Admin nin should not be empty")
    @Pattern(regexp = "^[0-9]{11}$", message = "NIN must be exactly 11 digits")
    @Column(updatable = false)
    private String adminNin;

    @NotBlank(message = "Admin password should not be empty")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*+=])(?=\\S+$).{8,}$",
            message = "Password must contain at least one uppercase, one lowercase, one digit, a character and no whitespace")
    private String adminPassword;
}
