package com.labelforge.service;

import com.labelforge.dto.*;
import com.labelforge.exception.DuplicateEanException;
import com.labelforge.exception.ResourceNotFoundException;
import com.labelforge.model.Category;
import com.labelforge.model.Product;
import com.labelforge.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository  productRepository;
    private final EanService         eanService;
    private final CategoryService    categoryService;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public ProductResponse create(CreateProductRequest req) {
        String ean;
        int    itemRef;

        if (req.getEan() != null && !req.getEan().isBlank()) {
            // Manual EAN provided — validate and check for duplicates
            if (!eanService.isValid(req.getEan())) {
                throw new IllegalArgumentException("Invalid EAN-13: check digit mismatch");
            }
            if (productRepository.existsByEan(req.getEan())) {
                throw new DuplicateEanException(req.getEan());
            }
            ean      = req.getEan();
            itemRef  = productRepository.nextItemRef();
        } else {
            // Auto-generate EAN
            itemRef = productRepository.nextItemRef();
            ean     = eanService.buildEan(itemRef);
        }

        Product product = new Product();
        product.setName(req.getName());
        product.setWeight(req.getWeight());
        product.setPrice(req.getPrice());
        product.setEan(ean);
        product.setItemRef(itemRef);
        product.setCategory(categoryService.getOrNull(req.getCategoryId()));

        log.info("Creating product '{}' EAN={}", req.getName(), ean);
        return ProductResponse.from(productRepository.save(product));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest req) {
        Product product = getOrThrow(id);
        product.setName(req.getName());
        product.setWeight(req.getWeight());
        product.setPrice(req.getPrice());
        product.setCategory(categoryService.getOrNull(req.getCategoryId()));
        log.info("Updated product id={}", id);
        return ProductResponse.from(productRepository.save(product));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll(Long categoryId) {
        return productRepository.findAllWithCategory(categoryId)
            .stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> search(String query, Long categoryId) {
        return productRepository.searchByNameOrEan(query, categoryId)
            .stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return ProductResponse.from(getOrThrow(id));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        productRepository.delete(getOrThrow(id));
    }

    @Transactional
    public int bulkDelete(List<Long> ids) {
        List<Product> products = productRepository.findAllById(ids);
        productRepository.deleteAll(products);
        log.info("Bulk deleted {} products", products.size());
        return products.size();
    }

    // ── Import (preview) ──────────────────────────────────────────────────────

    /**
     * Parse the uploaded Excel file and return a preview with conflict info.
     * Does NOT write to DB — frontend shows this to the user for conflict resolution.
     */
    @Transactional(readOnly = true)
    public ImportPreviewResponse previewImport(MultipartFile file) throws IOException {
        List<ImportRow> rows = new ArrayList<>();

        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Row header  = sheet.getRow(0);

            // Find column indices
            int colName     = findCol(header, "product name", "name");
            int colWeight   = findCol(header, "weight", "weight/quantity", "quantity");
            int colPrice    = findCol(header, "price");
            int colCategory = findCol(header, "category");
            int colEan      = findCol(header, "ean", "ean-13", "ean13", "barcode");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                ImportRow ir = new ImportRow();
                ir.setRowNumber(i + 1);
                ir.setName(    getString(row, colName));
                ir.setWeight(  getString(row, colWeight));
                ir.setCategoryName(getString(row, colCategory));

                try {
                    ir.setPrice(new BigDecimal(getString(row, colPrice)));
                } catch (Exception e) {
                    ir.setPrice(BigDecimal.ZERO);
                }

                if (colEan >= 0) {
                    String ean = getString(row, colEan);
                    if (ean != null && !ean.isBlank()) {
                        ir.setEan(ean.trim());
                        // Check for conflicts
                        if (!eanService.isValid(ean.trim())) {
                            ir.setConflictType("INVALID_EAN");
                            ir.setConflictDetail("EAN-13 check digit is invalid");
                        } else if (productRepository.existsByEan(ean.trim())) {
                            ir.setConflictType("DUPLICATE_EAN");
                            Product existing = productRepository.findByEan(ean.trim()).get();
                            ir.setConflictDetail("EAN already used by: " + existing.getName());
                        }
                    }
                }
                rows.add(ir);
            }
        }

        ImportPreviewResponse resp = new ImportPreviewResponse();
        resp.setRows(rows);
        resp.setTotalRows(rows.size());
        resp.setConflictCount((int) rows.stream().filter(r -> r.getConflictType() != null).count());
        resp.setValidRows((int) rows.stream().filter(r -> r.getConflictType() == null).count());
        return resp;
    }

    /**
     * Commit the import after user has resolved all conflicts.
     */
    @Transactional
    public int commitImport(ImportRequest req) {
        int count = 0;
        for (ImportRequest.ImportRowResolution row : req.getRows()) {
            try {
                String resolution = row.getResolution(); // GENERATE_NEW | USE_PROVIDED | SKIP

                if ("SKIP".equals(resolution)) continue;

                int    itemRef = productRepository.nextItemRef();
                String ean;

                if ("USE_PROVIDED".equals(resolution) && row.getEan() != null) {
                    ean = row.getEan();
                } else {
                    ean = eanService.buildEan(itemRef);
                }

                if (productRepository.existsByEan(ean)) {
                    log.warn("Skipping row {} — EAN {} still conflicts after resolution", row.getRowNumber(), ean);
                    continue;
                }

                Product p = new Product();
                p.setName(row.getName());
                p.setWeight(row.getWeight() != null ? row.getWeight() : "N/A");
                p.setPrice(new BigDecimal(row.getPrice() != null ? row.getPrice() : "0"));
                p.setEan(ean);
                p.setItemRef(itemRef);
                p.setCategory(categoryService.getOrCreateByName(row.getCategoryName()));
                productRepository.save(p);
                count++;
            } catch (Exception e) {
                log.error("Failed to import row {}: {}", row.getRowNumber(), e.getMessage());
            }
        }
        log.info("Import committed: {} products saved", count);
        return count;
    }

    // ── Excel export ──────────────────────────────────────────────────────────

    public byte[] exportExcel(Long categoryId) throws IOException {
        List<Product> products = productRepository.findAllWithCategory(categoryId);

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Products");

            // Header row style
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font whiteFont = wb.createFont();
            whiteFont.setColor(IndexedColors.WHITE.getIndex());
            whiteFont.setBold(true);
            headerStyle.setFont(whiteFont);

            // Header
            Row header = sheet.createRow(0);
            String[] cols = {"Product Name", "Weight/Quantity", "Price (₹)", "EAN-13 Barcode", "Category"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowIdx = 1;
            for (Product p : products) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getName());
                row.createCell(1).setCellValue(p.getWeight());
                row.createCell(2).setCellValue(p.getPrice().doubleValue());
                row.createCell(3).setCellValue(p.getEan());
                row.createCell(4).setCellValue(
                    p.getCategory() != null ? p.getCategory().getName() : "");
            }

            // Auto-size columns
            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            wb.write(out);
            return out.toByteArray();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Product getOrThrow(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private int findCol(Row header, String... names) {
        if (header == null) return -1;
        for (Cell cell : header) {
            String val = cell.getStringCellValue().trim().toLowerCase();
            for (String name : names) {
                if (val.contains(name)) return cell.getColumnIndex();
            }
        }
        return -1;
    }

    private String getString(Row row, int col) {
        if (col < 0 || row == null) return null;
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default      -> null;
        };
    }

    private boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }
}
