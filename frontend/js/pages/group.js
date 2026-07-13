requireAuth();

const params = new URLSearchParams(location.search);
const groupId = params.get('id');
if (!groupId) location.href = 'groups.html';

let currentUserId = null;
let listenersInitialized = false;

async function loadGroup() {
  try {
    const group = await fetchGroup(groupId);
    document.getElementById('group-name').textContent = group.name;
    document.getElementById('group-desc').textContent = group.description || '';

    // Render Group Rules conditionally
    const rulesBox = document.getElementById('group-rules-box');
    const rulesText = document.getElementById('group-rules-text');
    if (group.rules && group.rules.trim() !== '') {
      rulesText.textContent = group.rules;
      rulesBox.classList.remove('hidden');
    } else {
      rulesBox.classList.add('hidden');
    }

    // Current user context assignment
    const user = await fetchCurrentUser();
    currentUserId = user.id;

    // Async validation and resolution of invite infrastructure
    if (group.createdBy && group.createdBy.id === currentUserId) {
      const inviteSection = document.getElementById('invite-section');
      inviteSection.classList.remove('hidden');

      const inviteRes = await fetch(`/api/groups/${groupId}/invite`, {
        method: 'POST',
        headers: authHeaders()
      });
      const inviteData = await inviteRes.json();
      if (inviteData.success) {
        document.getElementById('invite-code-display').textContent = inviteData.data;
        
        // Single static attachment pattern to prevent duplication memory leaks
        const copyBtn = document.getElementById('copy-invite');
        copyBtn.onclick = () => {
          navigator.clipboard.writeText(inviteData.data)
            .then(() => showAlert('Invite code copied.'))
            .catch(() => showAlert('Failed to copy code.', 'error'));
        };
      }
    }

    // Financial component evaluation
    const wallets = await fetchMyWallets();
    const groupWallet = wallets.find(w => w.group && w.group.id == groupId && w.type === 'GROUP');
    document.getElementById('group-balance').textContent = formatCurrency(groupWallet?.balance || 0);

    // Metadata & member calculations
    const statsRes = await fetch(`/api/groups/${groupId}/stats`, { headers: authHeaders() });
    const statsData = await statsRes.json();
    if (statsData.success) {
      document.getElementById('member-count').textContent = statsData.data.members;
    }

    // Dynamic proposal system tracking
    const proposals = await fetchProposalsForGroup(groupId);
    const proposalsList = document.getElementById('proposals-list');
    if (!proposals || proposals.length === 0) {
      proposalsList.innerHTML = '<p>No proposals yet.</p>';
    } else {
      proposalsList.innerHTML = proposals.map(p => `
        <div class="proposal-card">
          <strong>${escapeHtml(p.title)}</strong>
          <p>Amount: KES ${formatCurrency(p.amount)} &nbsp;|&nbsp; Status: <span class="badge">${escapeHtml(p.status)}</span></p>
          ${p.status === 'OPEN' ? `<button class="btn-primary small" onclick="vote('${p.id}')">Vote YES</button>` : ''}
        </div>
      `).join('');
    }

    // Expose voting functionality globally safely
    window.vote = async (proposalId) => {
      try {
        await voteOnProposal(proposalId, 'YES');
        showAlert('Vote recorded.');
        loadGroup();
      } catch (e) { 
        showAlert(e.message, 'error'); 
      }
    };

    // Initialize events only once
    initStaticListeners();

  } catch (err) {
    showAlert(err.message, 'error');
  }
}

// Separate permanent events outside the loop lifecycle
function initStaticListeners() {
  if (listenersInitialized) return;

  // Toggle member view layout dynamically
  document.getElementById('toggle-members').addEventListener('click', async () => {
    const list = document.getElementById('members-list');
    const toggleBtn = document.getElementById('toggle-members');
    
    if (list.classList.contains('hidden')) {
      try {
        const membersRes = await fetch(`/api/groups/${groupId}/members`, { headers: authHeaders() });
        const membersData = await membersRes.json();
        if (membersData.success && membersData.data.length > 0) {
          list.innerHTML = membersData.data.map(m => `
            <div class="member-item">
              👤 ${escapeHtml(m.user?.fullName || m.user?.phoneNumber || 'Unknown')}
            </div>
          `).join('');
        } else {
          list.innerHTML = '<p class="text-muted p-10">No members found.</p>';
        }
        list.classList.remove('hidden');
        toggleBtn.textContent = 'Hide Members';
      } catch (err) {
        showAlert('Could not load members.', 'error');
      }
    } else {
      list.classList.add('hidden');
      toggleBtn.textContent = 'View Members';
    }
  });

  // Financial contribution management
  document.getElementById('contribute-btn').addEventListener('click', async () => {
    const amount = prompt('Enter amount to contribute (KES):');
    if (amount && !isNaN(amount) && parseFloat(amount) > 0) {
      try {
        await contributeToGroup(groupId, parseFloat(amount));
        showAlert('Contribution successful.');
        loadGroup();
      } catch (e) { 
        showAlert(e.message, 'error'); 
      }
    } else if (amount) {
      showAlert('Please enter a valid numeric amount.', 'error');
    }
  });

  // Proposal execution mechanism
  document.getElementById('new-proposal-btn').addEventListener('click', async () => {
    const title = prompt('Proposal title:');
    if (!title || title.trim() === '') return;
    
    const desc = prompt('Description (optional):');
    const amount = prompt('Amount (KES):');
    if (!amount || isNaN(amount) || parseFloat(amount) <= 0) {
      return showAlert('A valid structural amount is required.', 'error');
    }
    
    try {
      await createProposal(groupId, title.trim(), desc?.trim(), parseFloat(amount));
      showAlert('Proposal created successfully.');
      loadGroup();
    } catch (e) { 
      showAlert(e.message, 'error'); 
    }
  });

  listenersInitialized = true;
}

// Kickstart rendering cycle
loadGroup();