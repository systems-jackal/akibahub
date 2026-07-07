requireAuth();

async function loadGroups() {
  try {
    const groups = await fetchMyGroups();
    const list = document.getElementById('groups-list');
    if (groups.length === 0) {
      list.innerHTML = '<p>No groups yet. Create or join one!</p>';
    } else {
      list.innerHTML = groups.map(g => `
        <div class="group-card">
          <h4><a href="group.html?id=${g.id}">${g.name}</a></h4>
          <p>${g.description || ''}</p>
          <p>ID: ${g.id}</p>
        </div>
      `).join('');
    }
  } catch (err) {
    showAlert(err.message, 'error');
  }
}

document.getElementById('create-btn').addEventListener('click', async () => {
  const name = document.getElementById('group-name').value.trim();
  const desc = document.getElementById('group-desc').value.trim();
  if (!name) return showAlert('Group name required', 'error');
  try {
    await createGroup(name, desc);
    showAlert('Group created!');
    loadGroups();
  } catch (e) { showAlert(e.message, 'error'); }
});

document.getElementById('join-btn').addEventListener('click', async () => {
  const id = document.getElementById('join-group-id').value;
  if (!id) return showAlert('Enter a Group ID', 'error');
  try {
    await joinGroup(id);
    showAlert('Joined group!');
    loadGroups();
  } catch (e) { showAlert(e.message, 'error'); }
});

loadGroups();
