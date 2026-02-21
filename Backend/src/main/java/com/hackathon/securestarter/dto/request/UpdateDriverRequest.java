package com.hackathon.securestarter.dto.request;

import com.hackathon.securestarter.enums.DriverStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDriverRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    private LocalDate licenseExpiryDate;

    @Size(max = 30, message = "License category cannot exceed 30 characters")
    private String licenseCategory;

    @Pattern(regexp = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$",
            message = "Phone number format is invalid")
    private String phone;

    private DriverStatus status;
}
