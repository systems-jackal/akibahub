// ============================================
// AKIBA HUB - AUTHENTICATION
// ============================================

class AuthManager {
    constructor() {
        this.token = localStorage.getItem('token');
        this.user = null;
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

// Redirect to login if not authenticated (except on login page)
document.addEventListener('DOMContentLoaded', () => {
    if (!window.location.pathname.includes('login.html')) {
        if (!auth.isAuthenticated()) {
            window.location.href = 'login.html';
        }
    }
});