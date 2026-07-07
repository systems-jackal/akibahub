const API_BASE = '';

function getToken() {
  return localStorage.getItem('akiba_token');
}

function authHeaders() {
  const token = getToken();
  return token ? { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' } : { 'Content-Type': 'application/json' };
}

// Generic API call that expects ApiResponse wrapper
async function apiFetch(url, options = {}) {
  const res = await fetch(url, options);
  const data = await res.json();
  if (!res.ok || data.success === false) {
    throw new Error(data.message || 'Request failed');
  }
  return data.data; // extract the payload
}

// Specific API functions
async function fetchDashboard() {
  return apiFetch('/api/dashboard', { headers: authHeaders() });
}

async function fetchMyWallets() {
  return apiFetch('/api/wallets/me', { headers: authHeaders() });
}

async function deposit(amount) {
  return apiFetch('/api/wallets/me/personal/deposit', {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ amount })
  });
}

async function withdraw(amount) {
  return apiFetch('/api/wallets/me/personal/withdraw', {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ amount })
  });
}

async function fetchMyGroups() {
  return apiFetch('/api/groups/my', { headers: authHeaders() });
}

async function fetchGroup(groupId) {
  return apiFetch(`/api/groups/${groupId}`, { headers: authHeaders() });
}

async function createGroup(name, description) {
  return apiFetch('/api/groups', {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ name, description })
  });
}

async function joinGroup(groupId) {
  return apiFetch(`/api/groups/${groupId}/join`, {
    method: 'POST',
    headers: authHeaders()
  });
}

async function contributeToGroup(groupId, amount) {
  return apiFetch(`/api/wallets/groups/${groupId}/contribute`, {
    method: 'POST',
    headers: authHeaders(),
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
    headers: authHeaders(),
    body: JSON.stringify({ title, description, amount })
  });
}

async function voteOnProposal(proposalId, vote) {
  return apiFetch(`/api/proposals/${proposalId}/vote`, {
    method: 'POST',
    headers: authHeaders(),
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