// Toggle password visibility
function initPasswordToggles() {
  document.querySelectorAll('.toggle-password').forEach(btn => {
    btn.addEventListener('click', function() {
      const input = this.previousElementSibling;
      const type = input.type === 'password' ? 'text' : 'password';
      input.type = type;
      this.textContent = type === 'password' ? '👁️' : '🙈';
    });
  });
}

// Phone input formatting (simple mask)
function initPhoneInputs() {
  document.querySelectorAll('input[data-phone]').forEach(input => {
    input.value = '+254 ';
    input.addEventListener('input', function(e) {
      let val = input.value.replace(/[^0-9+]/g, '');
      if (!val.startsWith('+254')) {
        val = '+254' + val.replace(/\+/g, '');
      }
      const digits = val.replace(/\+254/, '');
      let formatted = '+254';
      if (digits.length > 0) formatted += ' ' + digits.substring(0, 3);
      if (digits.length > 3) formatted += ' ' + digits.substring(3, 6);
      if (digits.length > 6) formatted += ' ' + digits.substring(6, 9);
      input.value = formatted;
    });
  });
}

// Show/hide loading spinners or alerts
function showAlert(message, type = 'success') {
  const alertDiv = document.createElement('div');
  alertDiv.className = `alert alert-${type}`;
  alertDiv.textContent = message;
  document.querySelector('.main-content')?.prepend(alertDiv);
  setTimeout(() => alertDiv.remove(), 3000);
}

// Render sidebar active state
function setActiveNav() {
  const currentPage = location.pathname.split('/').pop();
  document.querySelectorAll('.sidebar nav a').forEach(link => {
    const href = link.getAttribute('href');
    if (href === currentPage) {
      link.classList.add('active');
    }
  });
}

// Load sidebar into page (if using dynamic load, otherwise sidebar is inline)
function loadSidebar() {
  // Sidebar is already hardcoded in each page, so just set active
  setActiveNav();
}

// Check auth and redirect if not logged in
function requireAuth() {
  if (!getToken()) {
    window.location.href = 'login.html';
  }
}

// Logout
function logout() {
  localStorage.removeItem('akiba_token');
  localStorage.removeItem('akiba_phone');
  window.location.href = 'index.html';
}

// Attach logout event to the sidebar logout button
document.addEventListener('DOMContentLoaded', () => {
  const logoutBtn = document.getElementById('logout-btn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', logout);
  }
  initPasswordToggles();
  initPhoneInputs();
  loadSidebar();
});