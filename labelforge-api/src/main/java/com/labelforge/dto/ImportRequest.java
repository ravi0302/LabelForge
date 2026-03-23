package com.labelforge.dto;

import lombok.Data;
import java.util.List;

/**
 * Sent by the frontend after user resolves conflicts.
 * Each row carries a resolution decision.
 */
@Data
public class ImportRequest {

    private List<ImportRowResolution> rows;

    @Data
    public static class ImportRowResolution {
        private int rowNumber;
        private String name;
        private String weight;
        private String price;
        private String categoryName;
        private String ean;
        // GENERATE_NEW | USE_EXISTING_KEEP | USE_PROVIDED
        private String resolution;
    }
}
