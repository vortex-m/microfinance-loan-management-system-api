package com.microfinance.loan.admin.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAdminRequest {
    @NotBlank(message = "Name is required.")
    private String name;

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required.")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number.")
    private String phone;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 character.")
    private String password;

    @NotBlank(message = "Company name is required.")
    private String companyName;

    private String companyCode;
    private String designation;

    private String companyAddress;
    private String companyPhone;
    private String companyEmail;
    private String companyWebsite;
    private String gstNumber;
    private String legalEntityName;
    private String panNumber;

    private String address;
    private String city;
    private String state;
    private String pinCode;
    private String country;
}
