// Authentication JavaScript
class AuthManager {
    constructor() {
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.checkAuthStatus();
        this.setupMobileMenu();
        this.setupScrollBehavior();
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

        // Logout button
        const logoutBtn = document.getElementById('logout-btn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', (e) => this.handleLogout(e));
        }
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
        
        try {
            const response = await fetch('/api/users/logout', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (response.ok) {
                this.showMessage('success', 'Đăng xuất thành công!');
                setTimeout(() => {
                    window.location.href = '/';
                }, 1500);
            } else {
                this.showMessage('error', 'Lỗi đăng xuất');
            }
        } catch (error) {
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

    updateNavigation(isAuthenticated, user = null) {
        const authSection = document.querySelector('.auth-section');
        if (!authSection) return;

        // Clear existing auth content
        authSection.innerHTML = '';

        if (isAuthenticated && user) {
            // Add user menu
            const userMenu = document.createElement('div');
            userMenu.className = 'user-menu';
            userMenu.innerHTML = `
                <span class="user-name">Xin chào, ${user.name}</span>
                <a href="#" id="logout-btn" class="logout-link">Đăng xuất</a>
            `;
            authSection.appendChild(userMenu);
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

    setupMobileMenu() {
        const mobileToggle = document.getElementById('mobile-menu-toggle');
        const nav = document.querySelector('.headbar-nav');
        
        if (mobileToggle && nav) {
            mobileToggle.addEventListener('click', () => {
                mobileToggle.classList.toggle('active');
                nav.classList.toggle('active');
            });

            // Close mobile menu when clicking on nav links
            const navLinks = nav.querySelectorAll('.nav-link');
            navLinks.forEach(link => {
                link.addEventListener('click', () => {
                    mobileToggle.classList.remove('active');
                    nav.classList.remove('active');
                });
            });

            // Close mobile menu when clicking outside
            document.addEventListener('click', (e) => {
                if (!mobileToggle.contains(e.target) && !nav.contains(e.target)) {
                    mobileToggle.classList.remove('active');
                    nav.classList.remove('active');
                }
            });
        }
    }

    setupScrollBehavior() {
        const headbar = document.querySelector('.headbar');
        if (!headbar) return;

        let lastScrollY = window.scrollY;
        let ticking = false;

        const updateScrollState = () => {
            const currentScrollY = window.scrollY;
            
            // Add scrolled class when scrolled down
            if (currentScrollY > 50) {
                headbar.classList.add('scrolled');
            } else {
                headbar.classList.remove('scrolled');
            }

            // Hide/show header based on scroll direction
            if (currentScrollY > lastScrollY && currentScrollY > 100) {
                // Scrolling down - hide header
                headbar.classList.add('hidden');
            } else {
                // Scrolling up - show header
                headbar.classList.remove('hidden');
            }

            lastScrollY = currentScrollY;
            ticking = false;
        };

        const onScroll = () => {
            if (!ticking) {
                requestAnimationFrame(updateScrollState);
                ticking = true;
            }
        };

        window.addEventListener('scroll', onScroll, { passive: true });

        // Close mobile menu when scrolling
        window.addEventListener('scroll', () => {
            const mobileToggle = document.getElementById('mobile-menu-toggle');
            const nav = document.querySelector('.headbar-nav');
            
            if (mobileToggle && nav) {
                mobileToggle.classList.remove('active');
                nav.classList.remove('active');
            }
        }, { passive: true });
    }
}

// Initialize auth manager when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new AuthManager();
});
