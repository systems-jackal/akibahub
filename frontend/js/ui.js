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

// ===================== MOBILE SIDEBAR TOGGLE =====================
(function() {
  // Only run on authenticated pages that have a sidebar
  const sidebar = document.querySelector('.sidebar');
  if (!sidebar) return;

  // Create hamburger button (visible only on phones ≤768px via CSS)
  const hamburger = document.createElement('button');
  hamburger.className = 'hamburger';
  hamburger.innerHTML = '&#9776;'; // ☰
  hamburger.setAttribute('aria-label', 'Toggle menu');
  document.body.appendChild(hamburger);

  // Create overlay
  const overlay = document.createElement('div');
  overlay.className = 'sidebar-overlay';
  document.body.appendChild(overlay);

  // Open/close functions
  function openSidebar() {
    sidebar.classList.add('open');
    overlay.classList.add('active');
    hamburger.innerHTML = '&#10005;'; // ✕
  }
  function closeSidebar() {
    sidebar.classList.remove('open');
    overlay.classList.remove('active');
    hamburger.innerHTML = '&#9776;'; // ☰
  }

  hamburger.addEventListener('click', function() {
    if (sidebar.classList.contains('open')) {
      closeSidebar();
    } else {
      openSidebar();
    }
  });

  overlay.addEventListener('click', closeSidebar);

  // Close sidebar when any nav link is clicked
  sidebar.querySelectorAll('a').forEach(link => {
    link.addEventListener('click', function() {
      closeSidebar();
    });
  });
})();