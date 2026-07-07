requireAuth();

async function loadProfile() {
  try {
    const user = await fetchCurrentUser();
    document.getElementById('fullname').value = user.fullName || '';
    // phone field – prefill with the digits after +254
    const phoneInput = document.getElementById('phone');
    if (user.phoneNumber) {
      const digits = user.phoneNumber.replace('+254', '');
      phoneInput.value = digits;
    }
  } catch (err) {
    showAlert('Could not load profile', 'error');
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
    showAlert('Profile updated');
  } catch (e) {
    showAlert(e.message, 'error');
  }
});

document.getElementById('change-password').addEventListener('click', async () => {
  const current = document.getElementById('current-password').value;
  const newPass = document.getElementById('new-password').value;
  if (!current || !newPass) return showAlert('Both fields required', 'error');
  try {
    await changePassword(current, newPass);
    showAlert('Password changed successfully');
  } catch (e) {
    showAlert(e.message, 'error');
  }
});

loadProfile();