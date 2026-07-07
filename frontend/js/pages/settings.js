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
  let phone = document.getElementById('phone').value.replace(/\s/g, '');
  if (phone && !phone.startsWith('+254')) {
    phone = '+254' + phone;
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
  if (!current || !newPass) return showAlert('Both fields are required.', 'error');
  if (newPass.length < 6) return showAlert('New password must be at least 6 characters.', 'error');
  try {
    await changePassword(current, newPass);
    showAlert('Your password has been changed.');
  } catch (e) {
    showAlert(e.message, 'error');
  }
});

loadProfile();