requireAuth();

window.vote = async (proposalId, decision) => {
  try {
    await voteOnProposal(proposalId, decision);
    showAlert(`Your ${decision} vote has been recorded.`);
    await loadProposals();
  } catch (e) {
    showAlert(e.message, 'error');
  }
};

async function loadProposals() {
  try {
    const proposals = await fetchMyProposals();
    const container = document.getElementById('proposals-list');

    if (!proposals || proposals.length === 0) {
      container.innerHTML =
        '<p>No proposals yet. Open a group, contribute funds, then create a withdrawal proposal for members to vote on.</p>';
      return;
    }

    container.innerHTML = proposals.map(p => {
      const total = p.totalMembers || 1;
      const yesPct = Math.round((p.yesVotes / total) * 100);
      const noPct = Math.round((p.noVotes / total) * 100);
      const needed = Math.floor(total / 2) + 1;
      const groupId = p.group?.id;
      const groupName = p.group?.name || 'Unknown Group';
      const isOpen = p.status === 'OPEN';

      return `
        <div class="proposal-card">
          <div class="dao-stepper">
            <span class="step done">PROPOSE</span><span class="arrow">→</span>
            <span class="step ${isOpen ? 'active' : 'done'}">VOTE</span><span class="arrow">→</span>
            <span class="step ${p.status === 'APPROVED' ? 'done' : ''}">EXECUTE</span>
          </div>

          <div class="proposal-title">
            ${escapeHtml(p.title)}
            <span class="badge status-${escapeHtml(String(p.status).toLowerCase())}">${escapeHtml(p.status)}</span>
          </div>

          <div class="proposal-meta">
            Amount: KES <span class="proposal-amount">${formatCurrency(p.amount)}</span>
            &nbsp;·&nbsp;
            Group: ${groupId
              ? `<a href="group.html?id=${groupId}">${escapeHtml(groupName)}</a>`
              : escapeHtml(groupName)}
            ${isOpen ? ` · Needs ${needed} YES` : ''}
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

          ${isOpen && !p.myVote ? `
            <div class="dial-group">
              <button type="button" class="dial-option dial-yes" onclick="vote('${escapeHtml(String(p.id))}','YES')">YES</button>
              <button type="button" class="dial-option dial-no" onclick="vote('${escapeHtml(String(p.id))}','NO')">NO</button>
            </div>
          ` : isOpen && p.myVote
            ? `<p class="text-muted">You voted ${escapeHtml(p.myVote)}.</p>`
            : ''}
        </div>
      `;
    }).join('');
  } catch (err) {
    showAlert(err.message, 'error');
  }
}

loadProposals();
