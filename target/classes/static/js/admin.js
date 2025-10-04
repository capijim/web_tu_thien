// Admin Dashboard JavaScript

const API_BASE_URL = '/api/admin';

// Initialize admin dashboard
document.addEventListener('DOMContentLoaded', function() {
    // Check authentication first
    if (!checkAdminAuth()) {
        return; // Will redirect to login
    }
    
    // Update admin user display
    updateAdminUserDisplay();
    
    initializeAdmin();
    loadDashboardStats();
});

function checkAdminAuth() {
    const adminSession = localStorage.getItem('adminSession');
    
    if (!adminSession) {
        redirectToLogin();
        return false;
    }
    
    try {
        const session = JSON.parse(adminSession);
        if (!session.isAdmin || session.expires <= Date.now()) {
            localStorage.removeItem('adminSession');
            redirectToLogin();
            return false;
        }
        return true;
    } catch (e) {
        localStorage.removeItem('adminSession');
        redirectToLogin();
        return false;
    }
}

function redirectToLogin() {
    window.location.href = '/admin-login.html';
}

function updateAdminUserDisplay() {
    try {
        const adminSession = localStorage.getItem('adminSession');
        if (adminSession) {
            const session = JSON.parse(adminSession);
            const adminUserSpan = document.querySelector('.admin-user span');
            if (adminUserSpan && session.email) {
                adminUserSpan.textContent = session.email;
            }
        }
    } catch (e) {
        console.error('Error updating admin user display:', e);
    }
}

function initializeAdmin() {
    // Setup sidebar navigation
    const menuItems = document.querySelectorAll('.menu-item');
    menuItems.forEach(item => {
        item.addEventListener('click', function() {
            const section = this.dataset.section;
            switchSection(section);
        });
    });

    // Setup modal
    const modal = document.getElementById('status-modal');
    const closeBtn = document.querySelector('.close');
    
    closeBtn.addEventListener('click', closeModal);
    window.addEventListener('click', function(event) {
        if (event.target === modal) {
            closeModal();
        }
    });

    // Setup status form
    document.getElementById('status-form').addEventListener('submit', updateCampaignStatus);
}

function switchSection(sectionName) {
    // Update active menu item
    document.querySelectorAll('.menu-item').forEach(item => {
        item.classList.remove('active');
    });
    document.querySelector(`[data-section="${sectionName}"]`).classList.add('active');

    // Update active section
    document.querySelectorAll('.admin-section').forEach(section => {
        section.classList.remove('active');
    });
    document.getElementById(`${sectionName}-section`).classList.add('active');

    // Update page title
    const titles = {
        dashboard: 'Dashboard',
        users: 'Quản lý Users',
        campaigns: 'Quản lý Campaigns',
        donations: 'Quản lý Donations'
    };
    document.getElementById('page-title').textContent = titles[sectionName];

    // Load data for the section
    switch(sectionName) {
        case 'dashboard':
            loadDashboardStats();
            break;
        case 'users':
            loadUsers();
            break;
        case 'campaigns':
            loadCampaigns();
            break;
        case 'donations':
            loadDonations();
            break;
    }
}

// Dashboard functions
async function loadDashboardStats() {
    try {
        const response = await fetch(`${API_BASE_URL}/dashboard/stats`);
        if (response.ok) {
            const stats = await response.json();
            updateDashboardStats(stats);
        } else {
            console.error('Failed to load dashboard stats');
            showDefaultStats();
        }
    } catch (error) {
        console.error('Error loading dashboard stats:', error);
        showDefaultStats();
    }
}

function updateDashboardStats(stats) {
    document.getElementById('total-users').textContent = stats.totalUsers || 0;
    document.getElementById('total-campaigns').textContent = stats.totalCampaigns || 0;
    document.getElementById('active-campaigns').textContent = stats.activeCampaigns || 0;
    document.getElementById('total-donations').textContent = stats.totalDonations || 0;
    
    document.getElementById('total-target').textContent = formatCurrency(stats.totalTargetAmount || 0);
    document.getElementById('total-raised').textContent = formatCurrency(stats.totalRaisedAmount || 0);
    document.getElementById('total-donation-amount').textContent = formatCurrency(stats.totalDonationAmount || 0);
}

function showDefaultStats() {
    document.getElementById('total-users').textContent = '0';
    document.getElementById('total-campaigns').textContent = '0';
    document.getElementById('active-campaigns').textContent = '0';
    document.getElementById('total-donations').textContent = '0';
    document.getElementById('total-target').textContent = '0 VNĐ';
    document.getElementById('total-raised').textContent = '0 VNĐ';
    document.getElementById('total-donation-amount').textContent = '0 VNĐ';
}

