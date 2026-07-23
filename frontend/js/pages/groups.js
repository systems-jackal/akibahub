requireAuth();

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

async function loadGroups() {
  try {
    const groups = await fetchMyGroups();
    const list = document.getElementById('groups-list');
    if (!groups || groups.length === 0) {
      list.innerHTML = `
        <div class="card">
          <p>No groups yet. Create one above, or join with an invite code from a friend.</p>
        </div>`;
      return;
    }

    list.innerHTML = groups.map(g => `
      <div class="group-card">
        <h4><a href="group.html?id=${g.id}">${escapeHtml(g.name)}</a></h4>
        <p>${escapeHtml(g.description || 'No description')}</p>
        <p class="text-muted small">Invite code: <strong>${escapeHtml(g.inviteCode || 'N/A')}</strong></p>
        <a class="btn-secondary btn-sm" href="group.html?id=${g.id}">Open group</a>
      </div>
    `).join('');
  } catch (err) {
    showAlert(err.message, 'error');
  }
}

document.getElementById('join-invite-code').addEventListener('input', (e) => {
  e.target.value = e.target.value.replace(/[^a-zA-Z0-9]/g, '').toUpperCase().slice(0, 6);
});

document.getElementById('create-btn').addEventListener('click', async () => {
  const btn = document.getElementById('create-btn');
  const name = document.getElementById('group-name').value.trim();
  const desc = document.getElementById('group-desc').value.trim();
  const rules = document.getElementById('group-rules').value.trim();

  if (!name) return showAlert('Group name is required.', 'error');

  setBusy(btn, true, 'Creating…');
  try {
    const group = await createGroup(name, desc, rules);
    showAlert('Group created. Share the invite code with members.');

    document.getElementById('group-name').value = '';
    document.getElementById('group-desc').value = '';
    document.getElementById('group-rules').value = '';

    const inviteCode = group.inviteCode;
    document.getElementById('generated-code').textContent = inviteCode;
    document.getElementById('invite-code-area').classList.remove('hidden');

    const openLink = document.getElementById('open-created-group');
    openLink.href = `group.html?id=${group.id}`;

    document.getElementById('copy-code').onclick = async () => {
      try {
        await navigator.clipboard.writeText(inviteCode);
        showAlert('Invite code copied.');
      } catch {
        showAlert('Could not copy automatically. Select and copy the code instead.', 'error');
      }
    };

    await loadGroups();
  } catch (e) {
    showAlert(e.message, 'error');
  } finally {
    setBusy(btn, false);
  }
});

document.getElementById('join-btn').addEventListener('click', async () => {
  const btn = document.getElementById('join-btn');
  const code = document.getElementById('join-invite-code').value.trim();
  if (!code) return showAlert('Please enter an invite code.', 'error');
  if (code.length !== 6) return showAlert('Invite codes are 6 characters.', 'error');

  setBusy(btn, true, 'Joining…');
  try {
    const group = await joinGroup(code);
    showAlert(`Joined ${group.name}.`);
    document.getElementById('join-invite-code').value = '';
    window.location.href = `group.html?id=${group.id}`;
  } catch (e) {
    showAlert(e.message, 'error');
    setBusy(btn, false);
  }
});

loadGroups();
