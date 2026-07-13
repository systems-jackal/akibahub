requireAuth();

/**
 * Records a user's vote on a proposal.
 * @param {string} proposalId
 * @param {string} decision - "YES" or "NO"
 */
window.vote = async (proposalId, decision) => {
    try {
        await voteOnProposal(proposalId, decision);

        showAlert(`Your ${decision} vote has been recorded.`);

        await loadProposals();

    } catch (e) {
        showAlert(e.message, "error");
    }
};

async function loadProposals() {
    try {
        const proposals = await fetchMyProposals();

        const container = document.getElementById("proposals-list");

        if (!proposals || proposals.length === 0) {
            container.innerHTML =
                "<p>No proposals yet. When a group member proposes a withdrawal, you will see it here and can vote.</p>";
            return;
        }

        container.innerHTML = proposals
            .map(
                (p) => `
            <div class="proposal-card">

                <strong>${escapeHtml(p.title)}</strong>

                <p>
                    Amount:
                    <strong>KES ${formatCurrency(p.amount)}</strong>
                    &nbsp;|&nbsp;
                    Status:
                    <span class="status-${escapeHtml(
                        p.status.toLowerCase()
                    )}">
                        ${escapeHtml(p.status)}
                    </span>
                </p>

                <p>
                    Group:
                    ${escapeHtml(p.group?.name || "Unknown Group")}
                    (ID:
                    ${escapeHtml(String(p.group?.id || "N/A"))})
                </p>

                ${
                    p.status === "OPEN"
                        ? `
                    <div class="proposal-actions">

                        <button
                            class="btn-primary small"
                            onclick="vote('${escapeHtml(
                                String(p.id)
                            )}','YES')">
                            Vote YES
                        </button>

                        <button
                            class="btn-secondary small"
                            onclick="vote('${escapeHtml(
                                String(p.id)
                            )}','NO')">
                            Vote NO
                        </button>

                    </div>
                `
                        : ""
                }

            </div>
        `
            )
            .join("");

    } catch (err) {
        showAlert(err.message, "error");
    }
}

loadProposals();