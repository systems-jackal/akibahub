const API_BASE = 'https://akibahub.unitybridge.dev';

async function refreshAccessToken() {
  const res = await fetch(`${API_BASE}/auth/refresh`, {
    method: 'POST',
    credentials: 'include', // sends HttpOnly cookie automatically
    headers: { 'Content-Type': 'application/json' }
  });

  if (!res.ok) {
    window.accessToken = null;
    window.location.href = '/index.html';
    throw new Error('Session expired');
  }

  const data = await res.json();
  window.accessToken = data.accessToken;
  return data.accessToken;
}

async function request(method, path, body = null, retry = true) {
  if (!window.accessToken) await refreshAccessToken();

  const opts = {
    method,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${window.accessToken}`
    }
  };
  if (body) opts.body = JSON.stringify(body);

  const res = await fetch(`${API_BASE}${path}`, opts);

  if (res.status === 401 && retry) {
    await refreshAccessToken();
    return request(method, path, body, false);
  }

  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: 'Request failed' }));
    throw new Error(err.message || `Error ${res.status}`);
  }

  if (res.status === 204) return null;
  return res.json();
}

const api = {
  get:    (path)       => request('GET',    path),
  post:   (path, body) => request('POST',   path, body),
  put:    (path, body) => request('PUT',    path, body),
  delete: (path)       => request('DELETE', path),

  auth: {
    loginUrl: () => `${API_BASE}/auth/login`,
    logout: async () => {
      await request('POST', '/auth/logout').catch(() => {});
      window.accessToken = null;
      window.location.href = '/index.html';
    }
  },

  wallet: {
    balance:      ()       => api.get('/savings/balance'),
    transactions: (page=0) => api.get(`/savings/transactions?page=${page}&size=10`),
    deposit:      (body)   => api.post('/savings/deposit', body)
  },

  groups: {
    list:           ()        => api.get('/groups/my'),
    get:            (id)      => api.get(`/groups/${id}`),
    create:         (body)    => api.post('/groups', body),
    join:           (body)    => api.post('/groups/join', body),
    members:        (id)      => api.get(`/groups/${id}/members`),
    generateInvite: (id)      => api.post(`/groups/${id}/invite`),
    changeRole:     (id,body) => api.put(`/groups/${id}/members/role`, body),
    removeMember:   (id,uid)  => api.delete(`/groups/${id}/members/${uid}`)
  },

  proposals: {
    list:   (groupId, page=0) => api.get(`/proposals/group/${groupId}?page=${page}&size=10`),
    get:    (id)              => api.get(`/proposals/${id}`),
    create: (body)            => api.post('/proposals', body),
    vote:   (id, vote)        => api.post(`/proposals/${id}/vote`, { vote }),
    cancel: (id)              => api.delete(`/proposals/${id}`),
    votes:  (id)              => api.get(`/proposals/${id}/votes`)
  }
};

function showToast(msg, dur = 2500) {
  const t = document.createElement('div');
  t.className = 'toast';
  t.textContent = msg;
  document.body.appendChild(t);
  requestAnimationFrame(() => t.classList.add('show'));
  setTimeout(() => {
    t.classList.remove('show');
    setTimeout(() => t.remove(), 200);
  }, dur);
}

window.api       = api;
window.showToast = showToast;