// Users management
async function loadUsers() {
    const tbody = document.getElementById('users-table-body');
    tbody.innerHTML = '<tr><td colspan="5" class="loading"><i class="fas fa-spinner"></i> Đang tải...</td></tr>';

    try {
        const response = await fetch(`${API_BASE_URL}/users`);
        if (response.ok) {
            const users = await response.json();
            displayUsers(users);
        } else {
            tbody.innerHTML = '<tr><td colspan="5" class="empty-state">Không thể tải dữ liệu users</td></tr>';
        }
    } catch (error) {
        console.error('Error loading users:', error);
        tbody.innerHTML = '<tr><td colspan="5" class="empty-state">Lỗi khi tải dữ liệu users</td></tr>';
    }
}

function displayUsers(users) {
    const tbody = document.getElementById('users-table-body');
    
    if (users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-state">Không có users nào</td></tr>';
        return;
    }

    tbody.innerHTML = users.map(user => `
        <tr>
            <td>${user.id}</td>
            <td>${user.name}</td>
            <td>${user.email}</td>
            <td>${formatDate(user.createdAt)}</td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-danger" onclick="deleteUser(${user.id})">
                        <i class="fas fa-trash"></i> Xóa
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

async function deleteUser(userId) {
    if (!confirm('Bạn có chắc chắn muốn xóa user này?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/users/${userId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            showNotification('Xóa user thành công', 'success');
            loadUsers();
        } else {
            showNotification('Không thể xóa user', 'error');
        }
    } catch (error) {
        console.error('Error deleting user:', error);
        showNotification('Lỗi khi xóa user', 'error');
    }
}

// Campaigns management
async function loadCampaigns() {
    const tbody = document.getElementById('campaigns-table-body');
    tbody.innerHTML = '<tr><td colspan="7" class="loading"><i class="fas fa-spinner"></i> Đang tải...</td></tr>';

    try {
        const response = await fetch(`${API_BASE_URL}/campaigns`);
        if (response.ok) {
            const campaigns = await response.json();
            displayCampaigns(campaigns);
        } else {
            tbody.innerHTML = '<tr><td colspan="7" class="empty-state">Không thể tải dữ liệu campaigns</td></tr>';
        }
    } catch (error) {
        console.error('Error loading campaigns:', error);
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state">Lỗi khi tải dữ liệu campaigns</td></tr>';
    }
}

function displayCampaigns(campaigns) {
    const tbody = document.getElementById('campaigns-table-body');
    
    if (campaigns.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state">Không có campaigns nào</td></tr>';
        return;
    }

    tbody.innerHTML = campaigns.map(campaign => `
        <tr>
            <td>${campaign.id}</td>
            <td>${campaign.title}</td>
            <td>${campaign.category}</td>
            <td>${formatCurrency(campaign.targetAmount)}</td>
            <td>${formatCurrency(campaign.currentAmount)}</td>
            <td>
                <span class="status-badge status-${campaign.status}">
                    ${getStatusText(campaign.status)}
                </span>
            </td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-warning" onclick="openStatusModal(${campaign.id}, '${campaign.status}')">
                        <i class="fas fa-edit"></i> Sửa
                    </button>
                    <button class="btn btn-danger" onclick="deleteCampaign(${campaign.id})">
                        <i class="fas fa-trash"></i> Xóa
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function openStatusModal(campaignId, currentStatus) {
    document.getElementById('campaign-id').value = campaignId;
    document.getElementById('campaign-status').value = currentStatus;
    document.getElementById('status-modal').style.display = 'block';
}

function closeModal() {
    document.getElementById('status-modal').style.display = 'none';
}

async function updateCampaignStatus(event) {
    event.preventDefault();
    
    const campaignId = document.getElementById('campaign-id').value;
    const status = document.getElementById('campaign-status').value;

    try {
        const response = await fetch(`${API_BASE_URL}/campaigns/${campaignId}/status`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ status })
        });

        if (response.ok) {
            showNotification('Cập nhật trạng thái thành công', 'success');
            closeModal();
            loadCampaigns();
        } else {
            showNotification('Không thể cập nhật trạng thái', 'error');
        }
    } catch (error) {
        console.error('Error updating campaign status:', error);
        showNotification('Lỗi khi cập nhật trạng thái', 'error');
    }
}

