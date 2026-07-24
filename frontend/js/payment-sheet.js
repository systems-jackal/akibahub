/**
 * Presentation STK top-up sheet: confirm → waiting → labeled PIN simulator → result.
 * Does not collect or store real M-Pesa PINs; demo-complete credits via the API.
 */
(function (global) {
  const POLL_MS = 2000;
  const DEMO_OVERLAY_DELAY_MS = 1200;

  let root = null;
  let pollTimer = null;
  let overlayTimer = null;
  let state = {
    step: 'confirm',
    amount: null,
    phone: null,
    reference: null,
    expiresAt: null,
    pin: '',
    busy: false,
    onDone: null
  };

  function esc(s) {
    if (typeof escapeHtml === 'function') return escapeHtml(s);
    return String(s ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function formatKes(n) {
    const v = Number(n);
    if (!Number.isFinite(v)) return '0.00';
    return v.toLocaleString('en-KE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  function ensureRoot() {
    if (root) return root;
    root = document.createElement('div');
    root.id = 'payment-sheet-root';
    root.className = 'pay-sheet-root hidden';
    root.setAttribute('aria-hidden', 'true');
    document.body.appendChild(root);
    root.addEventListener('click', (e) => {
      if (e.target === root) {
        // Only dismiss from confirm / result — not mid-STK.
        if (state.step === 'confirm' || state.step === 'success' || state.step === 'fail') {
          closeSheet();
        }
      }
    });
    return root;
  }

  function clearTimers() {
    if (pollTimer) {
      clearInterval(pollTimer);
      pollTimer = null;
    }
    if (overlayTimer) {
      clearTimeout(overlayTimer);
      overlayTimer = null;
    }
  }

  function closeSheet() {
    clearTimers();
    state.busy = false;
    state.pin = '';
    if (root) {
      root.classList.add('hidden');
      root.setAttribute('aria-hidden', 'true');
      root.innerHTML = '';
    }
  }

  function setStep(step) {
    state.step = step;
    render();
  }

  function startPolling() {
    clearTimers();
    pollTimer = setInterval(async () => {
      if (!state.reference) return;
      try {
        const status = await fetchPaymentStatus(state.reference);
        if (status.status === 'COMPLETED') {
          clearTimers();
          state.balance = status.balance;
          setStep('success');
          if (typeof state.onDone === 'function') state.onDone(status);
        } else if (status.status === 'FAILED' || status.status === 'EXPIRED' || status.status === 'CANCELLED') {
          clearTimers();
          state.failReason = status.status;
          setStep('fail');
        }
        // No client-side "is expiresAt in the past?" check here on purpose.
        // expiresAt comes from the backend as a plain LocalDateTime with no
        // timezone offset attached (e.g. "2025-07-24T15:32:00"). When the
        // browser does new Date(thatString), JS parses a timezone-less
        // datetime as the BROWSER'S OWN local time, not the server's. If
        // the server runs in a different zone than whoever's testing (very
        // likely here — server clock vs. Nairobi time), that misreads the
        // real expiry by the zone offset. With only a 120s TTL, even a
        // 1-hour mismatch makes the client think it's already expired
        // before the STK screen has even finished appearing. The backend
        // already flips PENDING -> EXPIRED itself once the real TTL
        // elapses (PaymentService.expireIfNeeded(), checked on every
        // status poll) using only its own clock — no cross-machine time
        // comparison needed. Polling that status, which this loop already
        // does every 2s, is the correct and only source of truth here.
      } catch (e) {
        // Keep waiting; transient poll errors are non-fatal.
      }
    }, POLL_MS);
  }

  async function submitConfirm() {
    if (state.busy) return;
    const amount = parseFloat(state.amount);
    if (!amount || amount <= 0) {
      showAlert('Enter a valid amount (KES).', 'error');
      return;
    }
    state.busy = true;
    render();
    try {
      const pending = await initiateDeposit(amount, state.phone);
      state.reference = pending.reference;
      state.expiresAt = pending.expiresAt;
      state.amount = pending.amount;
      state.phone = pending.phone || state.phone;
      state.busy = false;
      setStep('waiting');
      startPolling();
      overlayTimer = setTimeout(() => {
        if (state.step === 'waiting') {
          state.pin = '';
          setStep('stk');
        }
      }, DEMO_OVERLAY_DELAY_MS);
    } catch (e) {
      state.busy = false;
      render();
      showAlert(e.message || 'Could not start deposit.', 'error');
    }
  }

  async function submitDemoPin() {
    if (state.busy || !state.reference) return;
    if (state.pin.length < 4) {
      showAlert('Enter a 4-digit PIN to continue the simulation.', 'error');
      return;
    }
    state.busy = true;
    render();
    try {
      const result = await completeDemoPayment(state.reference);
      clearTimers();
      state.balance = result.balance;
      state.busy = false;
      setStep('success');
      if (typeof state.onDone === 'function') state.onDone(result);
    } catch (e) {
      state.busy = false;
      state.failReason = e.message || 'FAILED';
      setStep('fail');
    }
  }

  function pinDots() {
    return [0, 1, 2, 3].map((i) =>
      `<span class="stk-dot${state.pin.length > i ? ' filled' : ''}"></span>`
    ).join('');
  }

  function renderConfirm() {
    return `
      <div class="pay-sheet" role="dialog" aria-labelledby="pay-sheet-title">
        <div class="pay-sheet-header">
          <h2 id="pay-sheet-title">Top up with M-Pesa</h2>
          <button type="button" class="pay-sheet-close" data-action="close" aria-label="Close">&times;</button>
        </div>
        <p class="pay-sheet-lead">Confirm amount and phone. Your wallet is credited only after payment succeeds.</p>
        <div class="form-group">
          <label for="pay-amount">Amount (KES)</label>
          <input type="number" id="pay-amount" min="1" step="0.01" value="${esc(state.amount || '')}" placeholder="e.g. 500" />
        </div>
        <div class="form-group">
          <label for="pay-phone">M-Pesa phone</label>
          <input type="tel" id="pay-phone" value="${esc(state.phone || '')}" data-phone />
        </div>
        <p class="text-muted small">Demo mode: you will see a labeled STK simulation — no real charge.</p>
        <div class="pay-sheet-actions">
          <button type="button" class="btn-outline" data-action="close">Cancel</button>
          <button type="button" class="btn-primary" data-action="confirm" ${state.busy ? 'disabled' : ''}>
            <span class="wrap">${state.busy ? 'Starting…' : 'Send STK Push'}</span>
          </button>
        </div>
      </div>`;
  }

  function renderWaiting() {
    return `
      <div class="pay-sheet" role="dialog" aria-labelledby="pay-wait-title">
        <div class="pay-sheet-header">
          <h2 id="pay-wait-title">Check your phone</h2>
          <button type="button" class="pay-sheet-close" data-action="cancel-wait" aria-label="Cancel">&times;</button>
        </div>
        <div class="pay-wait-body">
          <div class="matrix-loader"><span></span><span></span><span></span><span></span><span></span></div>
          <p>Enter your M-Pesa PIN on the prompt that appears on <strong>${esc(state.phone || 'your phone')}</strong>.</p>
          <p class="mono small">Ref: ${esc(state.reference || '—')}</p>
          <p class="text-muted small">Waiting for confirmation…</p>
        </div>
        <div class="pay-sheet-actions">
          <button type="button" class="btn-outline full-width" data-action="cancel-wait">Cancel</button>
        </div>
      </div>`;
  }

  function renderStk() {
    return `
      <div class="pay-sheet pay-sheet--stk" role="dialog" aria-labelledby="stk-title">
        <div class="stk-sim-banner">SIMULATION — not a real Safaricom prompt</div>
        <div class="stk-phone">
          <div class="stk-phone-bezel">
            <div class="stk-status-bar">Safaricom</div>
            <div class="stk-panel">
              <p class="stk-label" id="stk-title">Lipa na M-Pesa</p>
              <p class="stk-merchant">Akiba Hub</p>
              <p class="stk-amount">KES ${formatKes(state.amount)}</p>
              <p class="stk-hint">Enter M-Pesa PIN</p>
              <div class="stk-dots" aria-hidden="true">${pinDots()}</div>
              <div class="stk-pad" role="group" aria-label="PIN keypad">
                ${[1,2,3,4,5,6,7,8,9,'',0,'⌫'].map((k) => {
                  if (k === '') return '<span class="stk-key stk-key--spacer"></span>';
                  const action = k === '⌫' ? 'pin-back' : 'pin-digit';
                  const val = k === '⌫' ? '' : ` data-digit="${k}"`;
                  return `<button type="button" class="stk-key" data-action="${action}"${val}>${k}</button>`;
                }).join('')}
              </div>
              <div class="stk-actions">
                <button type="button" class="stk-btn stk-btn--cancel" data-action="cancel-wait">Cancel</button>
                <button type="button" class="stk-btn stk-btn--ok" data-action="pin-confirm" ${state.busy || state.pin.length < 4 ? 'disabled' : ''}>
                  ${state.busy ? '…' : 'OK'}
                </button>
              </div>
            </div>
          </div>
        </div>
        <p class="text-muted small text-center mt-10">This PIN never leaves your browser and is not stored.</p>
      </div>`;
  }

  function renderSuccess() {
    return `
      <div class="pay-sheet" role="dialog" aria-labelledby="pay-ok-title">
        <div class="pay-sheet-header">
          <h2 id="pay-ok-title">Deposit received</h2>
          <button type="button" class="pay-sheet-close" data-action="close" aria-label="Close">&times;</button>
        </div>
        <div class="pay-receipt">
          <p class="pay-receipt-amount">KES ${formatKes(state.amount)}</p>
          <p class="mono">Ref: ${esc(state.reference)}</p>
          ${state.balance != null ? `<p>New balance: <strong>KES ${formatKes(state.balance)}</strong></p>` : ''}
        </div>
        <div class="pay-sheet-actions">
          <button type="button" class="btn-primary full-width" data-action="close"><span class="wrap">Done</span></button>
        </div>
      </div>`;
  }

  function renderFail() {
    const reason = state.failReason || 'FAILED';
    return `
      <div class="pay-sheet" role="dialog" aria-labelledby="pay-fail-title">
        <div class="pay-sheet-header">
          <h2 id="pay-fail-title">Deposit not completed</h2>
          <button type="button" class="pay-sheet-close" data-action="close" aria-label="Close">&times;</button>
        </div>
        <p class="text-muted">${esc(String(reason))}. No funds were credited.</p>
        <p class="mono small">Ref: ${esc(state.reference || '—')}</p>
        <div class="pay-sheet-actions">
          <button type="button" class="btn-outline" data-action="close">Close</button>
          <button type="button" class="btn-primary" data-action="retry"><span class="wrap">Try again</span></button>
        </div>
      </div>`;
  }

  function render() {
    const el = ensureRoot();
    el.classList.remove('hidden');
    el.setAttribute('aria-hidden', 'false');
    let html = '';
    switch (state.step) {
      case 'waiting': html = renderWaiting(); break;
      case 'stk': html = renderStk(); break;
      case 'success': html = renderSuccess(); break;
      case 'fail': html = renderFail(); break;
      default: html = renderConfirm();
    }
    el.innerHTML = html;
    bind();
    if (state.step === 'confirm' && typeof initPhoneInputs === 'function') {
      initPhoneInputs();
    }
  }

  function bind() {
    if (!root) return;
    root.querySelectorAll('[data-action]').forEach((btn) => {
      btn.addEventListener('click', onAction);
    });
    const amountInput = root.querySelector('#pay-amount');
    const phoneInput = root.querySelector('#pay-phone');
    if (amountInput) {
      amountInput.addEventListener('input', () => { state.amount = amountInput.value; });
    }
    if (phoneInput) {
      phoneInput.addEventListener('input', () => { state.phone = phoneInput.value; });
    }
  }

  function onAction(e) {
    const action = e.currentTarget.getAttribute('data-action');
    if (action === 'close') {
      closeSheet();
      return;
    }
    if (action === 'cancel-wait') {
      clearTimers();
      closeSheet();
      showAlert('Top-up cancelled. If an STK was pending it will expire unused.', 'error');
      return;
    }
    if (action === 'confirm') {
      const amountInput = root.querySelector('#pay-amount');
      const phoneInput = root.querySelector('#pay-phone');
      if (amountInput) state.amount = amountInput.value;
      if (phoneInput) state.phone = phoneInput.value.replace(/\s/g, '');
      submitConfirm();
      return;
    }
    if (action === 'retry') {
      state.reference = null;
      state.pin = '';
      state.failReason = null;
      setStep('confirm');
      return;
    }
    if (action === 'pin-digit') {
      if (state.pin.length >= 4) return;
      state.pin += e.currentTarget.getAttribute('data-digit') || '';
      render();
      return;
    }
    if (action === 'pin-back') {
      state.pin = state.pin.slice(0, -1);
      render();
      return;
    }
    if (action === 'pin-confirm') {
      submitDemoPin();
    }
  }

  /**
   * @param {{ amount?: number|string, phone?: string, onDone?: function }} opts
   */
  async function openTopUpSheet(opts = {}) {
    clearTimers();
    let phone = opts.phone || '';
    if (!phone) {
      try {
        const user = (typeof readCachedUser === 'function' && readCachedUser())
          || (typeof fetchCurrentUser === 'function' ? await fetchCurrentUser() : null);
        if (user) {
          if (typeof cacheCurrentUser === 'function') cacheCurrentUser(user);
          phone = user.phoneNumber || '';
        }
      } catch (e) { /* ignore */ }
    }
    state = {
      step: 'confirm',
      amount: opts.amount != null ? String(opts.amount) : '',
      phone,
      reference: null,
      expiresAt: null,
      pin: '',
      busy: false,
      balance: null,
      failReason: null,
      onDone: opts.onDone || null
    };
    render();
  }

  global.openTopUpSheet = openTopUpSheet;
  global.closeTopUpSheet = closeSheet;
})(window);