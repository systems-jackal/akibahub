requireAuth();

async function loadTransactions(type, groupId, start, end) {
  try {
    const txns = await fetchTransactions(type || null, groupId || null, start || null, end || null);
    const div = document.getElementById('transactions-table');
    if (txns.length === 0) {
      div.innerHTML = '<p>No transactions match the criteria.</p>';
      return;
    }
    div.innerHTML = `
      <table>
        <thead><tr><th>Date</th><th>Type</th><th>Amount</th><th>Reference</th></tr></thead>
        <tbody>
          ${txns.map(t => `
            <tr>
              <td>${formatDate(t.timestamp)}</td>
              <td>${t.type}</td>
              <td>KES ${formatCurrency(t.amount)}</td>
              <td>${t.reference || ''}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;
  } catch (err) {
    showAlert(err.message, 'error');
  }
}

document.getElementById('apply-filter').addEventListener('click', () => {
  const type = document.getElementById('filter-type').value.trim();
  const groupId = document.getElementById('filter-group').value;
  const start = document.getElementById('filter-start').value;
  const end = document.getElementById('filter-end').value;
  loadTransactions(type, groupId, start, end);
});

// Initial load with no filters
loadTransactions();