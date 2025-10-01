class CampaignManager {
    constructor() {
        this.currentFilter = 'all';
        this.campaigns = [];
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadCampaigns();
    }

    setupEventListeners() {
        // Filter buttons
        document.querySelectorAll('.filter-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
                e.target.classList.add('active');
                this.currentFilter = e.target.dataset.category;
                this.filterCampaigns();
            });
        });

        // Create campaign button
        document.getElementById('create-campaign-btn').addEventListener('click', () => {
            this.showCreateModal();
        });

        // Modal close buttons
        document.querySelectorAll('.close').forEach(btn => {
            btn.addEventListener('click', (e) => {
                this.hideModal(e.target.closest('.modal'));
            });
        });

        // Cancel buttons
        document.getElementById('cancel-campaign').addEventListener('click', () => {
            this.hideModal(document.getElementById('create-campaign-modal'));
        });

        document.getElementById('cancel-donate').addEventListener('click', () => {
            this.hideModal(document.getElementById('donate-modal'));
        });

        // Campaign form
        document.getElementById('campaign-form').addEventListener('submit', (e) => {
            this.handleCreateCampaign(e);
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
        cards.forEach(card => {
            const category = card.querySelector('.campaign-category').textContent;
            if (this.currentFilter === 'all' || category === this.currentFilter) {
                card.style.display = 'block';
            } else {
                card.style.display = 'none';
            }
        });
    }

    showCreateModal() {
        document.getElementById('create-campaign-modal').style.display = 'block';
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

    hideModal(modal) {
        modal.style.display = 'none';
    }

    async handleCreateCampaign(e) {
        e.preventDefault();
        
        const formData = new FormData(e.target);
        let imageUrl = null;

        try {
            const file = formData.get('imageFile');
            if (file && file.size > 0) {
                const uploadForm = new FormData();
                uploadForm.append('file', file);
                const uploadRes = await fetch('/api/campaigns/upload', {
                    method: 'POST',
                    body: uploadForm
                });
                if (!uploadRes.ok) {
                    const err = await uploadRes.json().catch(() => ({}));
                    throw new Error(err.error || 'Upload ảnh thất bại');
                }
                const { url } = await uploadRes.json();
                imageUrl = url;
            }
        } catch (err) {
            this.showMessage(err.message || 'Lỗi upload ảnh', 'error');
            return;
        }

        const campaignData = {
            title: formData.get('title'),
            description: formData.get('description'),
            targetAmount: parseFloat(formData.get('targetAmount')),
            category: formData.get('category'),
            imageUrl,
            endDate: formData.get('endDate') ? new Date(formData.get('endDate')).toISOString() : null
        };

        try {
            const response = await fetch('/api/campaigns', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(campaignData)
            });

            if (response.ok) {
                this.showMessage('Tạo khuyến góp thành công!', 'success');
                this.hideModal(document.getElementById('create-campaign-modal'));
                e.target.reset();
                this.loadCampaigns();
            } else {
                const result = await response.json();
                this.showMessage('Lỗi: ' + (result.error || 'Có lỗi xảy ra'), 'error');
            }
        } catch (error) {
            console.error('Error creating campaign:', error);
            this.showMessage('Lỗi tạo khuyến góp', 'error');
        }
    }

    async handleDonate(e) {
        e.preventDefault();
        
        const campaignId = document.getElementById('donate-campaign-id').value;
        const amount = parseFloat(document.getElementById('donateAmount').value);

        try {
            const response = await fetch(`/api/campaigns/${campaignId}/donate`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ amount })
            });

            if (response.ok) {
                this.showMessage('Ủng hộ thành công!', 'success');
                this.hideModal(document.getElementById('donate-modal'));
                document.getElementById('donate-form').reset();
                this.loadCampaigns();
            } else {
                const result = await response.json();
                this.showMessage('Lỗi: ' + (result.error || 'Có lỗi xảy ra'), 'error');
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
