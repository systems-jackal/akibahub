requireAuth();

function greetingForNow() {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good morning';
  if (hour < 17) return 'Good afternoon';
  return 'Good evening';
}

function firstNameFrom(fullName) {
  const parts = String(fullName || '').trim().split(/\s+/).filter(Boolean);
  return parts[0] || 'there';
}

function applyGreeting(user) {
  const greet = document.getElementById('dashboard-greeting');
  const sub = document.getElementById('dashboard-sub');
  if (greet) greet.textContent = `Welcome back, ${firstNameFrom(user.fullName)}`;
  if (sub) sub.textContent = `${greetingForNow()} — here's your savings overview`;
}

async function loadDashboard() {
  let data, user;
  try {
    [data, user] = await Promise.all([
      fetchDashboard(),
      // Cache first (instant, no network) so a slow/failed /api/auth/me
      // doesn't leave the greeting on its generic default even briefly.
      // If there's no cache either (e.g. a brand-new session) this still
      // makes one real attempt rather than giving up immediately - a
      // transient failure here (including a rate-limited /api/auth/me,
      // see RateLimitFilter) used to mean the page silently never showed
      // the user's name at all, with no error and no retry.
      Promise.resolve(readCachedUser()).then(cached => cached || fetchCurrentUser().catch(() => null))
    ]);
  } catch (err) {
    // fetchDashboard() failed. Left alone, personal-balance stays on its
    // HTML default of "0.00" — indistinguishable from a real zero balance.
    // A load failure needs to look like a load failure, not like data.
    showPersistentError(err.message || 'Could not load your dashboard. Refresh to try again.');
    return;
  }

  try {
    if (user) {
      cacheCurrentUser(user);
      applyGreeting(user);
    }

    document.getElementById('personal-balance').textContent = formatCurrency(data.personalBalance);

    const tickerBalance = document.getElementById('ticker-balance');
    if (tickerBalance) {
      const total = parseFloat(data.personalBalance) + parseFloat(data.groupBalance || 0);
      tickerBalance.textContent = formatCurrency(total);
    }

    const chartEl = document.getElementById('asset-distribution-chart');
    if (chartEl && data.assetDistribution) {
      renderBlockBarChart(chartEl, data.assetDistribution.map(d => ({ label: d.label, value: parseFloat(d.value) })));
    }

    // Groups fetched separately from the core dashboard data above: if this
    // one call fails, the balance/chart already rendered should stay put
    // rather than the whole page bailing out on an unrelated error.
    let groups = [];
    try {
      groups = await fetchMyGroups();
    } catch (err) {
      showAlert('Could not load your groups. Refresh to try again.', 'error');
      return;
    }

    if (groups.length === 0) {
      // Solo saver – hide group sections, show prompt
      document.getElementById('groups-section').classList.add('hidden');
      document.getElementById('solo-saver-prompt').classList.remove('hidden');
    } else {
      // Group saver – show group sections, hide solo prompt
      document.getElementById('groups-section').classList.remove('hidden');
      document.getElementById('solo-saver-prompt').classList.add('hidden');

      // Groups summary
      const summary = document.getElementById('groups-summary');
      summary.innerHTML = `
        <p>You are in <strong>${groups.length}</strong> group(s) with a total balance of <strong>KES ${formatCurrency(data.groupBalance)}</strong>.</p>
        <ul>
          ${groups.map(g => `<li><a href="group.html?id=${g.id}">${escapeHtml(g.name)}</a></li>`).join('')}
        </ul>
      `;

      // Recent proposals
      const proposals = await fetchMyProposals();
      const list = document.getElementById('recent-proposals');
      if (proposals && proposals.length > 0) {
        list.innerHTML = proposals.slice(0, 5).map(p => {
          const total = p.totalMembers || 1;
          const yesPct = Math.round((p.yesVotes / total) * 100);
          const noPct = Math.round((p.noVotes / total) * 100);
          const groupId = p.group?.id;
          return `
          <div class="proposal-card">
            <div class="proposal-title">${escapeHtml(p.title)} <span class="badge status-${p.status.toLowerCase()}">${escapeHtml(p.status)}</span></div>
            <div class="proposal-meta">
              KES <span class="proposal-amount">${formatCurrency(p.amount)}</span>
              ${groupId ? ` · <a href="group.html?id=${groupId}">${escapeHtml(p.group?.name || 'Group')}</a>` : ''}
            </div>
            <div class="vote-tally">
              <div class="vote-tally-bar">
                <div class="yes-fill" style="width:${yesPct}%"></div>
                <div class="no-fill" style="width:${noPct}%"></div>
              </div>
              <div class="vote-tally-labels"><span>YES: ${p.yesVotes}</span><span>NO: ${p.noVotes}</span></div>
            </div>
          </div>
        `;
        }).join('');
      } else {
        list.innerHTML = '<p>No proposals yet.</p>';
      }
    }
  } catch (err) {
    showAlert(err.message, 'error');
  }
}

// Money actions deep-link to wallet (STK sheet / ledger withdraw) — no prompt().
document.getElementById('quick-join-group').addEventListener('click', async () => {
  const code = window.prompt('Enter the 6-character invite code:');
  if (!code) return;
  try {
    const group = await joinGroup(code.trim());
    showAlert(`Joined ${group.name}.`);
    window.location.href = `group.html?id=${group.id}`;
  } catch (e) {
    showAlert(e.message, 'error');
  }
});

loadDashboard();