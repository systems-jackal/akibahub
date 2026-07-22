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

      const inviteCode = await fetchGroupInviteCode(groupId);
      document.getElementById('invite-code-display').textContent = inviteCode;

      // Single static attachment pattern to prevent duplication memory leaks
      const copyBtn = document.getElementById('copy-invite');
      copyBtn.onclick = () => {
        navigator.clipboard.writeText(inviteCode)
          .then(() => showAlert('Invite code copied.'))
          .catch(() => showAlert('Failed to copy code.', 'error'));
      };
    }

    // Financial component evaluation
    const wallets = await fetchMyWallets();
    const groupWallet = wallets.find(w => w.group && w.group.id == groupId && w.type === 'GROUP');
    document.getElementById('group-balance').textContent = formatCurrency(groupWallet?.balance || 0);

    // Metadata & member calculations
    const stats = await fetchGroupStats(groupId);
    document.getElementById('member-count').textContent = stats.members;

    // Dynamic proposal system tracking
    const proposals = await fetchProposalsForGroup(groupId);
    const proposalsList = document.getElementById('proposals-list');
    if (!proposals || proposals.length === 0) {
      proposalsList.innerHTML = '<p>No proposals yet.</p>';
    } else {
      proposalsList.innerHTML = proposals.map(p => {
        const isOpen = p.status === 'OPEN';
        const isApproved = p.status === 'APPROVED';
        const total = p.totalMembers || 1;
        const yesPct = Math.round((p.yesVotes / total) * 100);
        const noPct = Math.round((p.noVotes / total) * 100);
        return `
        <div class="proposal-card">
          <div class="dao-stepper">
            <span class="step done">PROPOSE</span><span class="arrow">→</span>
            <span class="step ${isOpen ? 'active' : 'done'}">VOTE</span><span class="arrow">→</span>
            <span class="step ${isApproved ? 'done' : ''}">EXECUTE</span>
          </div>
          <div class="proposal-title">
            ${escapeHtml(p.title)}
            <span class="badge status-${p.status.toLowerCase()}">${escapeHtml(p.status)}</span>
          </div>
          <div class="proposal-meta">Amount: KES <span class="proposal-amount">${formatCurrency(p.amount)}</span></div>
          <div class="vote-tally">
            <div class="vote-tally-bar">
              <div class="yes-fill" style="width:${yesPct}%"></div>
              <div class="no-fill" style="width:${noPct}%"></div>
            </div>
            <div class="vote-tally-labels">
              <span>YES: ${p.yesVotes}</span>
              <span>NO: ${p.noVotes}</span>
              <span>${p.yesVotes + p.noVotes}/${total} voted</span>
            </div>
          </div>
          ${isOpen ? `
            <div class="dial-group" data-proposal-id="${p.id}">
              <div class="dial-option dial-yes" onclick="vote('${p.id}','YES')">YES</div>
              <div class="dial-option dial-no" onclick="vote('${p.id}','NO')">NO</div>
            </div>
          ` : ''}
        </div>
      `;
      }).join('');
    }

    // Growth-over-time analytics, sourced directly from the ledger
    const growthChartEl = document.getElementById('group-growth-chart');
    if (growthChartEl) {
      try {
        const growth = await fetchGroupGrowth(groupId);
        renderStepLineChart(growthChartEl, growth.map(g => ({ label: g.label, value: parseFloat(g.value) })));
      } catch (e) {
        growthChartEl.innerHTML = '<div class="chart-empty">ANALYTICS_UNAVAILABLE</div>';
      }
    }

    // Expose voting functionality globally safely
    window.vote = async (proposalId, decision) => {
      try {
        await voteOnProposal(proposalId, decision);
        showAlert(`Your ${decision} vote has been recorded.`);
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
        const members = await fetchGroupMembers(groupId);
        if (members && members.length > 0) {
          list.innerHTML = members.map(m => `
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