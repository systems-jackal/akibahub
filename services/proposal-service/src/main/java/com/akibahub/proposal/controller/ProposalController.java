package com.akibahub.proposal.controller;

import com.akibahub.proposal.dto.request.CastVoteRequest;
import com.akibahub.proposal.dto.request.CreateProposalRequest;
import com.akibahub.proposal.dto.response.ProposalResponse;
import com.akibahub.proposal.model.Vote;
import com.akibahub.proposal.service.ProposalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/proposals")
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;

    @PostMapping
    public ResponseEntity<ProposalResponse> createProposal(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String userEmail,
            @Valid @RequestBody CreateProposalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ProposalResponse.from(proposalService.createProposal(
                        userId, userEmail,
                        request.getGroupId(),
                        request.getTitle(),
                        request.getDescription(),
                        request.getAmount(),
                        request.getRecipientPhone(),
                        request.getRecipientDescription())));
    }

    @GetMapping("/{proposalId}")
    public ResponseEntity<ProposalResponse> getProposal(
            @PathVariable String proposalId,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(
                ProposalResponse.from(
                        proposalService.getProposal(proposalId, userId)));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<Page<ProposalResponse>> getGroupProposals(
            @PathVariable String groupId,
            @RequestHeader("X-User-Id") String userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                proposalService.getGroupProposals(groupId, userId, pageable)
                        .map(ProposalResponse::from));
    }

    @PostMapping("/{proposalId}/vote")
    public ResponseEntity<Map<String, String>> castVote(
            @PathVariable String proposalId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String userEmail,
            @Valid @RequestBody CastVoteRequest request) {
        Vote vote = proposalService.castVote(
                proposalId, userId, userEmail, request.getVote());
        return ResponseEntity.ok(Map.of(
                "message", "Vote cast successfully",
                "voteId", vote.getId(),
                "value", vote.getValue().name()
        ));
    }

    @GetMapping("/{proposalId}/votes")
    public ResponseEntity<List<Vote>> getVotes(
            @PathVariable String proposalId,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(
                proposalService.getVotes(proposalId, userId));
    }

    @DeleteMapping("/{proposalId}")
    public ResponseEntity<Void> cancelProposal(
            @PathVariable String proposalId,
            @RequestHeader("X-User-Id") String userId) {
        proposalService.cancelProposal(proposalId, userId);
        return ResponseEntity.ok().build();
    }
}
