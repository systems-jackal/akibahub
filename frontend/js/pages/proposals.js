requireAuth();

async function loadProposals() {
  try {
    const proposals = await fetchMyProposals();
    const container = document.getElementById('proposals-list');
    if (proposals.length === 0) {
      container.innerHTML = '<p>No proposals yet.</p>';
    } else {
      container.innerHTML = proposals.map(p => `
        <div class="proposal-card">
          <strong>${p.title}</strong> – Group: ${p.group?.id || 'N/A'} – Amount: KES ${formatCurrency(p.amount)} – Status: ${p.status}
          ${p.status === 'OPEN' ? `<button class="btn-primary small" onclick="vote('${p.id}')">Vote YES</button>` : ''}
        </div>
      `).join('');
    }
    window.vote = async (proposalId) => {
      try {
        await voteOnProposal(proposalId, 'YES');
        showAlert('Vote recorded!');
        loadProposals();
      } catch (e) { showAlert(e.message, 'error'); }
    };
  } catch (err) {
    showAlert(err.message, 'error');
  }
}

loadProposals();