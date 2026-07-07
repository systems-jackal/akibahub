// app.js – Reusable Akiba Hub API functions

async function loadDashboardData(token) {
  const headers = { 'Authorization': `Bearer ${token}` };
  try {
    const [walletsRes, groupsRes] = await Promise.all([
      fetch('/api/wallets/me', { headers }),
      fetch('/api/groups', { headers })
    ]);
    const wallets = await walletsRes.json();
    const groups = await groupsRes.json();

    renderWallets(wallets);
    renderGroups(groups, token);
  } catch (err) {
    console.error(err);
  }
}

function renderWallets(wallets) {
  const container = document.getElementById('wallets');
  if (!container) return;
  container.innerHTML = wallets.map(w => `
    <div class="wallet-item">
      <strong>${w.type}</strong> – Balance: ${w.balance}
    </div>
  `).join('');
}

function renderGroups(groups, token) {
  const container = document.getElementById('groups-list');
  if (!container) return;
  container.innerHTML = groups.map(g => `
    <div class="group-item">
      <strong>${g.name}</strong> (ID: ${g.id}) – ${g.description || ''}
      <button class="primary-btn small" onclick="contributeToGroup(${g.id}, '${token}')">Contribute</button>
      <button class="primary-btn small" onclick="loadProposals(${g.id}, '${token}')">View Proposals</button>
    </div>
  `).join('');
}

async function deposit(token, amount) {
  if (!amount || amount <= 0) return alert('Enter a valid amount');
  await fetch('/api/wallets/me/personal/deposit', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    body: JSON.stringify({ amount: parseFloat(amount) })
  });
  loadDashboardData(token);
}

async function createGroup(token, name, description) {
  await fetch('/api/groups', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    body: JSON.stringify({ name, description })
  });
  loadDashboardData(token);
}

async function joinGroup(token, groupId) {
  await fetch(`/api/groups/${groupId}/join`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  loadDashboardData(token);
}

async function contributeToGroup(groupId, token) {
  const amount = prompt('Amount to contribute:');
  if (!amount) return;
  await fetch(`/api/wallets/groups/${groupId}/contribute`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    body: JSON.stringify({ amount: parseFloat(amount) })
  });
  loadDashboardData(token);
}

async function loadProposals(groupId, token) {
  const res = await fetch(`/api/groups/${groupId}/proposals`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const proposals = await res.json();
  const container = document.getElementById('proposals-list');
  if (container) {
    container.innerHTML = proposals.map(p => `
      <div class="proposal-item">
        <strong>${p.title}</strong> – Amount: ${p.amount}, Status: ${p.status}
        ${p.status === 'OPEN' ? `<button class="primary-btn small" onclick="voteOnProposal(${p.id}, '${token}')">Vote YES</button>` : ''}
      </div>
    `).join('');
  }
}

async function createProposal(token, groupId, title, description, amount) {
  await fetch(`/api/groups/${groupId}/proposals`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    body: JSON.stringify({ title, description, amount: parseFloat(amount) })
  });
  loadDashboardData(token);
}

async function voteOnProposal(proposalId, token) {
  await fetch(`/api/proposals/${proposalId}/vote`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    body: JSON.stringify({ vote: 'YES' })
  });
  loadDashboardData(token);
}