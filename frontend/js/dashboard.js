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

  const msgEl = document.getElementById('proposal-message');
  if (!msgEl) {
    // create a message element if not present (add in HTML or dynamically)
    const p = document.createElement('p');
    p.id = 'proposal-message';
    p.style.marginTop = '10px';
    document.getElementById('create-proposal-form').appendChild(p);
  }

  try {
    await createProposal(groupId, title, desc, amount);
    // Success
    document.getElementById('proposal-message').textContent = 'Proposal created successfully!';
    document.getElementById('proposal-message').style.color = 'green';
    // Clear inputs
    document.getElementById('proposal-group-id').value = '';
    document.getElementById('proposal-title').value = '';
    document.getElementById('proposal-desc').value = '';
    document.getElementById('proposal-amount').value = '';
    // Hide form after a short delay (optional)
    setTimeout(() => {
      document.getElementById('create-proposal-form').classList.add('hidden');
      document.getElementById('proposal-message').textContent = '';
    }, 2000);
    await renderDashboard();
  } catch (err) {
    document.getElementById('proposal-message').textContent = err.message;
    document.getElementById('proposal-message').style.color = 'red';
  }
});

async function renderDashboard() {
  const wallets = await fetchWallets();
  document.getElementById('wallets').innerHTML = wallets.map(w => `
    <div class="wallet-item">
      <strong>${w.type}</strong> – Balance: ${w.balance}
    </div>
  `).join('');

  const groups = await fetchMyGroups();
  const groupsDiv = document.getElementById('groups-list');
  if (groups.length === 0) {
    groupsDiv.innerHTML = '<p>No groups yet. Create or join one!</p>';
  } else {
    groupsDiv.innerHTML = groups.map(g => `
      <div class="group-item">
        <a href="group.html?id=${g.id}"><strong>${g.name}</strong></a> (ID: ${g.id}) – ${g.description || ''}
        <button class="btn-primary small" onclick="contributeToGroup(${g.id})">Contribute</button>
      </div>
    `).join('');
  }

  // Fetch and display proposals
  const proposals = await fetchMyProposals();
  const proposalsDiv = document.getElementById('proposals-list');
  if (proposals.length === 0) {
    proposalsDiv.innerHTML = '<p>No proposals yet.</p>';
  } else {
    proposalsDiv.innerHTML = proposals.map(p => `
      <div class="proposal-item">
        <strong>${p.title}</strong> – Group: ${p.group?.name || 'Group ' + p.group?.id} – Amount: ${p.amount}, Status: ${p.status}
        ${p.status === 'OPEN' ? `<button class="btn-primary small" onclick="vote(${p.id})">Vote YES</button>` : ''}
      </div>
    `).join('');
  }
}

// Global vote function for inline onclick
window.vote = async function(proposalId) {
  await voteOnProposal(proposalId);
  renderDashboard();
};

// Contribute helper
window.contributeToGroup = async function(groupId) {
  const amount = prompt('Amount to contribute:');
  if (amount) {
    await contributeToGroup(groupId, amount);
    await renderDashboard();
  }
};

renderDashboard();