requireAuth();

async function loadProfile() {
  try {
    const user = await fetchCurrentUser();
    document.getElementById('fullname').value = user.fullName || '';
    const phoneInput = document.getElementById('phone');
    if (user.phoneNumber) {
      const digits = user.phoneNumber.replace('+254', '');
      phoneInput.value = digits;
    }
  } catch (err) {
    showAlert('Could not load your profile.', 'error');
  }
}

document.getElementById('update-profile').addEventListener('click', async () => {
  const fullName = document.getElementById('fullname').value.trim();
  const phoneRaw = document.getElementById('phone').value.trim();
  const phone = phoneRaw ? normalizeKenyanPhone(phoneRaw) : '';
  if (phoneRaw && !phone) {
    return showAlert('Enter a valid Kenyan phone number (9 digits after +254).', 'error');
  }
  try {
    await updateProfile({ fullName, phoneNumber: phone });
    showAlert('Your profile has been updated.');
  } catch (e) {
    showAlert(e.message, 'error');
  }
});

document.getElementById('change-password').addEventListener('click', async () => {
  const current = document.getElementById('current-password').value;
  const newPass = document.getElementById('new-password').value;
  const confirmPass = document.getElementById('confirm-new-password').value;
  if (!current || !newPass) return showAlert('Both fields are required.', 'error');
  if (newPass.length < 6) return showAlert('New password must be at least 6 characters.', 'error');
  if (newPass !== confirmPass) return showAlert('New passwords do not match.', 'error');
  try {
    await changePassword(current, newPass);
    showAlert('Your password has been changed.');
    document.getElementById('current-password').value = '';
    document.getElementById('new-password').value = '';
    document.getElementById('confirm-new-password').value = '';
  } catch (e) {
    showAlert(e.message, 'error');
  }
});

loadProfile();