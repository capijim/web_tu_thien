class CampaignDetailPage {
    constructor() {
        this.init();
    }

    async init() {
        const params = new URLSearchParams(window.location.search);
        const id = params.get('id');
        if (!id) {
            this.showError();
            return;
        }

        await this.loadCampaign(id);
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
            if (!res.ok) throw new Error('Not Found');
            const campaign = await res.json();
            this.renderCampaign(campaign);
            loading.style.display = 'none';
            content.style.display = 'block';
        } catch (_) {
            loading.style.display = 'none';
            this.showError();
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

    showError() {
        const error = document.getElementById('detail-error');
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


