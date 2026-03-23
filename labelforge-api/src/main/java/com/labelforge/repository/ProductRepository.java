package com.labelforge.repository;

import com.labelforge.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByEan(String ean);
    boolean existsByEan(String ean);

    List<Product> findByCategoryIdOrderByCreatedAtDesc(Long categoryId);

    @Query("""
        SELECT p FROM Product p
        LEFT JOIN FETCH p.category
        WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (
            LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR p.ean LIKE CONCAT('%', :query, '%')
          )
        ORDER BY p.createdAt DESC
    """)
    List<Product> searchByNameOrEan(
        @Param("query") String query,
        @Param("categoryId") Long categoryId
    );

    @Query("""
        SELECT p FROM Product p
        LEFT JOIN FETCH p.category
        WHERE :categoryId IS NULL OR p.category.id = :categoryId
        ORDER BY p.createdAt DESC
    """)
    List<Product> findAllWithCategory(@Param("categoryId") Long categoryId);

    @Query(value = "SELECT nextval('product_item_ref_seq')", nativeQuery = true)
    Integer nextItemRef();
}
