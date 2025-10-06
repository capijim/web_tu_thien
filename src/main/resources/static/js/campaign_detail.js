class CampaignDetailPage {
    constructor() {
        this.init();
    }

    async init() {
        const id = this.resolveCampaignIdFromUrl();
        if (!id) {
            this.showError();
            return;
        }

        await this.loadCampaign(id);
    }

    resolveCampaignIdFromUrl() {
        // 1) Try query param ?id=123
        const params = new URLSearchParams(window.location.search);
        let id = params.get('id');
        if (id) return this.onlyDigits(id);

        // 2) Try alternative query ?campaignId=123
        id = params.get('campaignId');
        if (id) return this.onlyDigits(id);

        // 3) Try hash formats: #id=123 or #campaign-123
        const hash = window.location.hash || '';
        if (hash.startsWith('#id=')) {
            return this.onlyDigits(hash.replace('#id=', ''));
        }
        if (hash.startsWith('#campaign-')) {
            return this.onlyDigits(hash.replace('#campaign-', ''));
        }

        // 4) Try path like /campaign_detail.html/123
        const parts = window.location.pathname.split('/').filter(Boolean);
        const last = parts[parts.length - 1];
        if (last && /\d+/.test(last)) return this.onlyDigits(last);

        return null;
    }

    onlyDigits(value) {
        const match = String(value).match(/\d+/);
        return match ? match[0] : null;
    }

    async loadCampaign(id) {
        const loading = document.getElementById('detail-loading');
        const error = document.getElementById('detail-error');
        const content = document.getElementById('detail-content');

        loading.style.display = 'block';
        error.style.display = 'none';
        content.style.display = 'none';

        try {
            const res = await fetch(`/api/campaigns/${encodeURIComponent(id)}`);
            if (!res.ok) {
                const msg = await res.text().catch(() => '');
                console.error('Failed to load campaign', { id, status: res.status, body: msg });
                throw new Error(`status ${res.status}`);
            }
            const campaign = await res.json();
            this.renderCampaign(campaign);
            loading.style.display = 'none';
            content.style.display = 'block';
        } catch (err) {
            console.error('Error fetching campaign detail:', err);
            loading.style.display = 'none';
            this.showError(`Không tìm thấy chiến dịch (id=${id}).`);
        }
    }

    renderCampaign(campaign) {
        const titleEl = document.getElementById('detail-title');
        const catEl = document.getElementById('detail-category');
        const statusEl = document.getElementById('detail-status');
        const descEl = document.getElementById('detail-description');
        const imgEl = document.getElementById('detail-image');
        const progressEl = document.getElementById('detail-progress');
        const curEl = document.getElementById('detail-current');
        const tgtEl = document.getElementById('detail-target');
        const donateBtn = document.getElementById('detail-donate');

        titleEl.textContent = this.escapeHtml(campaign.title || '');
        catEl.textContent = this.escapeHtml(campaign.category || '');
        statusEl.textContent = this.getStatusText(campaign.status);
        statusEl.className = `campaign-status ${campaign.status}`;
        descEl.textContent = campaign.description || '';
        imgEl.innerHTML = campaign.imageUrl ? `<img src="${campaign.imageUrl}" alt="${this.escapeHtml(campaign.title)}"/>` : '<div class="no-image">Không có hình ảnh</div>';

        const progressPercentage = campaign.targetAmount ? (campaign.currentAmount / campaign.targetAmount) * 100 : 0;
        progressEl.style.width = `${Math.min(progressPercentage, 100)}%`;
        curEl.textContent = `${Number(campaign.currentAmount).toLocaleString('vi-VN')} VNĐ`;
        tgtEl.textContent = `${Number(campaign.targetAmount).toLocaleString('vi-VN')} VNĐ`;

        donateBtn.onclick = () => {
            window.location.href = `/campaigns.html#campaign-${campaign.id}`;
        };
    }

    showError(message) {
        const error = document.getElementById('detail-error');
        if (message) error.textContent = message;
        error.style.display = 'block';
    }

    escapeHtml(str) {
        return String(str)
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#039;');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new CampaignDetailPage();
});


