// Index page JavaScript for displaying featured campaigns

class IndexManager {
    constructor() {
        this.init();
    }

    async init() {
        await this.loadFeaturedCampaigns();
    }

    async loadFeaturedCampaigns() {
        try {
            const response = await fetch('/api/campaigns/with-stats');
            if (!response.ok) {
                throw new Error('Failed to fetch campaigns');
            }

            const campaigns = await response.json();
            
            // Filter active campaigns and take first 3 for featured section
            const activeCampaigns = campaigns
                .filter(campaign => campaign.status === 'active')
                .slice(0, 3);

            this.displayFeaturedCampaigns(activeCampaigns);
        } catch (error) {
            console.error('Error loading campaigns:', error);
            this.showError();
        }
    }

    displayFeaturedCampaigns(campaigns) {
        const container = document.getElementById('featured-campaigns');
        container.innerHTML = ''; // Clear existing content
        
        if (campaigns.length === 0) {
            container.innerHTML = `
                <div class="no-campaigns">
                    <p>Hiện tại chưa có chiến dịch nào đang diễn ra.</p>
                    <a href="/campaigns.html" class="cta">Xem tất cả chiến dịch</a>
                </div>
            `;
            return;
        }

        campaigns.forEach(campaign => {
            const campaignCard = this.createCampaignCard(campaign);
            // Navigate to campaign detail when clicking card (except donate button)
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
        
        const progressPercentage = this.calculateProgress(campaign.currentAmount, campaign.targetAmount);
        const isCompleted = campaign.currentAmount >= campaign.targetAmount;
        
        card.innerHTML = `
            <div class="campaign-image">
                ${campaign.imageUrl ? `<img src="${campaign.imageUrl}" alt="${this.escapeHtml(campaign.title)}" />` : '<div class="no-image">Không có hình ảnh</div>'}
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
                    <div class="progress-stats">
                        <span class="donors-count">Lượt quyên góp: <strong>${campaign.donationCount || 0}</strong></span>
                        <span class="progress-percent">Đạt được: <strong>${progressPercentage}%</strong></span>
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
            this.handleDonate(campaign.id);
        });

        return card;
    }


    handleDonate(campaignId) {
        // Check if user is logged in
        const userId = sessionStorage.getItem('userId');
        if (!userId) {
            // Redirect to login page
            window.location.href = '/login.html?redirect=' + encodeURIComponent(window.location.href);
            return;
        }

        // Redirect to campaigns page with specific campaign
        window.location.href = `/campaigns.html#campaign-${campaignId}`;
    }

    calculateProgress(current, target) {
        if (!target || target === 0) return 0;
        const percentage = (current / target) * 100;
        return Math.min(Math.round(percentage), 100);
    }

    calculateDaysLeft(endDate) {
        if (!endDate) return '∞';
        
        const end = new Date(endDate);
        const now = new Date();
        const diffTime = end - now;
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        
        if (diffDays < 0) return 'Đã kết thúc';
        if (diffDays === 0) return 'Hôm nay';
        if (diffDays === 1) return '1 ngày';
        return `${diffDays} ngày`;
    }

    formatCurrency(amount) {
        if (typeof amount !== 'number') return '0đ';
        
        if (amount >= 1000000) {
            return (amount / 1000000).toFixed(1).replace('.0', '') + 'M đ';
        } else if (amount >= 1000) {
            return (amount / 1000).toFixed(0) + 'K đ';
        }
        return amount.toLocaleString('vi-VN') + 'đ';
    }

    escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#039;');
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

    showError() {
        const container = document.getElementById('featured-campaigns');
        container.innerHTML = `
            <div class="error-campaigns">
                <i class="fas fa-exclamation-triangle"></i>
                <p>Không thể tải danh sách chiến dịch. Vui lòng thử lại sau.</p>
                <button onclick="location.reload()" class="btn-retry">Thử lại</button>
            </div>
        `;
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new IndexManager();
});
