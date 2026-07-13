requireAuth();

async function loadProposals() {
  try {
    const proposals = await fetchMyProposals();
    const container = document.getElementById('proposals-list');
    if (proposals.length === 0) {
      container.innerHTML = '<p>No proposals yet. When a group member proposes a withdrawal, you will see it here and can vote.</p>';
    } else {
      container.innerHTML = proposals.map(p => `
        <div class="proposal-card">
          <strong>${escapeHtml(p.title)}</strong>
          <p>Amount: KES ${formatCurrency(p.amount)} &nbsp;|&nbsp; Status: <span class="status-${p.status.toLowerCase()}">${escapeHtml(p.status)}</span></p>
          <p>Group: ${escapeHtml(p.group?.name || 'Unknown Group')} (ID: ${p.group?.id || 'N/A'})</p>
          ${p.status === 'OPEN' ? `<button class="btn-primary small" onclick="vote('${p.id}')">Vote YES</button>` : ''}
        </div>
      `).join('');
    }
    window.vote = async (proposalId) => {
      try {
        await voteOnProposal(proposalId, 'YES');
        showAlert('Your vote has been recorded.');
        loadProposals();
      } catch (e) { showAlert(e.message, 'error'); }
    };
  } catch (err) {
    showAlert(err.message, 'error');
  }
}

loadProposals();