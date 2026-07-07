package com.akibahub.proposal;

import com.akibahub.proposal.entity.Proposal;
import com.akibahub.proposal.entity.Vote;
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

    @PostMapping("/groups/{groupId}/proposals")
    public ResponseEntity<Proposal> createProposal(@PathVariable Long groupId,
                                                   @RequestBody Map<String, Object> body,
                                                   @AuthenticationPrincipal User user) {
        Proposal p = proposalService.createProposal(groupId,
                (String) body.get("title"),
                (String) body.get("description"),
                new BigDecimal(body.get("amount").toString()),
                user);
        return ResponseEntity.ok(p);
    }

    @PostMapping("/proposals/{proposalId}/vote")
    public ResponseEntity<?> vote(@PathVariable Long proposalId,
                                  @RequestBody Map<String, String> body,
                                  @AuthenticationPrincipal User user) {
        Vote.VoteValue value = Vote.VoteValue.valueOf(body.get("vote").toUpperCase());
        proposalService.vote(proposalId, user, value);
        return ResponseEntity.ok(Map.of("message", "Vote recorded"));
    }

    @GetMapping("/groups/{groupId}/proposals")
    public ResponseEntity<List<Proposal>> getProposals(@PathVariable Long groupId) {
        return ResponseEntity.ok(proposalService.getProposalsForGroup(groupId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Proposal>> getMyProposals(@AuthenticationPrincipal User user) {
    return ResponseEntity.ok(proposalService.getProposalsForUserGroups(user));
    }
}