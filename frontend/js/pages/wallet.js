requireAuth();

async function loadWallet() {
  try {
    const wallets = await fetchMyWallets();
    const personal = wallets.find(w => w.type === 'PERSONAL');
    const balanceEl = document.getElementById('balance');
    if (balanceEl) balanceEl.textContent = formatCurrency(personal?.balance || 0);

    const txns = await fetchTransactions('', null, null, null);
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
                <td>${escapeHtml(t.type)}</td>
                <td>KES ${formatCurrency(t.amount)}</td>
                <td>${escapeHtml(t.reference) || '—'}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      `;
    } else {
      recentDiv.innerHTML = '<p class="text-muted">No transactions yet.</p>';
    }
  } catch (err) {
    showAlert(err.message, 'error');
  }
}

function openDepositFlow(prefillAmount) {
  openTopUpSheet({
    amount: prefillAmount,
    onDone: () => loadWallet()
  });
}

document.getElementById('deposit-btn').addEventListener('click', () => {
  openDepositFlow();
});

document.getElementById('withdraw-btn').addEventListener('click', async () => {
  const amount = parseFloat(document.getElementById('withdraw-amount').value);
  if (!amount || amount <= 0) return showAlert('Enter a valid withdraw amount.', 'error');
  try {
    await withdraw(amount);
    showAlert('Withdrawal recorded on ledger.');
    document.getElementById('withdraw-amount').value = '';
    loadWallet();
  } catch (e) {
    showAlert(e.message, 'error');
  }
});

const params = new URLSearchParams(location.search);
const action = params.get('action');
if (action === 'topup' || action === 'deposit') {
  const amt = params.get('amount');
  openDepositFlow(amt || undefined);
  history.replaceState({}, '', 'wallet.html');
} else if (action === 'withdraw') {
  const el = document.getElementById('withdraw-amount');
  if (el) {
    el.focus();
    el.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }
  history.replaceState({}, '', 'wallet.html');
}

loadWallet();
