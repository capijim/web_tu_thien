async function fetchDonations() {
  const res = await fetch('/api/donations');
  const data = await res.json();
  const tbody = document.getElementById('donations-body');
  tbody.innerHTML = '';
  data.forEach(d => {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${escapeHtml(d.donorName)}</td>` +
      `<td>${Number(d.amount).toLocaleString('vi-VN')}</td>` +
      `<td>${escapeHtml(d.message || '')}</td>` +
      `<td>${new Date(d.createdAt).toLocaleString('vi-VN')}</td>`;
    tbody.appendChild(tr);
  });
}

function escapeHtml(str) {
  return String(str)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');
}

document.getElementById('donation-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const donorName = document.getElementById('donorName').value.trim();
  const amount = document.getElementById('amount').value;
  const message = document.getElementById('message').value;
  const res = await fetch('/api/donations', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ donorName, amount, message })
  });
  if (res.ok) {
    document.getElementById('donation-form').reset();
    fetchDonations();
  } else {
    const text = await res.text();
    alert('Lỗi: ' + text);
  }
});

fetchDonations();


