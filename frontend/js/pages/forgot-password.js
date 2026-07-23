document.getElementById('forgot-form').addEventListener('submit', async function(e) {
  e.preventDefault();
  const msgEl = document.getElementById('auth-message');

  const phoneRaw = document.getElementById('phone').value.replace(/\s/g, '');
  const phone = phoneRaw.startsWith('+254')
    ? phoneRaw
    : '+254' + phoneRaw.replace(/^0+/, '').replace(/^\+254/, '');
  const idNumber = document.getElementById('idnumber').value.trim();
  const password = document.getElementById('password').value;
  const confirmPassword = document.getElementById('confirm-password').value;

  if (!/^\+254\d{9}$/.test(phone)) {
    msgEl.textContent = 'Enter a valid phone number (9 digits).';
    msgEl.style.color = 'var(--red)';
    return;
  }
  if (!/^\d{8}$/.test(idNumber)) {
    msgEl.textContent = 'ID number must be exactly 8 digits.';
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

  try {
    const res = await fetch('/api/auth/forgot-password', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        phoneNumber: phone,
        idNumber,
        newPassword: password
      })
    });
    const json = await res.json();
    if (json.success) {
      msgEl.textContent = json.message || 'Password updated. Redirecting to login…';
      msgEl.style.color = 'var(--green)';
      setTimeout(() => { window.location.href = 'login.html'; }, 1500);
    } else {
      msgEl.textContent = json.message || 'Could not reset password.';
      msgEl.style.color = 'var(--red)';
    }
  } catch (err) {
    msgEl.textContent = 'Network error. Please try again.';
    msgEl.style.color = 'var(--red)';
  }
});

const passwordEl = document.getElementById('password');
const confirmEl = document.getElementById('confirm-password');
function checkPasswordMatch() {
  const msgEl = document.getElementById('auth-message');
  if (!confirmEl.value) {
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
