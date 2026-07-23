requireAuth();

async function loadDashboard() {
  try {
    const data = await fetchDashboard();
    document.getElementById('personal-balance').textContent = formatCurrency(data.personalBalance);

    const tickerBalance = document.getElementById('ticker-balance');
    if (tickerBalance) {
      const total = parseFloat(data.personalBalance) + parseFloat(data.groupBalance || 0);
      tickerBalance.textContent = formatCurrency(total);
    }

    const chartEl = document.getElementById('asset-distribution-chart');
    if (chartEl && data.assetDistribution) {
      renderBlockBarChart(chartEl, data.assetDistribution.map(d => ({ label: d.label, value: parseFloat(d.value) })));
    }

    const groups = await fetchMyGroups();

    if (groups.length === 0) {
      // Solo saver – hide group sections, show prompt
      document.getElementById('groups-section').classList.add('hidden');
      document.getElementById('solo-saver-prompt').classList.remove('hidden');
    } else {
      // Group saver – show group sections, hide solo prompt
      document.getElementById('groups-section').classList.remove('hidden');
      document.getElementById('solo-saver-prompt').classList.add('hidden');

      // Groups summary
      const summary = document.getElementById('groups-summary');
      summary.innerHTML = `
        <p>You are in <strong>${groups.length}</strong> group(s) with a total balance of <strong>KES ${formatCurrency(data.groupBalance)}</strong>.</p>
        <ul>
          ${groups.map(g => `<li><a href="group.html?id=${g.id}">${escapeHtml(g.name)}</a></li>`).join('')}
        </ul>
      `;

      // Recent proposals
      const proposals = await fetchMyProposals();
      const list = document.getElementById('recent-proposals');
      if (proposals && proposals.length > 0) {
        list.innerHTML = proposals.slice(0, 5).map(p => {
          const total = p.totalMembers || 1;
          const yesPct = Math.round((p.yesVotes / total) * 100);
          const noPct = Math.round((p.noVotes / total) * 100);
          const groupId = p.group?.id;
          return `
          <div class="proposal-card">
            <div class="proposal-title">${escapeHtml(p.title)} <span class="badge status-${p.status.toLowerCase()}">${escapeHtml(p.status)}</span></div>
            <div class="proposal-meta">
              KES <span class="proposal-amount">${formatCurrency(p.amount)}</span>
              ${groupId ? ` · <a href="group.html?id=${groupId}">${escapeHtml(p.group?.name || 'Group')}</a>` : ''}
            </div>
            <div class="vote-tally">
              <div class="vote-tally-bar">
                <div class="yes-fill" style="width:${yesPct}%"></div>
                <div class="no-fill" style="width:${noPct}%"></div>
              </div>
              <div class="vote-tally-labels"><span>YES: ${p.yesVotes}</span><span>NO: ${p.noVotes}</span></div>
            </div>
          </div>
        `;
        }).join('');
      } else {
        list.innerHTML = '<p>No proposals yet.</p>';
      }
    }
  } catch (err) {
    showAlert(err.message, 'error');
  }
}

// Quick actions
document.getElementById('quick-deposit').addEventListener('click', async () => {
  const amount = prompt('Enter amount to deposit:');
  const n = parseFloat(amount);
  if (!amount) return;
  if (!n || n <= 0) return showAlert('Please enter a valid amount.', 'error');
  try {
    await deposit(n);
    showAlert('Deposit successful');
    loadDashboard();
  } catch (e) { showAlert(e.message, 'error'); }
});

document.getElementById('quick-withdraw').addEventListener('click', async () => {
  const amount = prompt('Enter amount to withdraw:');
  const n = parseFloat(amount);
  if (!amount) return;
  if (!n || n <= 0) return showAlert('Please enter a valid amount.', 'error');
  try {
    await withdraw(n);
    showAlert('Withdrawal successful');
    loadDashboard();
  } catch (e) { showAlert(e.message, 'error'); }
});

document.getElementById('quick-join-group').addEventListener('click', async () => {
  const code = prompt('Enter the 6-character invite code:');
  if (!code) return;
  try {
    const group = await joinGroup(code.trim());
    showAlert(`Joined ${group.name}!`);
    window.location.href = `group.html?id=${group.id}`;
  } catch (e) {
    showAlert(e.message, 'error');
  }
});

loadDashboard();