async function deleteCampaign(campaignId) {
    if (!confirm('Bạn có chắc chắn muốn xóa campaign này?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/campaigns/${campaignId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            showNotification('Xóa campaign thành công', 'success');
            loadCampaigns();
        } else {
            showNotification('Không thể xóa campaign', 'error');
        }
    } catch (error) {
        console.error('Error deleting campaign:', error);
        showNotification('Lỗi khi xóa campaign', 'error');
    }
}

// Donations management
async function loadDonations() {
    const tbody = document.getElementById('donations-table-body');
    tbody.innerHTML = '<tr><td colspan="7" class="loading"><i class="fas fa-spinner"></i> Đang tải...</td></tr>';

    try {
        const response = await fetch(`${API_BASE_URL}/donations`);
        if (response.ok) {
            const donations = await response.json();
            displayDonations(donations);
        } else {
            console.error('Failed to load donations, status:', response.status);
            tbody.innerHTML = '<tr><td colspan="7" class="empty-state">Không thể tải dữ liệu donations</td></tr>';
        }
    } catch (error) {
        console.error('Error loading donations:', error);
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state">Lỗi khi tải dữ liệu donations</td></tr>';
    }
}

function displayDonations(donations) {
    const tbody = document.getElementById('donations-table-body');
    
    if (donations.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state">Không có donations nào</td></tr>';
        return;
    }

    tbody.innerHTML = donations.map(donation => `
        <tr>
            <td>${donation.id}</td>
            <td>${donation.campaignId || 'N/A'}</td>
            <td>${donation.donorName}</td>
            <td>${formatCurrency(donation.amount)}</td>
            <td>${donation.message || 'Không có tin nhắn'}</td>
            <td>${formatDate(donation.createdAt)}</td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-danger" onclick="deleteDonation(${donation.id})">
                        <i class="fas fa-trash"></i> Xóa
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

async function deleteDonation(donationId) {
    if (!confirm('Bạn có chắc chắn muốn xóa donation này?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/donations/${donationId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            showNotification('Xóa donation thành công', 'success');
            loadDonations();
        } else {
            showNotification('Không thể xóa donation', 'error');
        }
    } catch (error) {
        console.error('Error deleting donation:', error);
        showNotification('Lỗi khi xóa donation', 'error');
    }
}


// Utility functions
function formatCurrency(amount) {
    if (typeof amount === 'number') {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount);
    }
    return '0 VNĐ';
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    
    try {
        const date = new Date(dateString);
        return new Intl.DateTimeFormat('vi-VN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        }).format(date);
    } catch (error) {
        return 'N/A';
    }
}

function getStatusText(status) {
    const statusMap = {
        'active': 'Đang hoạt động',
        'completed': 'Hoàn thành',
        'cancelled': 'Đã hủy',
        'expired': 'Hết hạn'
    };
    return statusMap[status] || status;
}

function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
        <span>${message}</span>
        <button class="notification-close" onclick="this.parentElement.remove()">
            <i class="fas fa-times"></i>
        </button>
    `;

    // Add notification styles if not already added
    if (!document.getElementById('notification-styles')) {
        const styles = document.createElement('style');
        styles.id = 'notification-styles';
        styles.textContent = `
            .notification {
                position: fixed;
                top: 20px;
                right: 20px;
                background: white;
                padding: 1rem 1.5rem;
                border-radius: 8px;
                box-shadow: 0 4px 20px rgba(0,0,0,0.15);
                display: flex;
                align-items: center;
                gap: 0.75rem;
                z-index: 3000;
                min-width: 300px;
                animation: slideIn 0.3s ease;
            }
            
            .notification-success {
                border-left: 4px solid #48bb78;
                color: #22543d;
            }
            
            .notification-error {
                border-left: 4px solid #f56565;
                color: #742a2a;
            }
            
            .notification-info {
                border-left: 4px solid #667eea;
                color: #2d3748;
            }
            
            .notification-close {
                background: none;
                border: none;
                cursor: pointer;
                color: #a0aec0;
                margin-left: auto;
            }
            
            .notification-close:hover {
                color: #2d3748;
            }
            
            @keyframes slideIn {
                from {
                    transform: translateX(100%);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }
        `;
        document.head.appendChild(styles);
    }

    // Add to page
    document.body.appendChild(notification);

    // Auto remove after 5 seconds
    setTimeout(() => {
        if (notification.parentElement) {
            notification.remove();
        }
    }, 5000);
}

function logout() {
    if (confirm('Bạn có chắc chắn muốn đăng xuất?')) {
        // Clear any stored session data
        localStorage.removeItem('adminSession');
        sessionStorage.clear();
        
        // Redirect to admin login page
        window.location.href = '/admin-login.html';
    }
}
