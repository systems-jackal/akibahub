requireAuth();
const params = new URLSearchParams(location.search);
const groupId = params.get('id');
if (!groupId) location.href = 'groups.html';

let currentUserId = null;

async function loadGroup() {
  try {
    const group = await fetchGroup(groupId);
    document.getElementById('group-name').textContent = group.name;
    document.getElementById('group-desc').textContent = group.description || '';

    // Current user info
    const user = await fetchCurrentUser();
    currentUserId = user.id;

    // Show invite section if user is the creator
    if (group.createdBy && group.createdBy.id === currentUserId) {
      const inviteSection = document.getElementById('invite-section');
      inviteSection.classList.remove('hidden');
      const inviteRes = await fetch(`/api/groups/${groupId}/invite`, {
        method: 'POST',
        headers: authHeaders()
      });
      const inviteData = await inviteRes.json();
      if (inviteData.success) {
        document.getElementById('invite-link-output').value = inviteData.data;
        document.getElementById('copy-invite').addEventListener('click', () => {
          document.getElementById('invite-link-output').select();
          document.execCommand('copy');
          showAlert('Invite link copied to clipboard.');
        });
      }
    }

    // Group wallet balance
    const wallets = await fetchMyWallets();
    const groupWallet = wallets.find(w => w.group && w.group.id == groupId && w.type === 'GROUP');
    document.getElementById('group-balance').textContent = formatCurrency(groupWallet?.balance || 0);

    // Members
    const membersRes = await fetch(`/api/groups/${groupId}/members`, { headers: authHeaders() });
    const membersData = await membersRes.json();
    const membersList = document.getElementById('members-list');
    if (membersData.data && membersData.data.length > 0) {
      membersList.innerHTML = membersData.data.map(m => `<div class="member-item">${m.user?.phoneNumber || 'Unknown'}</div>`).join('');
    } else {
      membersList.innerHTML = '<p>No members found.</p>';
    }

    // Proposals
    const proposals = await fetchProposalsForGroup(groupId);
    const proposalsList = document.getElementById('proposals-list');
    if (proposals.length === 0) {
      proposalsList.innerHTML = '<p>No proposals yet.</p>';
    } else {
      proposalsList.innerHTML = proposals.map(p => `
        <div class="proposal-card">
          <strong>${p.title}</strong>
          <p>Amount: KES ${formatCurrency(p.amount)} &nbsp;|&nbsp; Status: ${p.status}</p>
          ${p.status === 'OPEN' ? `<button class="btn-primary small" onclick="vote('${p.id}')">Vote YES</button>` : ''}
        </div>
      `).join('');
    }

    window.vote = async (proposalId) => {
      try {
        await voteOnProposal(proposalId, 'YES');
        showAlert('Vote recorded.');
        loadGroup();
      } catch (e) { showAlert(e.message, 'error'); }
    };
  } catch (err) {
    showAlert(err.message, 'error');
  }
}

document.getElementById('contribute-btn').addEventListener('click', async () => {
  const amount = prompt('Enter amount to contribute (KES):');
  if (amount) {
    try {
      await contributeToGroup(groupId, parseFloat(amount));
      showAlert('Contribution successful.');
      loadGroup();
    } catch (e) { showAlert(e.message, 'error'); }
  }
});

document.getElementById('new-proposal-btn').addEventListener('click', async () => {
  const title = prompt('Proposal title:');
  if (!title) return;
  const desc = prompt('Description (optional):');
  const amount = prompt('Amount (KES):');
  if (!amount) return;
  try {
    await createProposal(groupId, title, desc, parseFloat(amount));
    showAlert('Proposal created successfully.');
    loadGroup();
  } catch (e) { showAlert(e.message, 'error'); }
});

loadGroup();