requireAuth();

function initialsFromName(fullName) {
  const parts = String(fullName || '').trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return '?';
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

function formatAccountId(id) {
  const n = Number(id) || 0;
  return 'AH-' + String(n).padStart(6, '0');
}

function maskIdNumber(idNumber) {
  const s = String(idNumber || '');
  if (s.length < 4) return '****';
  return '****' + s.slice(-4);
}

function formatMemberSince(iso) {
  if (!iso) return '—';
  try {
    return new Date(iso).toLocaleDateString('en-KE', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  } catch {
    return '—';
  }
}

function setBusy(btn, busy, labelWhenBusy) {
  if (!btn) return;
  btn.disabled = !!busy;
  if (busy && labelWhenBusy) {
    btn.dataset.prevLabel = btn.innerHTML;
    btn.innerHTML = `<span class="wrap">${labelWhenBusy}</span>`;
  } else if (!busy && btn.dataset.prevLabel) {
    btn.innerHTML = btn.dataset.prevLabel;
    delete btn.dataset.prevLabel;
  }
}

function applyHero(user) {
  document.getElementById('profile-avatar').textContent = initialsFromName(user.fullName);
  document.getElementById('profile-name').textContent = user.fullName || 'Account holder';
  document.getElementById('profile-phone').textContent = user.phoneNumber || '';
  document.getElementById('profile-account-id').textContent = formatAccountId(user.id);
  document.getElementById('profile-id-masked').textContent = maskIdNumber(user.idNumber);
  document.getElementById('profile-member-since').textContent = formatMemberSince(user.createdAt);
  document.getElementById('detail-id-readonly').textContent = user.idNumber || '—';

  document.getElementById('fullname').value = user.fullName || '';
  const phoneInput = document.getElementById('phone');
  if (user.phoneNumber) {
    phoneInput.value = user.phoneNumber.replace(/^\+254/, '').replace(/(\d{3})(\d{3})(\d{0,3})/, (_, a, b, c) =>
      c ? `${a} ${b} ${c}` : b ? `${a} ${b}` : a
    ).trim();
  }

  cacheCurrentUser(user);
}

async function loadAccount() {
  try {
    const [user, wallets, groups] = await Promise.all([
      fetchCurrentUser(),
      fetchMyWallets(),
      fetchMyGroups()
    ]);

    applyHero(user);

    const personal = (wallets || []).find(w => w.type === 'PERSONAL');
    const groupWallets = (wallets || []).filter(w => w.type === 'GROUP');
    const groupTotal = groupWallets.reduce((sum, w) => sum + parseFloat(w.balance || 0), 0);

    document.getElementById('snap-personal').textContent = formatCurrency(personal?.balance || 0);
    document.getElementById('snap-groups').textContent = String((groups || []).length);
    document.getElementById('snap-group-bal').textContent = formatCurrency(groupTotal);
  } catch (err) {
    showAlert(err.message || 'Could not load your account.', 'error');
  }
}

document.getElementById('update-profile').addEventListener('click', async () => {
  const btn = document.getElementById('update-profile');
  const fullName = document.getElementById('fullname').value.trim();
  const phoneRaw = document.getElementById('phone').value.trim();
  const phone = phoneRaw ? normalizeKenyanPhone(phoneRaw) : '';

  if (!fullName) return showAlert('Full name is required.', 'error');
  if (!phone) return showAlert('Enter a valid Kenyan phone number (9 digits after +254).', 'error');

  setBusy(btn, true, 'Saving…');
  try {
    const updated = await updateProfile({ fullName, phoneNumber: phone });
    applyHero(updated);
    showAlert('Your profile has been updated.');
  } catch (e) {
    showAlert(e.message, 'error');
  } finally {
    setBusy(btn, false);
  }
});

document.getElementById('change-password').addEventListener('click', async () => {
  const btn = document.getElementById('change-password');
  const current = document.getElementById('current-password').value;
  const newPass = document.getElementById('new-password').value;
  const confirmPass = document.getElementById('confirm-new-password').value;

  if (!current || !newPass) return showAlert('Both password fields are required.', 'error');
  if (newPass.length < 6) return showAlert('New password must be at least 6 characters.', 'error');
  if (newPass !== confirmPass) return showAlert('New passwords do not match.', 'error');

  setBusy(btn, true, 'Updating…');
  try {
    await changePassword(current, newPass);
    showAlert('Password changed. Other sessions have been signed out.');
    document.getElementById('current-password').value = '';
    document.getElementById('new-password').value = '';
    document.getElementById('confirm-new-password').value = '';
  } catch (e) {
    showAlert(e.message, 'error');
  } finally {
    setBusy(btn, false);
  }
});

document.getElementById('account-logout').addEventListener('click', (e) => {
  e.preventDefault();
  logout('index.html');
});

loadAccount();
