requireAuth();

const params = new URLSearchParams(location.search);
const groupId = params.get('id');
if (!groupId) location.href = 'groups.html';

let currentUserId = null;
let isCreator = false;
let membersVisible = false;
let listenersInitialized = false;
let votingInFlight = false;

function setBusy(btn, busy, labelWhenBusy) {
  if (!btn) return;
  btn.disabled = !!busy;
  if (busy && labelWhenBusy) {
    btn.dataset.prevLabel = btn.innerHTML;
    btn.innerHTML = `<span class="wrap">${labelWhenBusy}</span>`;
  } else if (!busy && btn.dataset.prevLabel) {
    btn.innerHTML = btn.dataset.prevLabel;
    delete btn.dataset.prevLabel;
  }
}

function renderProposals(proposals) {
  const proposalsList = document.getElementById('proposals-list');
  if (!proposals || proposals.length === 0) {
    proposalsList.innerHTML = '<p class="text-muted">No proposals yet. Create one below when the group has funds.</p>';
    return;
  }

  proposalsList.innerHTML = proposals.map(p => {
    const isOpen = p.status === 'OPEN';
    const isApproved = p.status === 'APPROVED';
    const isRejected = p.status === 'REJECTED';
    const total = p.totalMembers || 1;
    const yesPct = Math.round((p.yesVotes / total) * 100);
    const noPct = Math.round((p.noVotes / total) * 100);
    const needed = Math.floor(total / 2) + 1;
    return `
      <div class="proposal-card">
        <div class="dao-stepper">
          <span class="step done">PROPOSE</span><span class="arrow">→</span>
          <span class="step ${isOpen ? 'active' : 'done'}">VOTE</span><span class="arrow">→</span>
          <span class="step ${isApproved ? 'done' : ''}">EXECUTE</span>
        </div>
        <div class="proposal-title">
          ${escapeHtml(p.title)}
          <span class="badge status-${escapeHtml(String(p.status).toLowerCase())}">${escapeHtml(p.status)}</span>
        </div>
        ${p.description ? `<p class="text-muted small">${escapeHtml(p.description)}</p>` : ''}
        <div class="proposal-meta">
          Amount: KES <span class="proposal-amount">${formatCurrency(p.amount)}</span>
          ${isOpen ? ` · Needs ${needed} YES to approve` : ''}
          ${isApproved ? ' · Paid to proposer' : ''}
          ${isRejected ? ' · Rejected' : ''}
        </div>
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
        ${isOpen && !p.myVote ? `
          <div class="dial-group" data-proposal-id="${p.id}">
            <button type="button" class="dial-option dial-yes" data-vote="YES">YES</button>
            <button type="button" class="dial-option dial-no" data-vote="NO">NO</button>
          </div>
        ` : isOpen && p.myVote ? `
          <p class="text-muted">You voted ${escapeHtml(p.myVote)}. Waiting for other members…</p>
        ` : ''}
      </div>
    `;
  }).join('');

  proposalsList.querySelectorAll('.dial-group').forEach(group => {
    const proposalId = group.getAttribute('data-proposal-id');
    group.querySelectorAll('[data-vote]').forEach(btn => {
      btn.addEventListener('click', () => castVote(proposalId, btn.getAttribute('data-vote')));
    });
  });
}

async function castVote(proposalId, decision) {
  if (votingInFlight) return;
  votingInFlight = true;
  try {
    await voteOnProposal(proposalId, decision);
    showAlert(`Your ${decision} vote has been recorded.`);
    await loadGroup();
  } catch (e) {
    showAlert(e.message, 'error');
  } finally {
    votingInFlight = false;
  }
}

