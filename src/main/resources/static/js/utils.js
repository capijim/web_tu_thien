// Lightweight generic utilities for formatting and simple metrics across pages
// Expose under window.utils for optional usage from inline scripts

(function () {
  function toNumber(value) {
    if (value == null || value === '') return 0;
    const n = Number(value);
    return Number.isFinite(n) ? n : 0;
  }

  function clamp(num, min, max) {
    return Math.min(Math.max(num, min), max);
  }

  function formatNumber(value) {
    const n = toNumber(value);
    return new Intl.NumberFormat('vi-VN').format(n);
  }

  function formatCurrencyVND(value) {
    const n = toNumber(value);
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      maximumFractionDigits: 0
    }).format(n);
  }

  function formatPercent(value, fractionDigits = 0) {
    const n = clamp(toNumber(value), 0, 100);
    return `${n.toFixed(fractionDigits)}%`;
  }

  function daysRemaining(endIsoString) {
    if (!endIsoString) return null;
    try {
      const end = new Date(endIsoString).getTime();
      const now = Date.now();
      const diff = Math.max(0, end - now);
      return Math.ceil(diff / (1000 * 60 * 60 * 24));
    } catch (_) {
      return null;
    }
  }

  function setProgress(element, percent) {
    if (!element) return;
    const p = clamp(toNumber(percent), 0, 100);
    element.style.width = `${p}%`;
    element.setAttribute('aria-valuenow', String(p));
  }

  // Optional auto-format hooks via data attributes
  // data-format="currency|number|percent", data-value, data-fraction
  function autoFormat() {
    document.querySelectorAll('[data-format]')
      .forEach(function (el) {
        const kind = el.getAttribute('data-format');
        const value = el.getAttribute('data-value') ?? el.textContent;
        const fraction = Number(el.getAttribute('data-fraction') || 0);
        if (kind === 'currency') {
          el.textContent = formatCurrencyVND(value);
        } else if (kind === 'number') {
          el.textContent = formatNumber(value);
        } else if (kind === 'percent') {
          el.textContent = formatPercent(value, fraction);
        }
      });

    document.querySelectorAll('[data-progress]')
      .forEach(function (el) {
        const value = el.getAttribute('data-progress');
        setProgress(el, value);
      });
  }

  window.utils = {
    toNumber,
    clamp,
    formatNumber,
    formatCurrencyVND,
    formatPercent,
    daysRemaining,
    setProgress,
    autoFormat
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', autoFormat);
  } else {
    autoFormat();
  }
})();


