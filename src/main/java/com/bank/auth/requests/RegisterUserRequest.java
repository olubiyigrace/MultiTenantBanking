package com.bank.auth.requests;

import com.bank.enums.UserAccountType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterUserRequest {

    @NotBlank(message = "Name should not be empty")
    @Pattern(regexp = "^[A-Za-z]+(?:[-\\s][A-Za-z]+)*(?:\\s[A-Za-z]+(?:[-\\s][A-Za-z]+)*)?\\s[A-Za-z]+(?:[-\\s][A-Za-z]+)*$",
            message = "Please enter first name, middle name(Optional), and last name separated by spaces")
    @Column(updatable = false)
    private String name;

    @NotBlank(message = "Email should not be empty")
    @Email(message = "example@email.com")
    @Column(updatable = false)
    private String email;

    @NotBlank(message = "Password should not be empty")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*+=])(?=\\S+$).{8,}$",
            message = "Password must contain at least one uppercase, one lowercase, one digit, a character and no whitespace")
    private String password;

    @NotBlank(message = "Phone should not be empty")
    @Column(updatable = false)
    @Pattern(regexp = "^\\+234(70|80|81|90|91)\\d{8}$", message = "Enter a valid phone number and ensure it starts with +234")
    private String phone;

    @NotBlank(message = "Nin should not be empty")
    @Pattern(regexp = "^[0-9]{11}$", message = "NIN must be exactly 11 digits")
    @Column(updatable = false)
    private String nin;

    @NotNull(message = "Account type cannot be null")
    private UserAccountType userAccountType;
}
