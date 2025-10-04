// Admin Login JavaScript

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('admin-login-form');
    const emailInput = document.getElementById('admin-email');
    const passwordInput = document.getElementById('admin-password');
    const loginBtn = document.getElementById('login-btn');
    const btnText = document.querySelector('.btn-text');
    const loading = document.querySelector('.loading');
    const errorMessage = document.getElementById('error-message');

    // Check if already logged in as admin
    const adminSession = localStorage.getItem('adminSession');
    if (adminSession) {
        try {
            const session = JSON.parse(adminSession);
            if (session.isAdmin && session.expires > Date.now()) {
                window.location.href = '/admin.html';
                return;
            }
        } catch (e) {
            localStorage.removeItem('adminSession');
        }
    }

    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const email = emailInput.value.trim();
        const password = passwordInput.value.trim();
        
        if (!email || !password) {
            showError('Vui lòng nhập đầy đủ email và mật khẩu');
            return;
        }

        // Show loading state
        loginBtn.disabled = true;
        btnText.style.display = 'none';
        loading.style.display = 'inline';
        hideError();

        try {
            // For demo purposes, use hardcoded admin credentials
            // In production, this should be a proper API call
            if (email === 'admin@webtuthien.com' && password === 'admin123') {
                // Create admin session
                const adminSession = {
                    isAdmin: true,
                    email: email,
                    expires: Date.now() + (24 * 60 * 60 * 1000) // 24 hours
                };
                
                localStorage.setItem('adminSession', JSON.stringify(adminSession));
                
                // Redirect to admin dashboard
                window.location.href = '/admin.html';
            } else {
                showError('Email hoặc mật khẩu không đúng');
            }
        } catch (error) {
            console.error('Login error:', error);
            showError('Có lỗi xảy ra khi đăng nhập. Vui lòng thử lại.');
        } finally {
            // Reset loading state
            loginBtn.disabled = false;
            btnText.style.display = 'inline';
            loading.style.display = 'none';
        }
    });

    function showError(message) {
        errorMessage.textContent = message;
        errorMessage.style.display = 'block';
    }

    function hideError() {
        errorMessage.style.display = 'none';
    }

    // Auto-focus email input
    emailInput.focus();
});
