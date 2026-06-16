package com.akibahub.controller;

import com.akibahub.dto.request.ProposalRequest;
import com.akibahub.dto.request.VoteRequest;
import com.akibahub.dto.response.ApiResponse;
import com.akibahub.model.Vote;
import com.akibahub.security.JwtUtil;
import com.akibahub.service.GovernanceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/governance")
public class GovernanceController {
    private final GovernanceService governanceService;
    private final JwtUtil jwtUtil;

    public GovernanceController(GovernanceService governanceService, JwtUtil jwtUtil) {
        this.governanceService = governanceService;
        this.jwtUtil = jwtUtil;
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUserId(token);
    }

    @PostMapping("/proposals")
    public ResponseEntity<?> createProposal(HttpServletRequest request, @RequestBody ProposalRequest req) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Proposal created",
                governanceService.createProposal(req.getGroupId(), userId, req.getTitle(), req.getDescription(), req.getAmount())));
    }

    @PostMapping("/proposals/{proposalId}/vote")
    public ResponseEntity<?> castVote(HttpServletRequest request, @PathVariable Long proposalId, @RequestBody VoteRequest req) {
        Long userId = getUserId(request);
        Vote.VoteDecision decision = Vote.VoteDecision.valueOf(req.getDecision().toUpperCase());
        return ResponseEntity.ok(new ApiResponse<>(true, "Vote cast", governanceService.castVote(proposalId, userId, decision)));
    }

    @GetMapping("/groups/{groupId}/pending")
    public ResponseEntity<?> getPending(HttpServletRequest request, @PathVariable Long groupId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Pending proposals", governanceService.getPendingProposals(groupId)));
    }
}