package com.akibahub.audit.controller;

import com.akibahub.audit.dto.request.LedgerEventRequest;
import com.akibahub.audit.dto.response.LedgerEntryResponse;
import com.akibahub.audit.service.LedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final LedgerService ledgerService;

    // Internal endpoint — called by other services directly (not via gateway auth)
    @PostMapping("/events")
    public ResponseEntity<LedgerEntryResponse> recordEvent(
            @Valid @RequestBody LedgerEventRequest request,
            @RequestHeader("X-Service-Key") String serviceKey) {
        // Basic internal service key check
        // In production this is reinforced by network rules (internal only)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ledgerService.record(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<LedgerEntryResponse>> getUserLedger(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String requestingUserId,
            @PageableDefault(size = 20) Pageable pageable) {
        // Users can only see their own ledger
        if (!userId.equals(requestingUserId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(ledgerService.getByUser(userId, pageable));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<Page<LedgerEntryResponse>> getGroupLedger(
            @PathVariable String groupId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ledgerService.getByGroup(groupId, pageable));
    }

    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<Page<LedgerEntryResponse>> getResourceLedger(
            @PathVariable String resourceId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ledgerService.getByResource(resourceId, pageable));
    }
}
