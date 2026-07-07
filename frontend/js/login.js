document.getElementById('login-form').addEventListener('submit', async function(e) {
  e.preventDefault();
  const phone = document.getElementById('phone').value;
  const password = document.getElementById('password').value;
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ phoneNumber: phone, password })
  });
  const data = await res.json();
  if (res.ok) {
    localStorage.setItem('akiba_token', data.token);
    localStorage.setItem('akiba_phone', data.phoneNumber);
    window.location.href = 'dashboard.html';
  } else {
    const msg = document.getElementById('auth-message');
    msg.textContent = data.error || 'Login failed';
    msg.style.color = 'red';
  }
});