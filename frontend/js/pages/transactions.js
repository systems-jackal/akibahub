requireAuth();

async function loadTransactions(type, groupId, start, end) {
  try {
    const txns = await fetchTransactions(type || null, groupId || null, start || null, end || null);
    const div = document.getElementById('transactions-table');
    if (txns.length === 0) {
      div.innerHTML = '<p>No transactions match your criteria.</p>';
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
              <td>${escapeHtml(t.reference) || '—'}</td>
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
  // <input type="date"> yields YYYY-MM-DD; backend expects ISO date-time.
  const startRaw = document.getElementById('filter-start').value;
  const endRaw = document.getElementById('filter-end').value;
  const start = startRaw ? `${startRaw}T00:00:00` : null;
  const end = endRaw ? `${endRaw}T23:59:59` : null;
  loadTransactions(type, groupId, start, end);
});

// Initial load without filters
loadTransactions();