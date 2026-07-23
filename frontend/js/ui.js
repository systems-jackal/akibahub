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
    return;
  }
  initInactivityTimer();
  applyRoleBasedNav();
}

// Shows the "Admin" nav link only for ADMIN accounts, and bounces a
// non-admin away from admin.html itself. The REAL enforcement is
// server-side (@PreAuthorize("hasRole('ADMIN')") on every /api/admin/**
// endpoint) - this is just about not showing a link or a page that would
// immediately 403 anyway, not a security boundary in itself.
async function applyRoleBasedNav() {
  try {
    const user = await fetchCurrentUser();
    const isAdmin = user.role === 'ADMIN';
    const currentPage = location.pathname.split('/').pop();

    if (currentPage === 'admin.html' && !isAdmin) {
      window.location.href = 'dashboard.html';
      return;
    }

    if (isAdmin && !document.getElementById('admin-nav-link')) {
      const logoutLink = document.getElementById('logout-btn');
      if (logoutLink) {
        const adminLink = document.createElement('a');
        adminLink.href = 'admin.html';
        adminLink.id = 'admin-nav-link';
        adminLink.title = 'Admin';
        adminLink.innerHTML = '<span class="icon">🛡️</span><span class="label">Admin</span>';
        if (currentPage === 'admin.html') adminLink.classList.add('active');
        logoutLink.parentElement.insertBefore(adminLink, logoutLink);
      }
    }
  } catch (e) {
    // If this fails (e.g. token mid-refresh), fail closed on admin.html
    // specifically - never leave an unverified user sitting on the
    // admin page - but don't disrupt normal pages over it.
    if (location.pathname.split('/').pop() === 'admin.html') {
      window.location.href = 'dashboard.html';
    }
  }
}

// Logout
async function logout(redirectTo = 'index.html') {
  await apiLogout(); // revokes refresh tokens server-side, clears storage either way
  window.location.href = redirectTo;
}

// ---------- Auto-logout on inactivity ----------
// Independent of token expiry - this is a client-enforced security
// control on top of the server-side session (refresh tokens, short-lived
// access tokens). Even if a device is left unlocked, an idle session
// ends itself. Uses the real logout() above, so an inactivity timeout
// actually revokes the refresh token server-side too, not just a
// client-side redirect that leaves the session usable elsewhere.
const INACTIVITY_LIMIT_MS = 10 * 60 * 1000; // 10 minutes idle
const INACTIVITY_WARNING_MS = 60 * 1000;    // warn 60s before logging out
const ACTIVITY_THROTTLE_MS = 3000;          // don't reset the timer on every single mousemove event

let inactivityTimerStarted = false;
let inactivityTimeout = null;
let warningTimeout = null;
let countdownInterval = null;
let warningEl = null;
let lastActivityReset = 0;

function initInactivityTimer() {
  if (inactivityTimerStarted) return; // guard against double-init
  inactivityTimerStarted = true;

  ['mousemove', 'mousedown', 'keydown', 'scroll', 'touchstart'].forEach(evt => {
    document.addEventListener(evt, () => {
      const now = Date.now();
      if (now - lastActivityReset > ACTIVITY_THROTTLE_MS) {
        lastActivityReset = now;
        resetInactivityTimer();
      }
    }, { passive: true });
  });

  resetInactivityTimer();
}

function resetInactivityTimer() {
  clearTimeout(inactivityTimeout);
  clearTimeout(warningTimeout);
  dismissInactivityWarning();

  warningTimeout = setTimeout(showInactivityWarning, INACTIVITY_LIMIT_MS - INACTIVITY_WARNING_MS);
  inactivityTimeout = setTimeout(() => {
    dismissInactivityWarning();
    // Full page navigation - this isn't just hiding the modal, the
    // entire authenticated page is torn down and replaced by the login
    // screen. Straight to login.html (not the marketing homepage) since
    // this is a timeout, not someone choosing to leave the app - they
    // should be able to log back in immediately with the fewest clicks.
    logout('login.html?reason=inactivity');
  }, INACTIVITY_LIMIT_MS);
}

function showInactivityWarning() {
  if (warningEl) return;
  let secondsLeft = Math.floor(INACTIVITY_WARNING_MS / 1000);

  warningEl = document.createElement('div');
  warningEl.className = 'inactivity-overlay';
  warningEl.innerHTML = `
    <div class="inactivity-modal">
      <div class="matrix-loader"><span></span><span></span><span></span><span></span><span></span></div>
      <h3>⚠ SESSION_TIMEOUT_WARNING</h3>
      <p>You've been inactive. For your security, you'll be logged out in
        <span id="inactivity-countdown">${secondsLeft}</span>s.</p>
      <button class="btn-primary full-width" id="stay-logged-in-btn">Stay Logged In</button>
    </div>
  `;
  document.body.appendChild(warningEl);

  const countdownEl = document.getElementById('inactivity-countdown');
  countdownInterval = setInterval(() => {
    secondsLeft--;
    if (countdownEl) countdownEl.textContent = secondsLeft;
    if (secondsLeft <= 0) clearInterval(countdownInterval);
  }, 1000);

  document.getElementById('stay-logged-in-btn').addEventListener('click', resetInactivityTimer);
}

function dismissInactivityWarning() {
  if (countdownInterval) clearInterval(countdownInterval);
  if (warningEl) {
    warningEl.remove();
    warningEl = null;
  }
}

// Ledger ticker live clock — purely cosmetic (the "SYNCED" status reflects
// that the page loaded, not a live connection), updates once a second on
// any page that has the ticker element.
function initLedgerTicker() {
  const timeEl = document.getElementById('ticker-time');
  if (!timeEl) return;
  function tick() {
    timeEl.textContent = new Date().toLocaleTimeString('en-KE', { hour12: false });
  }
  tick();
  setInterval(tick, 1000);
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
  initLedgerTicker();
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