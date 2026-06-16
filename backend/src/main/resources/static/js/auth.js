// ============================================
// AKIBA HUB - AUTHENTICATION
// ============================================

class AuthManager {
    constructor() {
        this.token = localStorage.getItem('token');
        this.user = null;
        this.isLoading = false;
        
        if (this.token) {
            this.decodeToken();
        }
    }

    decodeToken() {
        try {
            const payload = JSON.parse(atob(this.token.split('.')[1]));
            this.user = {
                id: payload.userId,
                email: payload.sub,
                name: payload.name || payload.sub
            };
            return true;
        } catch (e) {
            console.warn('Invalid token:', e);
            this.token = null;
            this.user = null;
            localStorage.removeItem('token');
            return false;
        }
    }

    isAuthenticated() {
        return !!this.token && !!this.user;
    }

    getToken() {
        return this.token;
    }

    getUser() {
        return this.user;
    }

    // Store token from URL (OAuth redirect)
    handleOAuthRedirect() {
        const params = new URLSearchParams(window.location.search);
        const token = params.get('token');
        
        if (token) {
            localStorage.setItem('token', token);
            this.token = token;
            this.decodeToken();
            // Remove token from URL without reloading
            const cleanUrl = window.location.pathname;
            window.history.replaceState({}, document.title, cleanUrl);
            return true;
        }
        return false;
    }

    logout() {
        this.token = null;
        this.user = null;
        localStorage.removeItem('token');
        window.location.replace('login.html');
    }
}

// Create single instance
const auth = new AuthManager();

// Handle OAuth redirect immediately
auth.handleOAuthRedirect();

// Redirect to login if not authenticated (for protected pages)
function requireAuth() {
    if (!auth.isAuthenticated() && !window.location.pathname.includes('login.html')) {
        window.location.replace('login.html');
        return false;
    }
    return true;
}