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
                (p) => {
                    const total = p.totalMembers || 1;
                    const yesPct = Math.round((p.yesVotes / total) * 100);
                    const noPct = Math.round((p.noVotes / total) * 100);
                    return `
            <div class="proposal-card">
                <div class="dao-stepper">
                    <span class="step done">PROPOSE</span><span class="arrow">→</span>
                    <span class="step ${p.status === 'OPEN' ? 'active' : 'done'}">VOTE</span><span class="arrow">→</span>
                    <span class="step ${p.status === 'APPROVED' ? 'done' : ''}">EXECUTE</span>
                </div>

                <div class="proposal-title">
                    ${escapeHtml(p.title)}
                    <span class="badge status-${escapeHtml(p.status.toLowerCase())}">${escapeHtml(p.status)}</span>
                </div>

                <div class="proposal-meta">
                    Amount: KES <span class="proposal-amount">${formatCurrency(p.amount)}</span>
                    &nbsp;·&nbsp;
                    Group: ${escapeHtml(p.group?.name || "Unknown Group")}
                </div>

                <div class="vote-tally">
                    <div class="vote-tally-bar">
                        <div class="yes-fill" style="width:${yesPct}%"></div>
                        <div class="no-fill" style="width:${noPct}%"></div>
                    </div>
                    <div class="vote-tally-labels">
                        <span>YES: ${p.yesVotes}</span>
                        <span>NO: ${p.noVotes}</span>
                        <span>${p.yesVotes + p.noVotes}/${total} voted</span>
                    </div>
                </div>

                ${
                    p.status === "OPEN" && !p.myVote
                        ? `
                    <div class="dial-group">
                        <div class="dial-option dial-yes" onclick="vote('${escapeHtml(String(p.id))}','YES')">YES</div>
                        <div class="dial-option dial-no" onclick="vote('${escapeHtml(String(p.id))}','NO')">NO</div>
                    </div>
                `
                        : p.status === "OPEN" && p.myVote
                        ? `<p class="text-muted">You voted ${escapeHtml(p.myVote)}.</p>`
                        : ""
                }
            </div>
        `;
                }
            )
            .join("");

    } catch (err) {
        showAlert(err.message, "error");
    }
}

loadProposals();