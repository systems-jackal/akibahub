package com.akibahub.proposal;

import com.akibahub.proposal.entity.Proposal;
import com.akibahub.proposal.entity.Vote;
import com.akibahub.shared.dto.ApiResponse;
import com.akibahub.user.entity.User;
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

    public ProposalController(ProposalService proposalService) {
        this.proposalService = proposalService;
    }

    // Create proposal
    @PostMapping("/groups/{groupId}/proposals")
    public ResponseEntity<ApiResponse<Proposal>> createProposal(@PathVariable Long groupId,
                                                                @RequestBody Map<String, Object> body,
                                                                @AuthenticationPrincipal User user) {
        Proposal p = proposalService.createProposal(groupId,
                (String) body.get("title"),
                (String) body.get("description"),
                new BigDecimal(body.get("amount").toString()),
                user);
        return ResponseEntity.ok(ApiResponse.<Proposal>builder()
                .success(true).message("Proposal created").data(p).build());
    }

    // Vote
    @PostMapping("/proposals/{proposalId}/vote")
    public ResponseEntity<ApiResponse<String>> vote(@PathVariable Long proposalId,
                                                    @RequestBody Map<String, String> body,
                                                    @AuthenticationPrincipal User user) {
        Vote.VoteValue value = Vote.VoteValue.valueOf(body.get("vote").toUpperCase());
        proposalService.vote(proposalId, user, value);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true).message("Vote recorded").build());
    }

    // Proposals for a specific group
    @GetMapping("/groups/{groupId}/proposals")
    public ResponseEntity<ApiResponse<List<Proposal>>> getProposals(@PathVariable Long groupId,
                                                                     @AuthenticationPrincipal User user) {
        List<Proposal> proposals = proposalService.getProposalsForGroup(groupId, user);
        return ResponseEntity.ok(ApiResponse.<List<Proposal>>builder()
                .success(true).data(proposals).build());
    }

    // Proposals for all groups the user belongs to
    @GetMapping("/proposals/my")
    public ResponseEntity<ApiResponse<List<Proposal>>> getMyProposals(@AuthenticationPrincipal User user) {
        List<Proposal> proposals = proposalService.getProposalsForUserGroups(user);
        return ResponseEntity.ok(ApiResponse.<List<Proposal>>builder()
                .success(true).data(proposals).build());
    }

    // Get single proposal detail
    @GetMapping("/proposals/{proposalId}")
    public ResponseEntity<ApiResponse<Proposal>> getProposal(@PathVariable Long proposalId,
                                                              @AuthenticationPrincipal User user) {
        Proposal proposal = proposalService.getProposal(proposalId, user);
        return ResponseEntity.ok(ApiResponse.<Proposal>builder()
                .success(true).data(proposal).build());
    }

    // Update proposal
    @PutMapping("/proposals/{proposalId}")
    public ResponseEntity<ApiResponse<Proposal>> updateProposal(@PathVariable Long proposalId,
                                                                @RequestBody Map<String, Object> body,
                                                                @AuthenticationPrincipal User user) {
        Proposal updated = proposalService.updateProposal(proposalId, body, user);
        return ResponseEntity.ok(ApiResponse.<Proposal>builder()
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