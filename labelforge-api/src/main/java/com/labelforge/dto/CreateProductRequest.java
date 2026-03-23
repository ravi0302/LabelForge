package com.labelforge.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "Weight/quantity is required")
    @Size(max = 100)
    private String weight;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    // Optional — if provided, use this EAN instead of auto-generating
    @Size(min = 13, max = 13, message = "EAN must be exactly 13 digits")
    @Pattern(regexp = "\\d{13}", message = "EAN must contain only digits")
    private String ean;

    // Optional category id
    private Long categoryId;
}