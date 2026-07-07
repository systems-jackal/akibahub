// Redirect if not logged in
if (!getToken()) {
  window.location.href = 'login.html';
}

// Logout
document.getElementById('logout-btn').addEventListener('click', () => {
  localStorage.removeItem('akiba_token');
  localStorage.removeItem('akiba_phone');
  window.location.href = 'index.html';
});

// Deposit
document.getElementById('deposit-btn').addEventListener('click', () => {
  document.getElementById('deposit-form').classList.toggle('hidden');
});
document.getElementById('do-deposit').addEventListener('click', async () => {
  const amount = document.getElementById('deposit-amount').value;
  await deposit(amount);
  await renderDashboard();
});

// Create group
document.getElementById('create-group-btn').addEventListener('click', () => {
  document.getElementById('create-group-form').classList.toggle('hidden');
});
document.getElementById('do-create-group').addEventListener('click', async () => {
  const name = document.getElementById('group-name').value;
  const desc = document.getElementById('group-desc').value;
  await createGroup(name, desc);
  await renderDashboard();
});

// Join group
document.getElementById('do-join-group').addEventListener('click', async () => {
  const id = document.getElementById('join-group-id').value;
  await joinGroup(id);
  await renderDashboard();
});

// New proposal
document.getElementById('create-proposal-btn').addEventListener('click', () => {
  document.getElementById('create-proposal-form').classList.toggle('hidden');
});
document.getElementById('do-create-proposal').addEventListener('click', async () => {
  const groupId = document.getElementById('proposal-group-id').value;
  const title = document.getElementById('proposal-title').value;
  const desc = document.getElementById('proposal-desc').value;
  const amount = document.getElementById('proposal-amount').value;
  await createProposal(groupId, title, desc, amount);
  await renderDashboard();
});

async function renderDashboard() {
  const wallets = await fetchWallets();
  document.getElementById('wallets').innerHTML = wallets.map(w => `
    <div class="wallet-item">
      <strong>${w.type}</strong> – Balance: ${w.balance}
    </div>
  `).join('');

  const groups = await fetchGroups();
  const groupsDiv = document.getElementById('groups-list');
  if (groups.length === 0) {
    groupsDiv.innerHTML = '<p>No groups yet.</p>';
  } else {
    groupsDiv.innerHTML = groups.map(g => `
      <div class="group-item">
        <a href="group.html?id=${g.id}"><strong>${g.name}</strong></a> (ID: ${g.id}) – ${g.description || ''}
        <button class="btn-primary small" onclick="contributeToGroup(${g.id})">Contribute</button>
      </div>
    `).join('');
  }

  // Show proposals from all groups? Not ideal. We'll just leave the proposals section for manual trigger via the group page.
  document.getElementById('proposals-list').innerHTML = '<p>Visit a group to see proposals.</p>';
}

// Contribute helper (called from inline onclick, still need to define globally)
window.contributeToGroup = async function(groupId) {
  const amount = prompt('Amount to contribute:');
  if (amount) {
    await contributeToGroup(groupId, amount);
    await renderDashboard();
  }
};

renderDashboard();