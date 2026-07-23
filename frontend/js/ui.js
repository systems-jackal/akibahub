// Toggle password visibility (show/hide). Bound once per button.
function initPasswordToggles() {
  document.querySelectorAll('.toggle-password').forEach(btn => {
    if (btn.dataset.toggleBound === '1') return;
    btn.dataset.toggleBound = '1';
    btn.setAttribute('aria-label', 'Show password');
    btn.setAttribute('type', 'button');
    btn.addEventListener('click', function() {
      const input = this.closest('.password-wrapper')?.querySelector('input');
      if (!input) return;
      const showing = input.type === 'password';
      input.type = showing ? 'text' : 'password';
      this.textContent = showing ? '🙈' : '👁️';
      this.setAttribute('aria-label', showing ? 'Hide password' : 'Show password');
      this.setAttribute('aria-pressed', showing ? 'true' : 'false');
    });
  });
}

// Phone input formatting for fields that store the full +254 number.
// Inputs inside .phone-input (with a visual +254 prefix) only hold local
// digits — those must not be overwritten with "+254 ".
function initPhoneInputs() {
  document.querySelectorAll('input[data-phone]').forEach(input => {
    const hasVisualPrefix = input.closest('.phone-input')?.querySelector('.prefix');
    if (hasVisualPrefix) {
      input.addEventListener('input', function() {
        // Strip country code / leading 0 so "0712…" and "254712…" both
        // become the 9-digit national number the +254 prefix already implies.
        const digits = input.value.replace(/\D/g, '')
          .replace(/^254/, '')
          .replace(/^0+/, '')
          .slice(0, 9);
        let formatted = '';
        if (digits.length > 0) formatted = digits.substring(0, 3);
        if (digits.length > 3) formatted += ' ' + digits.substring(3, 6);
        if (digits.length > 6) formatted += ' ' + digits.substring(6, 9);
        input.value = formatted;
      });
      return;
    }

    if (!input.value) input.value = '+254 ';
    input.addEventListener('input', function() {
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
  const currentPage = location.pathname.split('/').pop() || '';
  document.querySelectorAll('.sidebar nav a').forEach(link => {
    const href = link.getAttribute('href');
    if (href === currentPage) {
      link.classList.add('active');
    }
  });
}

function initialsFromName(fullName) {
  const parts = String(fullName || '').trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return '?';
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

const USER_CACHE_KEY = 'akiba_user_cache';

function cacheCurrentUser(user) {
  if (!user) return;
  try {
    sessionStorage.setItem(USER_CACHE_KEY, JSON.stringify(user));
  } catch (e) { /* ignore quota */ }
}

function readCachedUser() {
  try {
    const raw = sessionStorage.getItem(USER_CACHE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch (e) {
    return null;
  }
}

function clearCachedUser() {
  try { sessionStorage.removeItem(USER_CACHE_KEY); } catch (e) { /* ignore */ }
}

function renderSidebarProfileChip(user) {
  const sidebar = document.querySelector('.sidebar');
  if (!sidebar || !user) return;

  const esc = (typeof escapeHtml === 'function')
    ? escapeHtml
    : (s) => String(s ?? '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');

  let chip = document.getElementById('sidebar-profile-chip');
  if (!chip) {
    chip = document.createElement('a');
    chip.id = 'sidebar-profile-chip';
    chip.className = 'sidebar-profile-chip';
    chip.href = 'settings.html';
    chip.title = 'Open account';
    const logo = sidebar.querySelector('.logo');
    const nav = sidebar.querySelector('nav');
    if (nav) {
      sidebar.insertBefore(chip, nav);
    } else if (logo && logo.nextSibling) {
      sidebar.insertBefore(chip, logo.nextSibling);
    } else {
      sidebar.appendChild(chip);
    }
  }

  const name = user.fullName || 'Account';
  chip.innerHTML = `
    <span class="chip-avatar">${esc(initialsFromName(name))}</span>
    <span class="chip-text">
      <span class="chip-name">${esc(name)}</span>
      <span class="chip-sub">My account</span>
    </span>
  `;
}

async function initSidebarProfile() {
  if (!document.querySelector('.sidebar') || !getToken()) return;

  const cached = readCachedUser();
  if (cached) renderSidebarProfileChip(cached);

  try {
    const user = await fetchCurrentUser();
    cacheCurrentUser(user);
    renderSidebarProfileChip(user);
  } catch (e) {
    // Keep cached chip if present; auth redirect handled elsewhere on 401.
  }
}

// Load sidebar into page (if using dynamic load, otherwise sidebar is inline)
function loadSidebar() {
  setActiveNav();
  initSidebarProfile();
}

// Check auth and redirect if not logged in
function requireAuth() {
  if (!getToken()) {
    window.location.href = 'login.html';
    return;
  }
  initInactivityTimer();
}

// Logout. Do not bind this directly as an event listener — browsers pass
// the click Event as the first argument, which used to become
// location.href = "[object MouseEvent]" and nginx returned 404.
async function logout(redirectTo = 'index.html') {
  const target = (typeof redirectTo === 'string' && redirectTo) ? redirectTo : 'index.html';
  await apiLogout(); // revokes refresh tokens server-side, clears storage either way
  window.location.href = target;
}

function handleLogoutClick(e) {
  e.preventDefault();
  logout('index.html');
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
    logoutBtn.addEventListener('click', handleLogoutClick);
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