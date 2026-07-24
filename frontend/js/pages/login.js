// If we got here via an inactivity auto-logout, say so - a silent
// redirect to the login page reads as broken/confusing; an explained one
// reads as a deliberate security feature.
(function showInactivityNotice() {
  const params = new URLSearchParams(location.search);
  if (params.get('reason') === 'inactivity') {
    const msgEl = document.getElementById('auth-message');
    if (msgEl) {
      msgEl.textContent = '⏱ You were logged out due to inactivity. Please log in again.';
      msgEl.style.color = 'var(--orange)';
    }
  }
})();

function isPhoneLoginMode() {
  const phoneGroup = document.getElementById('phone-login-group');
  return phoneGroup && !phoneGroup.classList.contains('hidden');
}

function setLoginMode(usePhone) {
  const phoneGroup = document.getElementById('phone-login-group');
  const idGroup = document.getElementById('id-login-group');
  const phoneInput = document.getElementById('phone-login');
  const idInput = document.getElementById('id-login');
  const toggleLink = document.getElementById('toggle-login-method');

  if (usePhone) {
    phoneGroup.classList.remove('hidden');
    idGroup.classList.add('hidden');
    phoneInput.required = true;
    phoneInput.disabled = false;
    idInput.required = false;
    idInput.disabled = true;
    toggleLink.textContent = 'Login with ID Number instead';
  } else {
    phoneGroup.classList.add('hidden');
    idGroup.classList.remove('hidden');
    phoneInput.required = false;
    phoneInput.disabled = true;
    idInput.required = true;
    idInput.disabled = false;
    toggleLink.textContent = 'Login with Phone Number instead';
  }
  document.getElementById('auth-message').textContent = '';
}

document.getElementById('toggle-login-method').addEventListener('click', function(e) {
  e.preventDefault();
  setLoginMode(!isPhoneLoginMode());
});

// Default: phone login (disable the hidden ID field so autofill can't steal focus)
setLoginMode(true);

document.getElementById('login-form').addEventListener('submit', async function(e) {
  e.preventDefault();

  const msgEl = document.getElementById('auth-message');
  let login;

  if (isPhoneLoginMode()) {
    login = normalizeKenyanPhone(document.getElementById('phone-login').value);
    if (!login) {
      msgEl.textContent = 'Enter a valid phone number (9 digits after +254).';
      msgEl.style.color = 'var(--red)';
      return;
    }
  } else {
    login = document.getElementById('id-login').value.trim();
    if (!/^\d{8}$/.test(login)) {
      msgEl.textContent = 'ID number must be exactly 8 digits.';
      msgEl.style.color = 'var(--red)';
      return;
    }
  }

  const password = document.getElementById('password').value;
  if (!password) {
    msgEl.textContent = 'Password is required.';
    msgEl.style.color = 'var(--red)';
    return;
  }

  let res;
  try {
    res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ login, password })
    });
  } catch (err) {
    // fetch() rejected before we ever got a response - backend unreachable
    // or blocked by CORS, not a login failure. Don't call it "Login failed".
    msgEl.textContent = 'Could not reach the server. Check your connection and try again.';
    msgEl.style.color = 'var(--red)';
    return;
  }

  let json;
  try {
    json = await res.json();
  } catch (err) {
    // Response came back but wasn't JSON (proxy/HTML error page, etc).
    msgEl.textContent = `Request failed (${res.status}). Please try again.`;
    msgEl.style.color = 'var(--red)';
    return;
  }

  if (json.success) {
    setTokens(json.data.token, json.data.refreshToken);
    localStorage.setItem('akiba_phone', json.data.user.phoneNumber);
    if (typeof cacheCurrentUser === 'function') cacheCurrentUser(json.data.user);
    window.location.href = 'dashboard.html';
  } else {
    // buildErrorMessage (api.js) pulls field-level validation detail out of
    // json.data when present, instead of only ever showing the generic
    // top-level "Validation failed" message.
    msgEl.textContent = buildErrorMessage(json) || 'Login failed';
    msgEl.style.color = 'var(--red)';
  }
});