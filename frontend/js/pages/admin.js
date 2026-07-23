requireAuth();

async function loadAdminDashboard() {
  try {
    const stats = await fetchAdminStats();
    document.getElementById('stat-users').textContent = stats.totalUsers;
    document.getElementById('stat-groups').textContent = stats.totalGroups;
    document.getElementById('stat-personal').textContent = 'KES ' + formatCurrency(stats.totalPersonalSavings);
    document.getElementById('stat-group-savings').textContent = 'KES ' + formatCurrency(stats.totalGroupSavings);
    document.getElementById('stat-proposals').textContent = stats.totalProposals;
    document.getElementById('stat-pending').textContent = stats.pendingProposals;
  } catch (e) {
    showAlert('Could not load platform stats: ' + e.message, 'error');
  }

  await loadUsers();
  await loadGroups();
  await loadAuditLog();
}

async function loadUsers() {
  const wrapper = document.getElementById('users-table-wrapper');
  try {
    const users = await fetchAdminUsers();
    if (!users || users.length === 0) {
      wrapper.innerHTML = '<p class="text-muted">No users found.</p>';
      return;
    }
    wrapper.innerHTML = `
      <table>
        <thead>
          <tr><th>Name</th><th>Phone</th><th>ID Number</th><th>Role</th><th>Status</th><th>Joined</th><th>Action</th></tr>
        </thead>
        <tbody>
          ${users.map(u => `
            <tr data-user-id="${u.id}">
              <td>${escapeHtml(u.fullName)}</td>
              <td>${escapeHtml(u.phoneNumber)}</td>
              <td>${escapeHtml(u.idNumber)}</td>
              <td><span class="badge status-${u.role === 'ADMIN' ? 'approved' : 'open'}">${escapeHtml(u.role)}</span></td>
              <td><span class="badge ${u.status === 'ACTIVE' ? 'status-approved' : 'status-rejected'}">${escapeHtml(u.status)}</span></td>
              <td>${new Date(u.createdAt).toLocaleDateString('en-KE')}</td>
              <td>
                <button class="btn-secondary small toggle-status-btn" data-user-id="${u.id}" data-current-status="${u.status}">
                  ${u.status === 'ACTIVE' ? 'Suspend' : 'Reactivate'}
                </button>
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;

    wrapper.querySelectorAll('.toggle-status-btn').forEach(btn => {
      btn.addEventListener('click', async () => {
        const userId = btn.dataset.userId;
        const newStatus = btn.dataset.currentStatus === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
        const confirmMsg = newStatus === 'SUSPENDED'
          ? 'Suspend this user? They will be unable to log in until reactivated.'
          : 'Reactivate this user?';
        if (!confirm(confirmMsg)) return;

        btn.disabled = true;
        try {
          await updateUserStatus(userId, newStatus);
          showAlert(`User ${newStatus === 'SUSPENDED' ? 'suspended' : 'reactivated'}.`);
          await loadUsers();
        } catch (e) {
          showAlert(e.message, 'error');
          btn.disabled = false;
        }
      });
    });
  } catch (e) {
    wrapper.innerHTML = `<p class="text-muted">Could not load users: ${escapeHtml(e.message)}</p>`;
  }
}

async function loadGroups() {
  const wrapper = document.getElementById('groups-table-wrapper');
  try {
    const groups = await fetchAdminGroups();
    if (!groups || groups.length === 0) {
      wrapper.innerHTML = '<p class="text-muted">No groups found.</p>';
      return;
    }
    wrapper.innerHTML = `
      <table>
        <thead>
          <tr><th>Group</th><th>Members</th><th>Balance</th><th>Created</th></tr>
        </thead>
        <tbody>
          ${groups.map(g => `
            <tr>
              <td>${escapeHtml(g.name)}</td>
              <td>${g.memberCount}</td>
              <td>KES ${formatCurrency(g.balance)}</td>
              <td>${new Date(g.createdAt).toLocaleDateString('en-KE')}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;
  } catch (e) {
    wrapper.innerHTML = `<p class="text-muted">Could not load groups: ${escapeHtml(e.message)}</p>`;
  }
}

async function loadAuditLog() {
  const feed = document.getElementById('audit-log-feed');
  try {
    const entries = await fetchAdminAuditLog();
    if (!entries || entries.length === 0) {
      feed.innerHTML = '<p class="text-muted">No activity recorded yet.</p>';
      return;
    }
    feed.innerHTML = entries.map(e => `
      <div class="audit-feed-line">
        <span class="audit-feed-time">${new Date(e.createdAt).toLocaleString('en-KE', { hour12: false })}</span>
        <span class="audit-feed-event">${escapeHtml(e.eventType)}</span>
        <span class="audit-feed-payload">${escapeHtml(truncate(e.payload || '', 80))}</span>
      </div>
    `).join('');
  } catch (e) {
    feed.innerHTML = `<p class="text-muted">Could not load activity log: ${escapeHtml(e.message)}</p>`;
  }
}

loadAdminDashboard();