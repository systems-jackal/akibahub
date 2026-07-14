requireAuth();

async function loadGroups() {
  try {
    const groups = await fetchMyGroups();
    const list = document.getElementById('groups-list');
    if (!groups || groups.length === 0) {
      list.innerHTML = '<p>No groups yet. Create or join one!</p>';
    } else {
      list.innerHTML = groups.map(g => `
        <div class="group-card">
          <h4><a href="group.html?id=${g.id}">${escapeHtml(g.name)}</a></h4>
          <p>${escapeHtml(g.description || '')}</p>
          <p class="text-muted small">Code: <strong>${escapeHtml(g.inviteCode || 'N/A')}</strong></p>
        </div>
      `).join('');
    }
  } catch (err) {
    showAlert(err.message, 'error');
  }
}

// Create Group Flow
document.getElementById('create-btn').addEventListener('click', async () => {
  const name = document.getElementById('group-name').value.trim();
  const desc = document.getElementById('group-desc').value.trim();
  const rules = document.getElementById('group-rules').value.trim();

  if (!name) return showAlert('Group name is required.', 'error');

  try {
    const group = await createGroup(name, desc, rules);

    showAlert('Group created successfully.');

    // Clear input fields
    document.getElementById('group-name').value = '';
    document.getElementById('group-desc').value = '';
    document.getElementById('group-rules').value = '';

    // Unveil invite code mechanics
    const inviteCode = group.inviteCode;
    document.getElementById('generated-code').textContent = inviteCode;
    document.getElementById('invite-code-area').classList.remove('hidden');

    document.getElementById('copy-code').onclick = () => {
      navigator.clipboard.writeText(inviteCode)
        .then(() => showAlert('Invite code copied to clipboard.'))
        .catch(() => showAlert('Failed to copy code automatically.', 'error'));
    };

    loadGroups();
  } catch (e) { 
    showAlert(e.message, 'error'); 
  }
});

// Join Group Flow via Invite Code
document.getElementById('join-btn').addEventListener('click', async () => {
  const code = document.getElementById('join-invite-code').value.trim();
  if (!code) return showAlert('Please enter an invite code.', 'error');

  try {
    await joinGroup(code);

    showAlert('You have joined the group.');
    document.getElementById('join-invite-code').value = ''; // Reset field
    loadGroups();
  } catch (e) { 
    showAlert(e.message, 'error'); 
  }
});

// Initial execution
loadGroups();