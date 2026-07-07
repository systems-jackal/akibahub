requireAuth();

async function loadWallet() {
  try {
    const wallets = await fetchMyWallets();
    const personal = wallets.find(w => w.type === 'PERSONAL');
    const balanceEl = document.getElementById('balance');
    if (balanceEl) balanceEl.textContent = formatCurrency(personal?.balance || 0);

    // Recent personal transactions
    const txns = await fetchTransactions('', null, null, null); // all personal for now
    const recentDiv = document.getElementById('recent-transactions');
    if (txns && txns.length > 0) {
      const recent = txns.slice(0, 5);
      recentDiv.innerHTML = `
        <table>
          <thead><tr><th>Date</th><th>Type</th><th>Amount</th><th>Reference</th></tr></thead>
          <tbody>
            ${recent.map(t => `
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
    } else {
      recentDiv.innerHTML = '<p>No transactions yet.</p>';
    }
  } catch (err) {
    showAlert(err.message, 'error');
  }
}

document.getElementById('deposit-btn').addEventListener('click', async () => {
  const amount = parseFloat(document.getElementById('amount').value);
  if (!amount || amount <= 0) return showAlert('Enter a valid amount', 'error');
  try {
    await deposit(amount);
    showAlert('Deposit successful');
    loadWallet();
  } catch (e) { showAlert(e.message, 'error'); }
});

document.getElementById('withdraw-btn').addEventListener('click', async () => {
  const amount = parseFloat(document.getElementById('amount').value);
  if (!amount || amount <= 0) return showAlert('Enter a valid amount', 'error');
  try {
    await withdraw(amount);
    showAlert('Withdrawal successful');
    loadWallet();
  } catch (e) { showAlert(e.message, 'error'); }
});

loadWallet();