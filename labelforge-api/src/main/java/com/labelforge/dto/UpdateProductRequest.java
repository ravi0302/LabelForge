package com.labelforge.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateProductRequest {

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

    // nullable — removes category if null
    private Long categoryId;
}
