document.getElementById('login-form').addEventListener('submit', async function(e) {
  e.preventDefault();
  const login = document.getElementById('login').value;
  const password = document.getElementById('password').value;
  try {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ login, password })
    });
    const json = await res.json();
    if (json.success) {
      localStorage.setItem('akiba_token', json.data.token);
      localStorage.setItem('akiba_phone', json.data.user.phoneNumber);
      window.location.href = 'dashboard.html';
    } else {
      document.getElementById('auth-message').textContent = json.message || 'Login failed';
      document.getElementById('auth-message').style.color = 'red';
    }
  } catch (err) {
    document.getElementById('auth-message').textContent = 'Network error';
    document.getElementById('auth-message').style.color = 'red';
  }
});