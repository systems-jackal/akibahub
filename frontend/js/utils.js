// Format currency
function formatCurrency(amount) {
  return parseFloat(amount).toLocaleString('en-KE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

// Format date
function formatDate(dateString) {
  return new Date(dateString).toLocaleDateString('en-KE', { year: 'numeric', month: 'short', day: 'numeric' });
}

// Truncate text
function truncate(text, maxLength) {
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
}

// Escape any user-controlled string before inserting it into innerHTML.
// This used to live as a copy-pasted local function in group.js and
// groups.js only - dashboard.js, proposals.js, transactions.js, and
// wallet.js all rendered user-supplied text (proposal titles, statuses,
// transaction references) straight into innerHTML with no escaping at
// all, which is a stored-XSS hole: a proposal title like
// "<img src=x onerror=alert(document.cookie)>" would execute for every
// member who viewed their dashboard. Having one shared function in
// utils.js (loaded on every page before the page-specific script) means
// there's exactly one place to get this right, instead of five places
// that can silently drift out of sync.
function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}