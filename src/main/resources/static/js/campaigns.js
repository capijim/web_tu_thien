class CampaignManager {
    constructor() {
        this.currentFilter = 'all';
        this.currentSearch = '';
        this.campaigns = [];
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadCampaigns();
        this.handleDeepLink();
    }

    setupEventListeners() {
        // Filters: name input + category select (new UI)
        const searchInput = document.getElementById('search-name');
        const categorySelect = document.getElementById('filter-category');

        if (searchInput) {
            const onSearch = (e) => {
                this.currentSearch = e.target.value || '';
                this.filterCampaigns();
            };
            searchInput.addEventListener('input', onSearch);
            searchInput.addEventListener('change', onSearch);
        }

        if (categorySelect) {
            categorySelect.addEventListener('change', (e) => {
                this.currentFilter = e.target.value;
                this.filterCampaigns();
            });
        }

        // Modal close buttons
        document.querySelectorAll('.close').forEach(btn => {
            btn.addEventListener('click', (e) => {
                this.hideModal(e.target.closest('.modal'));
            });
        });

        document.getElementById('cancel-donate').addEventListener('click', () => {
            this.hideModal(document.getElementById('donate-modal'));
        });

        // Donate form
        document.getElementById('donate-form').addEventListener('submit', (e) => {
            this.handleDonate(e);
        });

        // Close modal when clicking outside
        window.addEventListener('click', (e) => {
            if (e.target.classList.contains('modal')) {
                this.hideModal(e.target);
            }
        });
    }

    async loadCampaigns() {
        try {
            const response = await fetch('/api/campaigns/active');
            this.campaigns = await response.json();
            this.renderCampaigns();
        } catch (error) {
            console.error('Error loading campaigns:', error);
            this.showMessage('Lỗi tải danh sách khuyến góp', 'error');
        }
    }

    renderCampaigns() {
        const container = document.getElementById('campaigns-list');
        container.innerHTML = '';

        if (this.campaigns.length === 0) {
            container.innerHTML = '<p class="no-data">Chưa có khuyến góp nào</p>';
            return;
        }

        this.campaigns.forEach(campaign => {
            const campaignCard = this.createCampaignCard(campaign);
            // Open detail on card click (except on donate button)
            campaignCard.addEventListener('click', (e) => {
                const isDonate = e.target.closest && e.target.closest('.btn-donate');
                if (isDonate) return;
                window.location.href = `/campaign_detail.html?id=${encodeURIComponent(campaign.id)}`;
            });
            container.appendChild(campaignCard);
        });
    }

    createCampaignCard(campaign) {
        const card = document.createElement('div');
        card.className = 'campaign-card';
        
        const progressPercentage = campaign.currentAmount / campaign.targetAmount * 100;
        const isCompleted = campaign.currentAmount >= campaign.targetAmount;
        
        card.innerHTML = `
            <div class="campaign-image">
                ${campaign.imageUrl ? `<img src="${campaign.imageUrl}" alt="${campaign.title}" />` : '<div class="no-image">Không có hình ảnh</div>'}
            </div>
            <div class="campaign-content">
                <div class="campaign-header">
                    <h3 class="campaign-title">${this.escapeHtml(campaign.title)}</h3>
                    <span class="campaign-category">${this.escapeHtml(campaign.category)}</span>
                </div>
                <p class="campaign-description">${this.escapeHtml(campaign.description)}</p>
                <div class="campaign-progress">
                    <div class="progress-bar">
                        <div class="progress-fill" style="width: ${Math.min(progressPercentage, 100)}%"></div>
                    </div>
                    <div class="progress-text">
                        <span>${Number(campaign.currentAmount).toLocaleString('vi-VN')} VNĐ</span>
                        <span>${Number(campaign.targetAmount).toLocaleString('vi-VN')} VNĐ</span>
                    </div>
                </div>
                <div class="campaign-actions">
                    <button class="btn-donate" data-campaign-id="${campaign.id}">Ủng hộ ngay</button>
                    <span class="campaign-status ${campaign.status}">${this.getStatusText(campaign.status)}</span>
                </div>
            </div>
        `;

        // Add donate button event listener
        const donateBtn = card.querySelector('.btn-donate');
        donateBtn.addEventListener('click', () => {
            this.showDonateModal(campaign);
        });

        return card;
    }

    filterCampaigns() {
        const cards = document.querySelectorAll('.campaign-card');
        const search = (this.currentSearch || '').trim().toLowerCase();
        const categoryFilter = this.currentFilter || 'all';

        cards.forEach(card => {
            const title = card.querySelector('.campaign-title').textContent.toLowerCase();
            const category = card.querySelector('.campaign-category').textContent;

            const matchName = search === '' || title.includes(search);
            const matchCategory = categoryFilter === 'all' || category === categoryFilter;

            card.style.display = matchName && matchCategory ? 'block' : 'none';
        });
    }

    showDonateModal(campaign) {
        const modal = document.getElementById('donate-modal');
        const infoDiv = document.getElementById('donate-campaign-info');
        const campaignIdInput = document.getElementById('donate-campaign-id');
        
        campaignIdInput.value = campaign.id;
        
        infoDiv.innerHTML = `
            <h3>${this.escapeHtml(campaign.title)}</h3>
            <p><strong>Mục tiêu:</strong> ${Number(campaign.targetAmount).toLocaleString('vi-VN')} VNĐ</p>
            <p><strong>Đã ủng hộ:</strong> ${Number(campaign.currentAmount).toLocaleString('vi-VN')} VNĐ</p>
            <p><strong>Còn thiếu:</strong> ${Number(campaign.targetAmount - campaign.currentAmount).toLocaleString('vi-VN')} VNĐ</p>
        `;
        
        modal.style.display = 'block';
    }

    openDetailModal(campaign) {
        const modal = document.getElementById('campaign-detail-modal');
        if (!modal) return;

        const titleEl = document.getElementById('detail-title');
        const catEl = document.getElementById('detail-category');
        const descEl = document.getElementById('detail-description');
        const imgEl = document.getElementById('detail-image');
        const progressEl = document.getElementById('detail-progress');
        const curEl = document.getElementById('detail-current');
        const tgtEl = document.getElementById('detail-target');
        const donateBtn = document.getElementById('detail-donate-btn');

        titleEl.textContent = campaign.title || '';
        catEl.textContent = campaign.category || '';
        descEl.textContent = campaign.description || '';
        imgEl.innerHTML = campaign.imageUrl ? `<img src="${campaign.imageUrl}" alt="${this.escapeHtml(campaign.title)}"/>` : '<div class="no-image">Không có hình ảnh</div>';

        const progressPercentage = campaign.targetAmount ? (campaign.currentAmount / campaign.targetAmount) * 100 : 0;
        progressEl.style.width = `${Math.min(progressPercentage, 100)}%`;
        curEl.textContent = `${Number(campaign.currentAmount).toLocaleString('vi-VN')} VNĐ`;
        tgtEl.textContent = `${Number(campaign.targetAmount).toLocaleString('vi-VN')} VNĐ`;

        donateBtn.onclick = () => this.showDonateModal(campaign);

        modal.style.display = 'block';
    }

    async handleDeepLink() {
        // Support #campaign-<id> or ?campaignId=<id>
        const hash = window.location.hash || '';
        const params = new URLSearchParams(window.location.search);
        let id = null;
        if (hash.startsWith('#campaign-')) {
            id = hash.replace('#campaign-', '');
        } else if (params.has('campaignId')) {
            id = params.get('campaignId');
        }

        if (!id) return;

        try {
            const res = await fetch(`/api/campaigns/${encodeURIComponent(id)}`);
            if (!res.ok) return;
            const campaign = await res.json();
            this.openDetailModal(campaign);
        } catch (_) {}
    }

    hideModal(modal) {
        modal.style.display = 'none';
    }

    

    async handleDonate(e) {
        e.preventDefault();
        
        const campaignId = document.getElementById('donate-campaign-id').value;
        const amount = parseFloat(document.getElementById('donateAmount').value);
        const messageInput = document.getElementById('donateMessage');
        const message = messageInput ? messageInput.value : undefined;

        if (Number.isNaN(amount) || amount <= 0) {
            this.showMessage('Số tiền không hợp lệ', 'error');
            return;
        }

        try {
            const response = await fetch(`/api/campaigns/${campaignId}/donate`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ amount, message })
            });

            if (response.ok) {
                this.showMessage('Ủng hộ thành công!', 'success');
                this.hideModal(document.getElementById('donate-modal'));
                document.getElementById('donate-form').reset();
                this.loadCampaigns();
            } else {
                if (response.status === 401) {
                    this.showMessage('Bạn cần đăng nhập để ủng hộ', 'error');
                    setTimeout(() => {
                        window.location.href = '/login.html';
                    }, 1200);
                } else {
                    const result = await response.json().catch(() => ({}));
                    this.showMessage('Lỗi: ' + (result.error || 'Có lỗi xảy ra'), 'error');
                }
            }
        } catch (error) {
            console.error('Error donating:', error);
            this.showMessage('Lỗi ủng hộ', 'error');
        }
    }

    getStatusText(status) {
        const statusMap = {
            'active': 'Đang hoạt động',
            'completed': 'Hoàn thành',
            'cancelled': 'Đã hủy',
            'expired': 'Hết hạn'
        };
        return statusMap[status] || status;
    }

    escapeHtml(str) {
        return String(str)
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#039;');
    }

    showMessage(message, type) {
        // Create a simple message display
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${type}`;
        messageDiv.textContent = message;
        messageDiv.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 12px 20px;
            border-radius: 4px;
            color: white;
            z-index: 10000;
            ${type === 'success' ? 'background: #4CAF50;' : 'background: #f44336;'}
        `;
        
        document.body.appendChild(messageDiv);
        
        setTimeout(() => {
            document.body.removeChild(messageDiv);
        }, 3000);
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new CampaignManager();
});
