package com.labelforge.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * Represents one parsed row from the imported Excel file.
 * Used internally and returned to the frontend for conflict review.
 */
@Data
public class ImportRow {
    private int rowNumber;
    private String name;
    private String weight;
    private BigDecimal price;
    private String categoryName;
    private String ean;              // null if not in the Excel file
    private String conflictType;     // null | "DUPLICATE_EAN"
    private String conflictDetail;   // human-readable description
}