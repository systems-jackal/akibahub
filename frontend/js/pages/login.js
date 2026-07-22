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

// Toggle between phone and ID login
document.getElementById('toggle-login-method').addEventListener('click', function(e) {
  e.preventDefault();
  const phoneGroup = document.getElementById('phone-login-group');
  const idGroup = document.getElementById('id-login-group');
  const toggleLink = document.getElementById('toggle-login-method');

  if (phoneGroup.classList.contains('hidden')) {
    // Switch to phone
    phoneGroup.classList.remove('hidden');
    idGroup.classList.add('hidden');
    toggleLink.textContent = 'Login with ID Number instead';
  } else {
    // Switch to ID
    phoneGroup.classList.add('hidden');
    idGroup.classList.remove('hidden');
    toggleLink.textContent = 'Login with Phone Number instead';
  }
  // Clear any previous error
  document.getElementById('auth-message').textContent = '';
});

// Form submission
document.getElementById('login-form').addEventListener('submit', async function(e) {
  e.preventDefault();

  let login;
  const phoneGroup = document.getElementById('phone-login-group');
  if (!phoneGroup.classList.contains('hidden')) {
    // Phone mode: remove spaces and ensure it starts with +254
    let phone = document.getElementById('phone-login').value.replace(/\s/g, '');
    if (!phone.startsWith('+254')) {
      phone = '+254' + phone.replace(/^0+/, ''); // remove leading zeros if any
    }
    login = phone;
  } else {
    // ID mode
    login = document.getElementById('id-login').value.trim();
  }

  const password = document.getElementById('password').value;
  const msgEl = document.getElementById('auth-message');

  try {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ login, password })
    });
    const json = await res.json();
    if (json.success) {
      setTokens(json.data.token, json.data.refreshToken);
      localStorage.setItem('akiba_phone', json.data.user.phoneNumber);
      window.location.href = 'dashboard.html';
    } else {
      msgEl.textContent = json.message || 'Login failed';
      msgEl.style.color = 'var(--red)';
    }
  } catch (err) {
    msgEl.textContent = 'Network error. Please try again.';
    msgEl.style.color = 'var(--red)';
  }
});