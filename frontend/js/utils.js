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
// The first entry is always treated as "Personal" (colored orange); every
// entry after that is a group (colored green) - this is a deliberate,
// meaningful color code (yours vs. shared), not decoration. Each bar gets
// its actual KES value and share-of-total percentage printed above it,
// there's a legend, and a running total underneath - a bar chart with no
// numbers on it isn't actually informative, just decorative.
function renderBlockBarChart(containerEl, data, options = {}) {
  if (!data || data.length === 0) {
    containerEl.innerHTML = '<div class="chart-empty">NO_DATA_YET</div>';
    return;
  }
  const width = 480, height = 210, padding = 28;
  const barAreaWidth = width - padding * 2;
  const barGap = 18;
  const barWidth = Math.max(24, (barAreaWidth / data.length) - barGap);
  const maxVal = Math.max(...data.map(d => d.value), 1);
  const total = data.reduce((sum, d) => sum + d.value, 0) || 1;
  const blockSize = 8, blockGap = 2;
  const chartFloor = height - 46; // leaves room for axis label + legend below

  let svg = `<svg viewBox="0 0 ${width} ${height}" xmlns="http://www.w3.org/2000/svg">`;
  data.forEach((d, i) => {
    const isPersonal = i === 0;
    const colorClass = isPersonal ? 'chart-bar-personal' : 'chart-bar-group';
    const x = padding + i * (barWidth + barGap);
    const barHeightPx = Math.max(4, (d.value / maxVal) * (chartFloor - 60));
    const numBlocks = Math.max(1, Math.floor(barHeightPx / (blockSize + blockGap)));
    const pct = Math.round((d.value / total) * 100);

    for (let b = 0; b < numBlocks; b++) {
      const y = chartFloor - (b + 1) * (blockSize + blockGap);
      // Staggered animation delay: blocks build up from the bottom,
      // bars build up left-to-right - this is what makes it read as
      // "alive" on load rather than just appearing fully formed.
      const delay = (i * 0.08 + b * 0.015).toFixed(3);
      svg += `<rect class="chart-bar-block ${colorClass}" x="${x}" y="${y}" width="${barWidth}" height="${blockSize}" style="animation-delay:${delay}s">`;
      svg += `<title>${escapeHtml(d.label)}: KES ${formatCurrency(d.value)} (${pct}%)</title>`;
      svg += `</rect>`;
    }

    // Value + percentage directly above the bar - the actual point of
    // a chart like this is answering "how much, and how much of the
    // whole" at a glance, without hovering or guessing from bar height.
    const topY = chartFloor - numBlocks * (blockSize + blockGap) - 8;
    svg += `<text class="chart-value-label" x="${x + barWidth / 2}" y="${Math.max(12, topY)}" text-anchor="middle">KES ${formatCurrency(d.value)}</text>`;
    svg += `<text class="chart-pct-label" x="${x + barWidth / 2}" y="${Math.max(24, topY + 12)}" text-anchor="middle">${pct}%</text>`;
    svg += `<text class="chart-axis-label" x="${x + barWidth / 2}" y="${chartFloor + 16}" text-anchor="middle">${escapeHtml(truncate(d.label, 10))}</text>`;
  });
  svg += `</svg>`;

  const legend = `
    <div class="chart-legend">
      <span class="legend-item"><span class="legend-swatch legend-personal"></span>Personal</span>
      <span class="legend-item"><span class="legend-swatch legend-group"></span>Group Savings</span>
      <span class="legend-total">TOTAL: KES ${formatCurrency(total)}</span>
    </div>`;

  containerEl.innerHTML = svg + legend;
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