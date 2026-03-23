package com.labelforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SettingsRequest {

    @NotBlank
    @Size(min = 5, max = 7, message = "GS1 prefix must be 5–7 digits")
    @Pattern(regexp = "\\d+", message = "GS1 prefix must contain only digits")
    private String eanCompanyPrefix;
}
