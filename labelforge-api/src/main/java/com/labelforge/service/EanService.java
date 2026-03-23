package com.labelforge.service;

import com.labelforge.repository.SettingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * EAN-13 generation and validation.
 * Prefix is loaded from DB on startup (via SettingsService) and can be
 * hot-updated via updatePrefix() without restarting the app.
 */
@Slf4j
@Service
public class EanService {

    private volatile String companyPrefix;

    public EanService(
            @Value("${labelforge.ean.company-prefix}") String defaultPrefix,
            SettingRepository settingRepository) {
        // Load from DB if available, else fall back to application.properties
        String dbPrefix = settingRepository.findById("ean.company.prefix")
            .map(s -> s.getValue())
            .orElse(defaultPrefix);
        this.companyPrefix = dbPrefix;
        log.info("EanService initialized with prefix: {}", this.companyPrefix);
    }

    /** Called by SettingsService when user updates the GS1 prefix. */
    public void updatePrefix(String newPrefix) {
        validatePrefixLength(newPrefix);
        this.companyPrefix = newPrefix;
    }

    public String getCompanyPrefix() {
        return companyPrefix;
    }

    /**
     * Build a full 13-digit EAN-13 from a 5-digit item reference.
     * Prefix can be 5–7 digits; item reference fills remaining digits to reach 12.
     */
    public String buildEan(int itemRef) {
        if (itemRef < 1 || itemRef > 99999) {
            throw new IllegalArgumentException("itemRef must be 1–99999, got: " + itemRef);
        }
        int refLength = 12 - companyPrefix.length();
        String ref    = String.format("%0" + refLength + "d", itemRef);
        String base12 = companyPrefix + ref;
        if (base12.length() != 12) {
            throw new IllegalStateException("base12 must be 12 digits, got: " + base12.length());
        }
        int check = calculateCheckDigit(base12);
        return base12 + check;
    }

    /**
     * Build EAN-13 from a manually provided 12-digit base (no check digit yet).
     */
    public String buildEanFromBase12(String base12) {
        if (base12 == null || base12.length() != 12 || !base12.matches("\\d{12}")) {
            throw new IllegalArgumentException("base12 must be exactly 12 digits");
        }
        int check = calculateCheckDigit(base12);
        return base12 + check;
    }

    /** Validates a full 13-digit EAN string. */
    public boolean isValid(String ean) {
        if (ean == null || ean.length() != 13 || !ean.matches("\\d{13}")) return false;
        int expected = calculateCheckDigit(ean.substring(0, 12));
        return expected == Character.getNumericValue(ean.charAt(12));
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private int calculateCheckDigit(String first12) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit  = Character.getNumericValue(first12.charAt(i));
            int weight = (i % 2 == 0) ? 1 : 3;
            sum += digit * weight;
        }
        return (10 - (sum % 10)) % 10;
    }

    private void validatePrefixLength(String prefix) {
        if (prefix == null || prefix.length() < 5 || prefix.length() > 7) {
            throw new IllegalArgumentException("Company prefix must be 5–7 digits");
        }
    }
}
