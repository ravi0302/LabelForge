package com.labelforge.controller;

import com.labelforge.dto.*;
import com.labelforge.service.BarcodeImageService;
import com.labelforge.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService      productService;
    private final BarcodeImageService barcodeImageService;

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId) {
        List<ProductResponse> products = (search != null && !search.isBlank())
            ? productService.search(search.trim(), categoryId)
            : productService.findAll(categoryId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest req) {
        return ResponseEntity.ok(productService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<BulkDeleteResponse> bulkDelete(@RequestBody BulkDeleteRequest req) {
        int count = productService.bulkDelete(req.getIds());
        return ResponseEntity.ok(new BulkDeleteResponse(count));
    }

    @GetMapping("/{id}/barcode")
    public ResponseEntity<byte[]> getBarcodeImage(
            @PathVariable Long id,
            @RequestParam(defaultValue = "300") int w,
            @RequestParam(defaultValue = "120") int h) {
        ProductResponse product = productService.findById(id);
        byte[] png = barcodeImageService.generatePng(product.getEan(), w, h);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"" + product.getEan() + ".png\"")
            .contentType(MediaType.IMAGE_PNG)
            .body(png);
    }

    // ── Import ────────────────────────────────────────────────────────────────

    @PostMapping("/import/preview")
    public ResponseEntity<ImportPreviewResponse> previewImport(
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(productService.previewImport(file));
    }

    @PostMapping("/import/commit")
    public ResponseEntity<ImportCommitResponse> commitImport(
            @RequestBody ImportRequest req) {
        int count = productService.commitImport(req);
        return ResponseEntity.ok(new ImportCommitResponse(count));
    }

    // ── Export ────────────────────────────────────────────────────────────────

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) Long categoryId) throws IOException {
        byte[] data = productService.exportExcel(categoryId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"LabelForge_Products.xlsx\"")
            .contentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(data);
    }

    // Simple response records
    record BulkDeleteResponse(int deleted) {}
    record ImportCommitResponse(int imported) {}
}