async function refreshMembersList() {
  const list = document.getElementById('members-list');
  const toggleBtn = document.getElementById('toggle-members');
  if (!membersVisible) return;
  try {
    const members = await fetchGroupMembers(groupId);
    if (members && members.length > 0) {
      list.innerHTML = members.map(m => `
        <div class="member-item">
          👤 ${escapeHtml(m.user?.fullName || m.user?.phoneNumber || 'Unknown')}
          ${Number(m.user?.id) === Number(currentUserId) ? ' <span class="text-muted">(you)</span>' : ''}
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
}

async function loadGroup() {
  try {
    const [group, user, stats, proposals, wallets] = await Promise.all([
      fetchGroup(groupId),
      fetchCurrentUser(),
      fetchGroupStats(groupId),
      fetchProposalsForGroup(groupId),
      fetchMyWallets()
    ]);

    currentUserId = user.id;
    isCreator = Number(group.createdById) === Number(currentUserId);

    document.getElementById('group-name').textContent = group.name;
    document.getElementById('group-desc').textContent = group.description || '';

    const rulesBox = document.getElementById('group-rules-box');
    const rulesText = document.getElementById('group-rules-text');
    if (group.rules && group.rules.trim() !== '') {
      rulesText.textContent = group.rules;
      rulesBox.classList.remove('hidden');
    } else {
      rulesBox.classList.add('hidden');
    }

    document.getElementById('group-balance').textContent = formatCurrency(stats.totalSavings || 0);
    document.getElementById('member-count').textContent = stats.members;

    const personal = (wallets || []).find(w => w.type === 'PERSONAL');
    document.getElementById('personal-balance').textContent = formatCurrency(personal?.balance || 0);

    const inviteCode = group.inviteCode || '';
    document.getElementById('invite-code-display').textContent = inviteCode || 'N/A';
    document.getElementById('copy-invite').onclick = async () => {
      if (!inviteCode) return showAlert('No invite code available.', 'error');
      try {
        await navigator.clipboard.writeText(inviteCode);
        showAlert('Invite code copied.');
      } catch {
        showAlert('Could not copy automatically. Select and copy the code instead.', 'error');
      }
    };

    document.getElementById('owner-actions').classList.toggle('hidden', !isCreator);

    renderProposals(proposals);
    await refreshMembersList();

    const growthChartEl = document.getElementById('group-growth-chart');
    if (growthChartEl) {
      try {
        const growth = await fetchGroupGrowth(groupId);
        renderStepLineChart(growthChartEl, growth.map(g => ({
          label: g.label,
          value: parseFloat(g.value)
        })));
      } catch (e) {
        growthChartEl.innerHTML = '<div class="chart-empty">NOT_ENOUGH_HISTORY_YET</div>';
      }
    }

    initStaticListeners();
  } catch (err) {
    showAlert(err.message, 'error');
    if (/not a member|not found|forbidden/i.test(err.message || '')) {
      setTimeout(() => { window.location.href = 'groups.html'; }, 1500);
    }
  }
}

function initStaticListeners() {
  if (listenersInitialized) return;
  listenersInitialized = true;

  document.getElementById('toggle-members').addEventListener('click', async () => {
    const list = document.getElementById('members-list');
    const toggleBtn = document.getElementById('toggle-members');
    if (membersVisible) {
      membersVisible = false;
      list.classList.add('hidden');
      toggleBtn.textContent = 'View Members';
      return;
    }
    membersVisible = true;
    await refreshMembersList();
  });

  document.getElementById('contribute-btn').addEventListener('click', async () => {
    const btn = document.getElementById('contribute-btn');
    const raw = document.getElementById('contribute-amount').value;
    const amount = parseFloat(raw);
    if (!raw || isNaN(amount) || amount <= 0) {
      return showAlert('Enter a valid contribution amount.', 'error');
    }

    setBusy(btn, true, 'Contributing…');
    try {
      await contributeToGroup(groupId, amount);
      document.getElementById('contribute-amount').value = '';
      showAlert('Contribution successful.');
      await loadGroup();
    } catch (e) {
      showAlert(e.message, 'error');
    } finally {
      setBusy(btn, false);
    }
  });

  document.getElementById('new-proposal-btn').addEventListener('click', async () => {
    const btn = document.getElementById('new-proposal-btn');
    const title = document.getElementById('proposal-title').value.trim();
    const desc = document.getElementById('proposal-desc').value.trim();
    const amount = parseFloat(document.getElementById('proposal-amount').value);

    if (!title) return showAlert('Proposal title is required.', 'error');
    if (!amount || amount <= 0) return showAlert('Enter a valid proposal amount.', 'error');

    setBusy(btn, true, 'Creating…');
    try {
      await createProposal(groupId, title, desc, amount);
      document.getElementById('proposal-title').value = '';
      document.getElementById('proposal-desc').value = '';
      document.getElementById('proposal-amount').value = '';
      showAlert('Proposal created. Members can vote YES or NO.');
      await loadGroup();
    } catch (e) {
      showAlert(e.message, 'error');
    } finally {
      setBusy(btn, false);
    }
  });

  document.getElementById('delete-group-btn').addEventListener('click', async () => {
    if (!confirm('Delete this group permanently? This only works if the wallet is empty and there is no proposal/transaction history.')) {
      return;
    }
    const btn = document.getElementById('delete-group-btn');
    setBusy(btn, true, 'Deleting…');
    try {
      await deleteGroup(groupId);
      showAlert('Group deleted.');
      window.location.href = 'groups.html';
    } catch (e) {
      showAlert(e.message, 'error');
      setBusy(btn, false);
    }
  });
}

loadGroup();
