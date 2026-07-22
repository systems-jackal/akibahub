document.getElementById('register-form').addEventListener('submit', async function(e) {
  e.preventDefault();
  const fullname = document.getElementById('fullname').value;
  const idnumber = document.getElementById('idnumber').value;
  const phone = document.getElementById('phone').value.replace(/\s/g, ''); // remove spaces
  const password = document.getElementById('password').value;
  try {
    const res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fullName: fullname, idNumber: idnumber, phoneNumber: phone, password })
    });
    const json = await res.json();
    if (json.success) {
      setTokens(json.data.token, json.data.refreshToken);
      localStorage.setItem('akiba_phone', json.data.user.phoneNumber);
      window.location.href = 'dashboard.html';
    } else {
      document.getElementById('auth-message').textContent = json.message || 'Registration failed';
      document.getElementById('auth-message').style.color = 'var(--red)';
    }
  } catch (err) {
    document.getElementById('auth-message').textContent = 'Network error';
    document.getElementById('auth-message').style.color = 'var(--red)';
  }
});