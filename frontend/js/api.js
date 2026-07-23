const API_BASE = '';

// ---------- token storage ----------
function getToken() {
  return localStorage.getItem('akiba_token');
}

function getRefreshToken() {
  return localStorage.getItem('akiba_refresh_token');
}

function setTokens(accessToken, refreshToken) {
  localStorage.setItem('akiba_token', accessToken);
  if (refreshToken) localStorage.setItem('akiba_refresh_token', refreshToken);
}

function clearTokens() {
  localStorage.removeItem('akiba_token');
  localStorage.removeItem('akiba_refresh_token');
  localStorage.removeItem('akiba_phone');
}

function authHeaders(extra = {}) {
  const token = getToken();
  const headers = { 'Content-Type': 'application/json', ...extra };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  return headers;
}

/** Fresh UUID for Idempotency-Key on money-moving POSTs. */
function newIdempotencyKey() {
  if (crypto && crypto.randomUUID) return crypto.randomUUID();
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

// ---------- silent token refresh ----------
let refreshPromise = null;

async function refreshAccessToken() {
  if (refreshPromise) return refreshPromise;

  refreshPromise = (async () => {
    const refreshToken = getRefreshToken();
    if (!refreshToken) throw new Error('No refresh token available');

    const res = await fetch('/api/auth/refresh', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    });
    let json;
    try {
      json = await res.json();
    } catch (e) {
      throw new Error('Session expired');
    }
    if (!res.ok || !json.success) throw new Error('Session expired');

    setTokens(json.data.token, json.data.refreshToken);
    return json.data.token;
  })();

  try {
    return await refreshPromise;
  } finally {
    refreshPromise = null;
  }
}

function redirectToLogin(message) {
  clearTokens();
  window.location.href = 'login.html';
  throw new Error(message || 'Your session has expired. Please log in again.');
}

// Generic API call that expects ApiResponse wrapper. Transparently
// retries once on a 401, after a silent token refresh.
async function apiFetch(url, options = {}) {
  let res = await fetch(url, options);

  if (res.status === 401) {
    if (!getRefreshToken()) {
      redirectToLogin();
    }
    try {
      const newToken = await refreshAccessToken();
      const retryOptions = {
        ...options,
        headers: { ...(options.headers || {}), Authorization: `Bearer ${newToken}` }
      };
      res = await fetch(url, retryOptions);
    } catch (e) {
      redirectToLogin();
    }
  }

  if (res.status === 401) {
    redirectToLogin();
  }

  let data;
  try {
    data = await res.json();
  } catch (e) {
    throw new Error(res.ok ? 'Unexpected server response' : `Request failed (${res.status})`);
  }
  if (!res.ok || data.success === false) {
    throw new Error(data.message || 'Request failed');
  }
  return data.data;
}

// Refresh if needed, then revoke refresh tokens server-side.
async function apiLogout() {
  try {
    let headers = authHeaders();
    let res = await fetch('/api/auth/logout', { method: 'POST', headers });
    if (res.status === 401 && getRefreshToken()) {
      try {
        await refreshAccessToken();
        headers = authHeaders();
        res = await fetch('/api/auth/logout', { method: 'POST', headers });
      } catch (e) {
        // Fall through — clear locally either way.
      }
    }
  } catch (e) {
    // not fatal - clear locally either way
  } finally {
    clearTokens();
  }
}

async function fetchDashboard() {
  return apiFetch('/api/dashboard', { headers: authHeaders() });
}

async function fetchMyWallets() {
  return apiFetch('/api/wallets/me', { headers: authHeaders() });
}

async function deposit(amount) {
  return apiFetch('/api/wallets/me/personal/deposit', {
    method: 'POST',
    headers: authHeaders({ 'Idempotency-Key': newIdempotencyKey() }),
    body: JSON.stringify({ amount })
  });
}

async function withdraw(amount) {
  return apiFetch('/api/wallets/me/personal/withdraw', {
    method: 'POST',
    headers: authHeaders({ 'Idempotency-Key': newIdempotencyKey() }),
    body: JSON.stringify({ amount })
  });
}

async function fetchMyGroups() {
  return apiFetch('/api/groups/my', { headers: authHeaders() });
}

async function fetchGroup(groupId) {
  return apiFetch(`/api/groups/${groupId}`, { headers: authHeaders() });
}

async function fetchGroupInviteCode(groupId) {
  return apiFetch(`/api/groups/${groupId}/invite`, {
    method: 'POST',
    headers: authHeaders()
  });
}

async function fetchGroupStats(groupId) {
  return apiFetch(`/api/groups/${groupId}/stats`, { headers: authHeaders() });
}

async function fetchGroupMembers(groupId) {
  return apiFetch(`/api/groups/${groupId}/members`, { headers: authHeaders() });
}

async function fetchGroupGrowth(groupId) {
  return apiFetch(`/api/groups/${groupId}/analytics/growth`, { headers: authHeaders() });
}

async function createGroup(name, description, rules) {
  return apiFetch('/api/groups', {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ name, description, rules })
  });
}

async function joinGroup(code) {
  return apiFetch('/api/groups/join', {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ code: String(code || '').trim() })
  });
}

async function contributeToGroup(groupId, amount) {
  return apiFetch(`/api/wallets/groups/${groupId}/contribute`, {
    method: 'POST',
    headers: authHeaders({ 'Idempotency-Key': newIdempotencyKey() }),
    body: JSON.stringify({ amount })
  });
}

async function fetchMyProposals() {
  return apiFetch('/api/proposals/my', { headers: authHeaders() });
}

async function fetchProposalsForGroup(groupId) {
  return apiFetch(`/api/groups/${groupId}/proposals`, { headers: authHeaders() });
}

async function createProposal(groupId, title, description, amount) {
  return apiFetch(`/api/groups/${groupId}/proposals`, {
    method: 'POST',
    headers: authHeaders({ 'Idempotency-Key': newIdempotencyKey() }),
    body: JSON.stringify({ title, description, amount })
  });
}

async function voteOnProposal(proposalId, vote) {
  return apiFetch(`/api/proposals/${proposalId}/vote`, {
    method: 'POST',
    headers: authHeaders({ 'Idempotency-Key': newIdempotencyKey() }),
    body: JSON.stringify({ vote })
  });
}

async function fetchTransactions(type, groupId, start, end) {
  const params = new URLSearchParams();
  if (type) params.append('type', type);
  if (groupId) params.append('groupId', groupId);
  if (start) params.append('start', start);
  if (end) params.append('end', end);
  return apiFetch(`/api/transactions/me?${params.toString()}`, { headers: authHeaders() });
}

async function updateProfile(data) {
  return apiFetch('/api/users/me', {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify(data)
  });
}

async function changePassword(currentPassword, newPassword) {
  return apiFetch('/api/users/me/password', {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify({ currentPassword, newPassword })
  });
}

async function fetchCurrentUser() {
  return apiFetch('/api/auth/me', { headers: authHeaders() });
}
