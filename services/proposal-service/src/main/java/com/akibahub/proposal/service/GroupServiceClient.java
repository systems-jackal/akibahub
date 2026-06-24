package com.akibahub.proposal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@Slf4j
public class GroupServiceClient {

    private final WebClient groupWebClient;
    private final String internalKey;

    public GroupServiceClient(@Qualifier("groupWebClient") WebClient groupWebClient,
                              @Value("${services.internal-key}") String internalKey) {
        this.groupWebClient = groupWebClient;
        this.internalKey = internalKey;
    }

    public boolean isMember(String groupId, String userId) {
        try {
            Map<?, ?> response = groupWebClient.get()
                    .uri("/groups/internal/{groupId}/is-member/{userId}", groupId, userId)
                    .header("X-Service-Key", internalKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return response != null && Boolean.TRUE.equals(response.get("member"));
        } catch (Exception e) {
            log.error("Failed to check group membership: {}", e.getMessage());
            throw new RuntimeException("Could not verify group membership");
        }
    }

    public int getMemberCount(String groupId) {
        try {
            Map<?, ?> response = groupWebClient.get()
                    .uri("/groups/{groupId}", groupId)
                    .header("X-Service-Key", internalKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (response != null && response.containsKey("memberCount")) {
                return ((Number) response.get("memberCount")).intValue();
            }
            return 0;
        } catch (Exception e) {
            log.error("Failed to get member count: {}", e.getMessage());
            return 0;
        }
    }
}
