const API_BASE = '';

function getToken() {
  return localStorage.getItem('akiba_token');
}

function authHeaders() {
  return { 'Authorization': `Bearer ${getToken()}` };
}

async function fetchWallets() {
  const res = await fetch('/api/wallets/me', { headers: authHeaders() });
  return await res.json();
}

async function fetchGroups() {
  const res = await fetch('/api/groups', { headers: authHeaders() });
  return await res.json();
}

async function fetchGroup(groupId) {
  const res = await fetch(`/api/groups/${groupId}`, { headers: authHeaders() });
  if (!res.ok) throw new Error('Group not found');
  return await res.json();
}

async function fetchMembers(groupId) {
  const res = await fetch(`/api/groups/${groupId}/members`, { headers: authHeaders() });
  return await res.json();
}

async function fetchProposals(groupId) {
  const res = await fetch(`/api/groups/${groupId}/proposals`, { headers: authHeaders() });
  return await res.json();
}

async function deposit(amount) {
  await fetch('/api/wallets/me/personal/deposit', {
    method: 'POST',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify({ amount: parseFloat(amount) })
  });
}

async function createGroup(name, description) {
  await fetch('/api/groups', {
    method: 'POST',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, description })
  });
}

async function joinGroup(groupId) {
  await fetch(`/api/groups/${groupId}/join`, {
    method: 'POST',
    headers: authHeaders()
  });
}

async function contributeToGroup(groupId, amount) {
  await fetch(`/api/wallets/groups/${groupId}/contribute`, {
    method: 'POST',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify({ amount: parseFloat(amount) })
  });
}

async function createProposal(groupId, title, description, amount) {
  await fetch(`/api/groups/${groupId}/proposals`, {
    method: 'POST',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify({ title, description, amount: parseFloat(amount) })
  });
}

async function voteOnProposal(proposalId) {
  await fetch(`/api/proposals/${proposalId}/vote`, {
    method: 'POST',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify({ vote: 'YES' })
  });
}