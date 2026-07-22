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

// ---------- Charts ----------
// Hand-rolled SVG rather than a charting library: no external dependency
// to fetch, and it lets every bar/line match the "blocky, dithered,
// terminal" aesthetic exactly rather than looking like a generic chart
// library dropped onto a themed page.

// Renders a labeled block-bar chart. `data` is [{ label, value }, ...].
function renderBlockBarChart(containerEl, data, options = {}) {
  if (!data || data.length === 0) {
    containerEl.innerHTML = '<div class="chart-empty">NO_DATA_YET</div>';
    return;
  }
  const width = 480, height = 180, padding = 28;
  const barAreaWidth = width - padding * 2;
  const barGap = 16;
  const barWidth = Math.max(20, (barAreaWidth / data.length) - barGap);
  const maxVal = Math.max(...data.map(d => d.value), 1);
  const blockSize = 8, blockGap = 2;

  let svg = `<svg viewBox="0 0 ${width} ${height}" xmlns="http://www.w3.org/2000/svg">`;
  data.forEach((d, i) => {
    const x = padding + i * (barWidth + barGap);
    const barHeightPx = Math.max(4, (d.value / maxVal) * (height - 50));
    const numBlocks = Math.max(1, Math.floor(barHeightPx / (blockSize + blockGap)));
    for (let b = 0; b < numBlocks; b++) {
      const y = (height - 30) - (b + 1) * (blockSize + blockGap);
      svg += `<rect class="chart-bar-block" x="${x}" y="${y}" width="${barWidth}" height="${blockSize}" />`;
    }
    svg += `<text class="chart-axis-label" x="${x + barWidth / 2}" y="${height - 8}" text-anchor="middle">${escapeHtml(truncate(d.label, 10))}</text>`;
  });
  svg += `</svg>`;
  containerEl.innerHTML = svg;
}

// Renders a stepped (blockchain-block-like) line chart for a time series.
// `data` is [{ label, value }, ...] ordered chronologically.
function renderStepLineChart(containerEl, data) {
  if (!data || data.length < 2) {
    containerEl.innerHTML = '<div class="chart-empty">NOT_ENOUGH_HISTORY_YET</div>';
    return;
  }
  const width = 480, height = 160, padding = 24;
  const maxVal = Math.max(...data.map(d => d.value), 1);
  const minVal = Math.min(...data.map(d => d.value), 0);
  const range = Math.max(maxVal - minVal, 1);
  const stepX = (width - padding * 2) / (data.length - 1);

  const points = data.map((d, i) => {
    const x = padding + i * stepX;
    const y = height - padding - ((d.value - minVal) / range) * (height - padding * 2);
    return { x, y };
  });

  // Build a stepped path (horizontal then vertical between points) rather
  // than a smooth curve - reads as discrete "blocks" of state, matching
  // the ledger-entry-by-entry nature of the underlying data.
  let path = `M ${points[0].x} ${points[0].y}`;
  for (let i = 1; i < points.length; i++) {
    path += ` L ${points[i].x} ${points[i - 1].y} L ${points[i].x} ${points[i].y}`;
  }

  let svg = `<svg viewBox="0 0 ${width} ${height}" xmlns="http://www.w3.org/2000/svg">`;
  svg += `<path class="chart-line" d="${path}" />`;
  points.forEach(p => { svg += `<rect class="chart-point" x="${p.x - 3}" y="${p.y - 3}" width="6" height="6" />`; });
  svg += `</svg>`;
  containerEl.innerHTML = svg;
}