document.getElementById('register-form').addEventListener('submit', async function(e) {
  e.preventDefault();
  const msgEl = document.getElementById('auth-message');
  const fullname = document.getElementById('fullname').value.trim();
  const idnumber = document.getElementById('idnumber').value.trim();
  const phone = normalizeKenyanPhone(document.getElementById('phone').value);
  const password = document.getElementById('password').value;
  const confirmPassword = document.getElementById('confirm-password').value;

  if (!phone) {
    msgEl.textContent = 'Enter a valid phone number (9 digits after +254).';
    msgEl.style.color = 'var(--red)';
    return;
  }

  if (password.length < 6) {
    msgEl.textContent = 'Password must be at least 6 characters.';
    msgEl.style.color = 'var(--red)';
    return;
  }

  if (password !== confirmPassword) {
    msgEl.textContent = 'Passwords do not match.';
    msgEl.style.color = 'var(--red)';
    return;
  }

  let res;
  try {
    res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fullName: fullname, idNumber: idnumber, phoneNumber: phone, password })
    });
  } catch (err) {
    msgEl.textContent = 'Could not reach the server. Check your connection and try again.';
    msgEl.style.color = 'var(--red)';
    return;
  }

  let json;
  try {
    json = await res.json();
  } catch (err) {
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
    // e.g. "Phone must be +254xxxxxxxxx" instead of just "Validation failed"
    msgEl.textContent = buildErrorMessage(json) || 'Registration failed';
    msgEl.style.color = 'var(--red)';
  }
});

// Live match feedback while typing confirm password
const passwordEl = document.getElementById('password');
const confirmEl = document.getElementById('confirm-password');
function checkPasswordMatch() {
  const msgEl = document.getElementById('auth-message');
  if (!confirmEl.value) {
    if (msgEl.textContent === 'Passwords do not match.') msgEl.textContent = '';
    confirmEl.classList.remove('input-error', 'input-ok');
    return;
  }
  if (passwordEl.value === confirmEl.value) {
    confirmEl.classList.remove('input-error');
    confirmEl.classList.add('input-ok');
    if (msgEl.textContent === 'Passwords do not match.') msgEl.textContent = '';
  } else {
    confirmEl.classList.remove('input-ok');
    confirmEl.classList.add('input-error');
  }
}
passwordEl.addEventListener('input', checkPasswordMatch);
confirmEl.addEventListener('input', checkPasswordMatch);