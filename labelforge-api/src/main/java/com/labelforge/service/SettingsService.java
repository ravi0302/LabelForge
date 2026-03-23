package com.labelforge.service;

import com.labelforge.dto.SettingsRequest;
import com.labelforge.dto.SettingsResponse;
import com.labelforge.model.Setting;
import com.labelforge.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsService {

    private static final String KEY_PREFIX = "ean.company.prefix";

    private final SettingRepository settingRepository;
    private final EanService        eanService;

    @Transactional(readOnly = true)
    public SettingsResponse getSettings() {
        String prefix = settingRepository.findById(KEY_PREFIX)
            .map(Setting::getValue)
            .orElse("20001");
        return new SettingsResponse(prefix);
    }

    @Transactional
    public SettingsResponse updateSettings(SettingsRequest req) {
        Setting s = settingRepository.findById(KEY_PREFIX)
            .orElseGet(() -> { Setting n = new Setting(); n.setKey(KEY_PREFIX); return n; });
        s.setValue(req.getEanCompanyPrefix());
        settingRepository.save(s);

        // Hot-reload the EanService prefix so new barcodes use the updated prefix
        eanService.updatePrefix(req.getEanCompanyPrefix());
        log.info("GS1 prefix updated to: {}", req.getEanCompanyPrefix());
        return new SettingsResponse(req.getEanCompanyPrefix());
    }
}
