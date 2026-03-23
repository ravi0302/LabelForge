package com.labelforge.controller;

import com.labelforge.dto.SettingsRequest;
import com.labelforge.dto.SettingsResponse;
import com.labelforge.service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public ResponseEntity<SettingsResponse> get() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @PutMapping
    public ResponseEntity<SettingsResponse> update(@Valid @RequestBody SettingsRequest req) {
        return ResponseEntity.ok(settingsService.updateSettings(req));
    }
}
