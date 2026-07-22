package com.akibahub.proposal;

import com.akibahub.idempotency.IdempotencyService;
import com.akibahub.proposal.dto.ProposalResponse;
import com.akibahub.proposal.entity.Vote;
import com.akibahub.shared.dto.ApiResponse;
import com.akibahub.user.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProposalController {
    private final ProposalService proposalService;
    private final IdempotencyService idempotencyService;

    public ProposalController(ProposalService proposalService, IdempotencyService idempotencyService) {
        this.proposalService = proposalService;
        this.idempotencyService = idempotencyService;
    }

    // Create proposal
    @PostMapping("/groups/{groupId}/proposals")
    public ResponseEntity<ApiResponse<ProposalResponse>> createProposal(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User user,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return idempotencyService.execute(idempotencyKey, user, body,
                new TypeReference<ApiResponse<ProposalResponse>>() {},
                () -> {
                    ProposalResponse p = proposalService.createProposal(groupId,
                            (String) body.get("title"),
                            (String) body.get("description"),
                            new BigDecimal(body.get("amount").toString()),
                            user);
                    return ResponseEntity.ok(ApiResponse.<ProposalResponse>builder()
                            .success(true).message("Proposal created").data(p).build());
                });
    }

    // Vote
    @PostMapping("/proposals/{proposalId}/vote")
    public ResponseEntity<ApiResponse<String>> vote(
            @PathVariable Long proposalId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User user,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return idempotencyService.execute(idempotencyKey, user, body,
                new TypeReference<ApiResponse<String>>() {},
                () -> {
                    Vote.VoteValue value = Vote.VoteValue.valueOf(body.get("vote").toUpperCase());
                    proposalService.vote(proposalId, user, value);
                    return ResponseEntity.ok(ApiResponse.<String>builder()
                            .success(true).message("Vote recorded").build());
                });
    }

    // Proposals for a specific group
    @GetMapping("/groups/{groupId}/proposals")
    public ResponseEntity<ApiResponse<List<ProposalResponse>>> getProposals(@PathVariable Long groupId,
                                                                     @AuthenticationPrincipal User user) {
        List<ProposalResponse> proposals = proposalService.getProposalsForGroup(groupId, user);
        return ResponseEntity.ok(ApiResponse.<List<ProposalResponse>>builder()
                .success(true).data(proposals).build());
    }

    // Proposals for all groups the user belongs to
    @GetMapping("/proposals/my")
    public ResponseEntity<ApiResponse<List<ProposalResponse>>> getMyProposals(@AuthenticationPrincipal User user) {
        List<ProposalResponse> proposals = proposalService.getProposalsForUserGroups(user);
        return ResponseEntity.ok(ApiResponse.<List<ProposalResponse>>builder()
                .success(true).data(proposals).build());
    }

    // Get single proposal detail
    @GetMapping("/proposals/{proposalId}")
    public ResponseEntity<ApiResponse<ProposalResponse>> getProposal(@PathVariable Long proposalId,
                                                              @AuthenticationPrincipal User user) {
        ProposalResponse proposal = proposalService.getProposal(proposalId, user);
        return ResponseEntity.ok(ApiResponse.<ProposalResponse>builder()
                .success(true).data(proposal).build());
    }

    // Update proposal
    @PutMapping("/proposals/{proposalId}")
    public ResponseEntity<ApiResponse<ProposalResponse>> updateProposal(@PathVariable Long proposalId,
                                                                @RequestBody Map<String, Object> body,
                                                                @AuthenticationPrincipal User user) {
        ProposalResponse updated = proposalService.updateProposal(proposalId, body, user);
        return ResponseEntity.ok(ApiResponse.<ProposalResponse>builder()
                .success(true).message("Proposal updated").data(updated).build());
    }

    // Delete proposal
    @DeleteMapping("/proposals/{proposalId}")
    public ResponseEntity<ApiResponse<Void>> deleteProposal(@PathVariable Long proposalId,
                                                            @AuthenticationPrincipal User user) {
        proposalService.deleteProposal(proposalId, user);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Proposal deleted").build());
    }
}