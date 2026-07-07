const API_BASE = ''; // same origin, Nginx proxy

let token = null;

document.addEventListener('DOMContentLoaded', () => {
  // Auth tabs
  document.getElementById('login-tab').addEventListener('click', showLogin);
  document.getElementById('register-tab').addEventListener('click', showRegister);
  document.getElementById('auth-form').addEventListener('submit', handleAuth);
  document.getElementById('logout-btn').addEventListener('click', logout);

  // Dashboard actions
  document.getElementById('deposit-btn').addEventListener('click', () => toggle('deposit-form'));
  document.getElementById('do-deposit').addEventListener('click', deposit);
  document.getElementById('create-group-btn').addEventListener('click', () => toggle('create-group-form'));
  document.getElementById('do-create-group').addEventListener('click', createGroup);
  document.getElementById('do-join-group').addEventListener('click', joinGroup);
  document.getElementById('create-proposal-form').addEventListener('click', () => toggle('create-proposal-form'));
  document.getElementById('do-create-proposal').addEventListener('click', createProposal);
});

function showLogin() {
  document.getElementById('login-tab').classList.add('active');
  document.getElementById('register-tab').classList.remove('active');
  document.getElementById('fullname-group').classList.add('hidden');
  document.getElementById('auth-button').textContent = 'Login';
}

function showRegister() {
  document.getElementById('register-tab').classList.add('active');
  document.getElementById('login-tab').classList.remove('active');
  document.getElementById('fullname-group').classList.remove('hidden');
  document.getElementById('auth-button').textContent = 'Register';
}

async function handleAuth(e) {
  e.preventDefault();
  const isLogin = document.getElementById('auth-button').textContent === 'Login';
  const phone = document.getElementById('phone').value;
  const password = document.getElementById('password').value;
  const body = { phoneNumber: phone, password };

  if (!isLogin) {
    body.fullName = document.getElementById('fullname').value;
  }

  const endpoint = isLogin ? '/api/auth/login' : '/api/auth/register';
  try {
    const res = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    const data = await res.json();
    if (res.ok) {
      token = data.token;
      localStorage.setItem('akiba_token', token); // for page reloads (dev only, consider in-memory for prod)
      showDashboard();
    } else {
      showMessage(data.error || 'Request failed', true);
    }
  } catch (err) {
    showMessage('Network error', true);
  }
}

function showMessage(msg, isError) {
  const el = document.getElementById('auth-message');
  el.textContent = msg;
  el.style.color = isError ? 'red' : 'green';
}

function showDashboard() {
  document.getElementById('auth-section').classList.add('hidden');
  document.getElementById('dashboard').classList.remove('hidden');
  loadDashboardData();
}

function logout() {
  token = null;
  localStorage.removeItem('akiba_token');
  document.getElementById('dashboard').classList.add('hidden');
  document.getElementById('auth-section').classList.remove('hidden');
}

async function loadDashboardData() {
  const headers = { 'Authorization': `Bearer ${token}` };
  try {
    const [walletsRes, groupsRes] = await Promise.all([
      fetch('/api/wallets/me', { headers }),
      fetch('/api/groups', { headers })
    ]);
    const wallets = await walletsRes.json();
    const groups = await groupsRes.json();

    renderWallets(wallets);
    renderGroups(groups);
  } catch (err) {
    console.error(err);
  }
}

function renderWallets(wallets) {
  const container = document.getElementById('wallets');
  container.innerHTML = wallets.map(w => `
    <div class="wallet-item">
      <strong>${w.type}</strong> – Balance: ${w.balance}
    </div>
  `).join('');
}

function renderGroups(groups) {
  const container = document.getElementById('groups-list');
  container.innerHTML = groups.map(g => `
    <div class="group-item">
      <strong>${g.name}</strong> (ID: ${g.id}) – ${g.description || ''}
      <button onclick="contributeToGroup(${g.id})">Contribute</button>
      <button onclick="loadProposals(${g.id})">View Proposals</button>
    </div>
  `).join('');
}

async function deposit() {
  const amount = document.getElementById('deposit-amount').value;
  await fetch('/api/wallets/me/personal/deposit', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    body: JSON.stringify({ amount: parseFloat(amount) })
  });
  loadDashboardData();
}

async function createGroup() {
  const name = document.getElementById('group-name').value;
  const description = document.getElementById('group-desc').value;
  await fetch('/api/groups', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    body: JSON.stringify({ name, description })
  });
  loadDashboardData();
}

async function joinGroup() {
  const id = document.getElementById('join-group-id').value;
  await fetch(`/api/groups/${id}/join`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  loadDashboardData();
}

async function contributeToGroup(groupId) {
  const amount = prompt('Amount to contribute:');
  if (!amount) return;
  await fetch(`/api/wallets/groups/${groupId}/contribute`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    body: JSON.stringify({ amount: parseFloat(amount) })
  });
  loadDashboardData();
}

async function loadProposals(groupId) {
  const res = await fetch(`/api/groups/${groupId}/proposals`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const proposals = await res.json();
  const container = document.getElementById('proposals-list');
  container.innerHTML = proposals.map(p => `
    <div class="proposal-item">
      <strong>${p.title}</strong> – Amount: ${p.amount}, Status: ${p.status}
      <button onclick="voteOnProposal(${p.id})">Vote YES</button>
    </div>
  `).join('');
}

async function createProposal() {
  const groupId = document.getElementById('proposal-group-id').value;
  const title = document.getElementById('proposal-title').value;
  const description = document.getElementById('proposal-desc').value;
  const amount = document.getElementById('proposal-amount').value;
  await fetch(`/api/groups/${groupId}/proposals`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    body: JSON.stringify({ title, description, amount: parseFloat(amount) })
  });
  loadDashboardData();
}

async function voteOnProposal(proposalId) {
  await fetch(`/api/proposals/${proposalId}/vote`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    body: JSON.stringify({ vote: 'YES' })
  });
  loadDashboardData();
}

function toggle(id) {
  document.getElementById(id).classList.toggle('hidden');
}

// On page load, check for existing token
const savedToken = localStorage.getItem('akiba_token');
if (savedToken) {
  token = savedToken;
  showDashboard();
}