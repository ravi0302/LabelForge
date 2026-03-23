package com.labelforge.dto;

import com.labelforge.model.Product;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String weight;
    private BigDecimal price;
    private String ean;
    private Integer itemRef;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductResponse from(Product p) {
        ProductResponse r = new ProductResponse();
        r.id           = p.getId();
        r.name         = p.getName();
        r.weight       = p.getWeight();
        r.price        = p.getPrice();
        r.ean          = p.getEan();
        r.itemRef      = p.getItemRef();
        r.createdAt    = p.getCreatedAt();
        r.updatedAt    = p.getUpdatedAt();
        if (p.getCategory() != null) {
            r.categoryId   = p.getCategory().getId();
            r.categoryName = p.getCategory().getName();
        }
        return r;
    }
}