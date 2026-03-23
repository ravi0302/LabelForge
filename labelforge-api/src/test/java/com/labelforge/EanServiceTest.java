package com.labelforge;

import com.labelforge.service.EanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EanServiceTest {

    private EanService eanService;

    @BeforeEach
    void setUp() {
        eanService = new EanService("20001");
    }

    @Test
    void buildEan_correctLength() {
        String ean = eanService.buildEan(1);
        assertThat(ean).hasSize(13);
    }

    @Test
    void buildEan_startsWithCompanyPrefix() {
        String ean = eanService.buildEan(42);
        assertThat(ean).startsWith("20001");
    }

    @Test
    void buildEan_itemRefPaddedTo5Digits() {
        String ean = eanService.buildEan(7);
        assertThat(ean.substring(5, 10)).isEqualTo("00007");
    }

    @Test
    void buildEan_checkDigitIsValid() {
        for (int i = 1; i <= 100; i++) {
            String ean = eanService.buildEan(i);
            assertThat(eanService.isValid(ean))
                .as("EAN %s (itemRef=%d) should be valid", ean, i)
                .isTrue();
        }
    }

    @Test
    void isValid_rejectsWrongLength() {
        assertThat(eanService.isValid("123456789012")).isFalse();   // 12 digits
        assertThat(eanService.isValid("12345678901234")).isFalse(); // 14 digits
    }

    @Test
    void isValid_rejectsTamperedCheckDigit() {
        String ean     = eanService.buildEan(1);
        char   lastCh  = ean.charAt(12);
        char   tampered = (lastCh == '9') ? '0' : (char)(lastCh + 1);
        String bad     = ean.substring(0, 12) + tampered;
        assertThat(eanService.isValid(bad)).isFalse();
    }

    @Test
    void buildEan_throwsOnInvalidItemRef() {
        assertThatThrownBy(() -> eanService.buildEan(0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> eanService.buildEan(100000))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
