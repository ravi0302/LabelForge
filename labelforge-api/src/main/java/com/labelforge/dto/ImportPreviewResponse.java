package com.labelforge.dto;

import lombok.Data;
import java.util.List;

@Data
public class ImportPreviewResponse {
    private List<ImportRow> rows;
    private int totalRows;
    private int conflictCount;
    private int validRows;
}
