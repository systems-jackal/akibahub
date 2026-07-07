requireAuth();

async function loadDashboard() {
  try {
    const data = await fetchDashboard();
    const stats = document.getElementById('stats-container');
    stats.innerHTML = `
      <div class="stat-card">
        <div class="number">KES ${formatCurrency(data.personalBalance)}</div>
        <div class="label">Personal Balance</div>
      </div>
      <div class="stat-card">
        <div class="number">KES ${formatCurrency(data.groupBalance)}</div>
        <div class="label">Group Savings</div>
      </div>
      <div class="stat-card">
        <div class="number">${data.activeGroups}</div>
        <div class="label">Active Groups</div>
      </div>
      <div class="stat-card">
        <div class="number">${data.pendingVotes}</div>
        <div class="label">Pending Votes</div>
      </div>
    `;
    // Load recent proposals
    const proposals = await fetchMyProposals();
    const list = document.getElementById('recent-proposals');
    if (proposals && proposals.length > 0) {
      list.innerHTML = proposals.slice(0, 5).map(p => `
        <div class="proposal-card">
          <strong>${p.title}</strong> – ${p.status} – Amount: KES ${formatCurrency(p.amount)}
        </div>
      `).join('');
    } else {
      list.innerHTML = '<p>No proposals yet.</p>';
    }
  } catch (err) {
    showAlert(err.message, 'error');
  }
}

loadDashboard();