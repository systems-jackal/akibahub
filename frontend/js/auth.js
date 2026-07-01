const AUTH_BASE = 'https://akibahub.unitybridge.dev';

function parseJwt(token) {
  try {
    return JSON.parse(atob(token.split('.')[1]));
  } catch { return null; }
}

function isTokenExpired(token) {
  const payload = parseJwt(token);
  if (!payload) return true;
  return Date.now() >= payload.exp * 1000;
}

async function initAuth() {
  // Handle OAuth callback
  const params = new URLSearchParams(window.location.search);
  const token   = params.get('token');
  const refresh = params.get('refresh');

  if (token && refresh) {
    setTokens(token, refresh);
    window.history.replaceState({}, '', window.location.pathname);
    return parseJwt(token);
  }

  // Try existing session
  const rt = sessionStorage.getItem('akiba_refresh');
  if (!rt) return null;

  try {
    const res = await fetch(`${AUTH_BASE}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: rt })
    });
    if (!res.ok) throw new Error('expired');
    const data = await res.json();
    setTokens(data.accessToken, data.refreshToken);
    return parseJwt(data.accessToken);
  } catch {
    clearTokens();
    return null;
  }
}

function requireAuth(user) {
  if (!user) {
    window.location.href = '/index.html';
    return false;
  }
  return true;
}

function getUser() {
  const rt = sessionStorage.getItem('akiba_refresh');
  if (!rt) return null;
  return null;
}

window.initAuth    = initAuth;
window.requireAuth = requireAuth;
window.parseJwt    = parseJwt;
