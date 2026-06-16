// ============================================
// AKIBA HUB - AUTHENTICATION
// ============================================

class AuthManager {
    constructor() {
        this.token = localStorage.getItem('token');
        this.user = null;
        this._isRedirecting = false;  // Prevent multiple redirects
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
            console.error('Failed to decode token:', e);
            this.user = null;
            this.token = null;
            localStorage.removeItem('token');
            return false;
        }
    }

    isAuthenticated() {
        return !!this.token && this.user !== null;
    }

    getToken() {
        return this.token;
    }

    getUser() {
        return this.user;
    }

    logout() {
        this.token = null;
        this.user = null;
        localStorage.removeItem('token');
        window.location.href = 'login.html';
    }
}

// Create global instance
const auth = new AuthManager();

// Handle OAuth token from URL (only once)
(function handleOAuthRedirect() {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    if (token) {
        localStorage.setItem('token', token);
        // Remove token from URL immediately
        window.history.replaceState({}, document.title, window.location.pathname);
        // Reload the page to trigger auth check with the stored token
        window.location.reload();
        return;
    }
})();

// Redirect to login if not authenticated (ONLY if not already on login page)
document.addEventListener('DOMContentLoaded', function() {
    if (!window.location.pathname.includes('login.html')) {
        if (!auth.isAuthenticated()) {
            window.location.href = 'login.html';
        }
    }
});