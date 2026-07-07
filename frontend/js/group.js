if (!getToken()) {
  window.location.href = 'login.html';
}

const params = new URLSearchParams(window.location.search);
const groupId = params.get('id');
if (!groupId) {
  window.location.href = 'dashboard.html';
}

document.getElementById('logout-btn').addEventListener('click', () => {
  localStorage.removeItem('akiba_token');
  localStorage.removeItem('akiba_phone');
  window.location.href = 'index.html';
});

async function loadGroup() {
  const group = await fetchGroup(groupId);
  document.getElementById('group-name').textContent = group.name;
  document.getElementById('group-desc').textContent = group.description || '';

  // Get group wallet
  const wallets = await fetchWallets();
  const groupWallet = wallets.find(w => w.type === 'GROUP' && w.groupId == groupId);
  document.getElementById('group-balance').textContent = groupWallet ? groupWallet.balance : '0.0000';

  // Members
  const members = await fetchMembers(groupId);
  document.getElementById('members-list').innerHTML = members.map(m => `
    <div class="member-item">${m.user ? m.user.phoneNumber : 'Unknown'}</div>
  `).join('');

  // Proposals
  const proposals = await fetchProposals(groupId);
  const proposalsDiv = document.getElementById('proposals-list');
  if (proposals.length === 0) {
    proposalsDiv.innerHTML = '<p>No proposals yet.</p>';
  } else {
    proposalsDiv.innerHTML = proposals.map(p => `
      <div class="proposal-item">
        <strong>${p.title}</strong> – Amount: ${p.amount}, Status: ${p.status}
        ${p.status === 'OPEN' ? `<button class="btn-primary small" onclick="vote(${p.id})">Vote YES</button>` : ''}
      </div>
    `).join('');
  }

  // Contribute
  document.getElementById('contribute-btn').addEventListener('click', async () => {
    const amount = prompt('Amount to contribute:');
    if (amount) {
      await contributeToGroup(groupId, amount);
      loadGroup();
    }
  });

  // New proposal
  document.getElementById('new-proposal-btn').addEventListener('click', async () => {
    const title = prompt('Proposal title:');
    const desc = prompt('Description (optional):');
    const amount = prompt('Amount:');
    if (title && amount) {
      await createProposal(groupId, title, desc, amount);
      loadGroup();
    }
  });
}

window.vote = async function(proposalId) {
  await voteOnProposal(proposalId);
  loadGroup();
};

loadGroup();