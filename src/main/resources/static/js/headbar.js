// Headbar Manager
class HeadbarManager {
    constructor() {
        this.currentPage = this.getCurrentPage();
        this.init();
    }

    init() {
        this.loadHeadbar();
        this.setupEventListeners();
        this.setupScrollBehavior();
        this.setupMobileMenu();
        this.setupActivePage();
    }

    getCurrentPage() {
        const path = window.location.pathname;
        if (path === '/' || path === '/index.html') return 'home';
        if (path.includes('donate')) return 'donate';
        if (path.includes('about')) return 'about';
        if (path.includes('contact')) return 'contact';
        if (path.includes('login')) return 'login';
        if (path.includes('register')) return 'register';
        return 'home';
    }

    async loadHeadbar() {
        try {
            const response = await fetch('/components/headbar.html');
            const headbarHTML = await response.text();
            
            // Find existing headbar and replace it
            const existingHeadbar = document.querySelector('.headbar');
            if (existingHeadbar) {
                existingHeadbar.outerHTML = headbarHTML;
            } else {
                // Insert at the beginning of body
                document.body.insertAdjacentHTML('afterbegin', headbarHTML);
            }
        } catch (error) {
            console.error('Error loading headbar:', error);
        }
    }

    setupEventListeners() {
        // Re-setup event listeners after headbar is loaded
        setTimeout(() => {
            this.setupMobileMenu();
            this.setupActivePage();
        }, 100);
    }

    setupActivePage() {
        const navLinks = document.querySelectorAll('.nav-link');
        navLinks.forEach(link => {
            const page = link.getAttribute('data-page');
            if (page === this.currentPage) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });
    }

    setupMobileMenu() {
        const mobileToggle = document.getElementById('mobile-menu-toggle');
        const nav = document.querySelector('.headbar-nav');
        
        if (mobileToggle && nav) {
            // Remove existing listeners
            mobileToggle.replaceWith(mobileToggle.cloneNode(true));
            const newMobileToggle = document.getElementById('mobile-menu-toggle');
            
            newMobileToggle.addEventListener('click', () => {
                newMobileToggle.classList.toggle('active');
                nav.classList.toggle('active');
            });

            // Close mobile menu when clicking on nav links
            const navLinks = nav.querySelectorAll('.nav-link');
            navLinks.forEach(link => {
                link.addEventListener('click', () => {
                    newMobileToggle.classList.remove('active');
                    nav.classList.remove('active');
                });
            });

            // Close mobile menu when clicking outside
            document.addEventListener('click', (e) => {
                if (!newMobileToggle.contains(e.target) && !nav.contains(e.target)) {
                    newMobileToggle.classList.remove('active');
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

    // Method to update headbar content
    updateHeadbar(newContent) {
        const headbar = document.querySelector('.headbar');
        if (headbar) {
            headbar.innerHTML = newContent;
            this.setupEventListeners();
        }
    }

    // Method to add new navigation item
    addNavItem(href, text, page) {
        const nav = document.querySelector('.headbar-nav');
        if (nav) {
            const newLink = document.createElement('a');
            newLink.href = href;
            newLink.textContent = text;
            newLink.className = 'nav-link';
            newLink.setAttribute('data-page', page);
            nav.appendChild(newLink);
        }
    }

    // Method to remove navigation item
    removeNavItem(page) {
        const navLink = document.querySelector(`.nav-link[data-page="${page}"]`);
        if (navLink) {
            navLink.remove();
        }
    }
}

// Initialize headbar manager when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.headbarManager = new HeadbarManager();
});
