package com.akibahub.proposal.dto.request;

import com.akibahub.proposal.model.VoteValue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CastVoteRequest {

    @NotNull(message = "Vote value is required")
    private VoteValue vote;
}
