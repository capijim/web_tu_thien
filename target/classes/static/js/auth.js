// Authentication JavaScript
class AuthManager {
    constructor() {
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.checkAuthStatus();
        // Mobile menu and scroll behavior are now handled by headbar.js
    }

    setupEventListeners() {
        // Login form
        const loginForm = document.getElementById('login-form');
        if (loginForm) {
            loginForm.addEventListener('submit', (e) => this.handleLogin(e));
        }

        // Register form
        const registerForm = document.getElementById('register-form');
        if (registerForm) {
            registerForm.addEventListener('submit', (e) => this.handleRegister(e));
        }

        // Setup logout button listener with delegation
        document.addEventListener('click', (e) => {
            if (e.target && e.target.id === 'logout-btn') {
                e.preventDefault();
                this.handleLogout(e);
            }
            if (e.target && e.target.id === 'change-password-link') {
                e.preventDefault();
                window.location.href = '/change_password.html';
            }
        });
    }

    async handleLogin(e) {
        e.preventDefault();
        
        const formData = new FormData(e.target);
        const loginData = {
            email: formData.get('email'),
            password: formData.get('password')
        };

        try {
            console.log('Sending login request:', loginData);
            
            const response = await fetch('/api/users/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(loginData)
            });

            console.log('Response status:', response.status);
            const result = await response.json();
            console.log('Response data:', result);

            if (response.ok) {
                this.showMessage('success', 'Đăng nhập thành công!');
                setTimeout(() => {
                    window.location.href = '/';
                }, 1500);
            } else {
                this.showMessage('error', result.error || 'Đăng nhập thất bại');
            }
        } catch (error) {
            console.error('Login error:', error);
            this.showMessage('error', 'Lỗi kết nối. Vui lòng thử lại.');
        }
    }

    async handleRegister(e) {
        e.preventDefault();
        
        const formData = new FormData(e.target);
        const password = formData.get('password');
        const confirmPassword = formData.get('confirmPassword');

        // Validate password confirmation
        if (password !== confirmPassword) {
            this.showMessage('error', 'Mật khẩu xác nhận không khớp');
            return;
        }

        const registerData = {
            name: formData.get('name'),
            email: formData.get('email'),
            password: password
        };

        try {
            const response = await fetch('/api/users', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(registerData)
            });

            const result = await response.json();

            if (response.ok) {
                this.showMessage('success', 'Đăng ký thành công! Bạn có thể đăng nhập ngay.');
                e.target.reset();
            } else {
                this.showMessage('error', result.error || 'Đăng ký thất bại');
            }
        } catch (error) {
            this.showMessage('error', 'Lỗi kết nối. Vui lòng thử lại.');
        }
    }

    async handleLogout(e) {
        e.preventDefault();
        console.log('Logout button clicked');
        
        try {
            console.log('Sending logout request...');
            const response = await fetch('/api/users/logout', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            console.log('Logout response status:', response.status);
            const result = await response.json();
            console.log('Logout response data:', result);

            if (response.ok) {
                console.log('Logout successful');
                this.showMessage('success', 'Đăng xuất thành công!');
                // Update navigation immediately
                this.updateNavigation(false);
                setTimeout(() => {
                    window.location.href = '/';
                }, 1500);
            } else {
                console.log('Logout failed:', result);
                this.showMessage('error', result.error || 'Lỗi đăng xuất');
            }
        } catch (error) {
            console.error('Logout error:', error);
            this.showMessage('error', 'Lỗi kết nối. Vui lòng thử lại.');
        }
    }

    async checkAuthStatus() {
        try {
            const response = await fetch('/api/users/check-auth');
            const result = await response.json();

            if (response.ok && result.isAuthenticated) {
                this.updateNavigation(true, result.user);
            } else {
                this.updateNavigation(false);
            }
        } catch (error) {
            console.error('Error checking auth status:', error);
            this.updateNavigation(false);
        }
    }

    // Wait for headbar to be loaded before updating navigation
    updateNavigation(isAuthenticated, user = null) {
        // Wait a bit for headbar to load
        setTimeout(() => {
            const authSection = document.querySelector('.auth-section');
            if (!authSection) {
                // Retry after a short delay
                setTimeout(() => this.updateNavigation(isAuthenticated, user), 200);
                return;
            }

            // Clear existing auth content
            authSection.innerHTML = '';

            if (isAuthenticated && user) {
                // Add user menu
                const userMenu = document.createElement('div');
                userMenu.className = 'user-menu';
                userMenu.innerHTML = `
                    <span class="user-name">Xin chào, ${user.name}</span>
                    <a href="#" id="change-password-link" class="auth-link">Đổi mật khẩu</a>
                    <a href="#" id="logout-btn" class="logout-link">Đăng xuất</a>
                `;
                authSection.appendChild(userMenu);
                
                // Setup logout button listener for this specific button
                const logoutBtn = document.getElementById('logout-btn');
                if (logoutBtn) {
                    logoutBtn.addEventListener('click', (e) => this.handleLogout(e));
                }
            } else {
                // Add login/register links
                const loginLink = document.createElement('a');
                loginLink.className = 'auth-link';
                loginLink.href = '/login.html';
                loginLink.textContent = 'Đăng nhập';
                authSection.appendChild(loginLink);

                const registerLink = document.createElement('a');
                registerLink.className = 'auth-link primary';
                registerLink.href = '/register.html';
                registerLink.textContent = 'Đăng ký';
                authSection.appendChild(registerLink);
            }
        }, 100);
    }


    showMessage(type, message) {
        const errorDiv = document.getElementById('error-message');
        const successDiv = document.getElementById('success-message');
        
        if (type === 'error' && errorDiv) {
            errorDiv.textContent = message;
            errorDiv.style.display = 'block';
            if (successDiv) successDiv.style.display = 'none';
        } else if (type === 'success' && successDiv) {
            successDiv.textContent = message;
            successDiv.style.display = 'block';
            if (errorDiv) errorDiv.style.display = 'none';
        }
    }

    async getCurrentUser() {
        try {
            const response = await fetch('/api/users/me');
            const result = await response.json();
            
            if (response.ok) {
                return result;
            }
            return null;
        } catch (error) {
            console.error('Error getting current user:', error);
            return null;
        }
    }

}

// Initialize auth manager when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new AuthManager();
});
