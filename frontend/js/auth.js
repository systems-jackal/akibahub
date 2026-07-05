const AUTH_BASE = 'https://akibahub.unitybridge.dev';

function parseJwt(token) {
  try {
    return JSON.parse(atob(token.split('.')[1]));
  } catch { return null; }
}

async function initAuth() {
  // Handle OAuth callback — token comes in URL fragment (#token=...)
  // Fragment is never sent to servers — safe from log leakage
  const hash = window.location.hash;
  if (hash && hash.includes('token=')) {
    const params = new URLSearchParams(hash.substring(1));
    const token  = params.get('token');
    if (token) {
      window.accessToken = token;
      // Clear fragment from URL immediately
      window.history.replaceState({}, '', window.location.pathname);
      return parseJwt(token);
    }
  }

  // Try refreshing via HttpOnly cookie
  // Cookie is sent automatically — no JS access needed
  try {
    const res = await fetch(`${AUTH_BASE}/auth/refresh`, {
      method: 'POST',
      credentials: 'include', // sends HttpOnly cookie
      headers: { 'Content-Type': 'application/json' }
    });
    if (!res.ok) throw new Error('expired');
    const data = await res.json();
    window.accessToken = data.accessToken;
    return parseJwt(data.accessToken);
  } catch {
    window.accessToken = null;
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

window.initAuth    = initAuth;
window.requireAuth = requireAuth;
window.parseJwt    = parseJwt;
