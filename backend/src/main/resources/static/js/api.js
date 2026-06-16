// ============================================
// AKIBA HUB - API CLIENT
// ============================================

// Use production URL when deployed, localhost for development
const API_BASE = window.location.hostname === 'localhost' 
    ? 'http://localhost:8080/api' 
    : 'https://akibahub.unitybridge.dev/api';

class ApiClient {
    constructor() {
        this.token = localStorage.getItem('token');
    }

    getAuthHeader() {
        return {
            'Authorization': `Bearer ${this.token}`,
            'Content-Type': 'application/json'
        };
    }

    async request(endpoint, method = 'GET', body = null) {
        const options = {
            method: method,
            headers: this.getAuthHeader()
        };
        if (body) {
            options.body = JSON.stringify(body);
        }

        const response = await fetch(`${API_BASE}${endpoint}`, options);
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || 'API Error');
        }
        return data;
    }

    // ============================================
    // AUTH
    // ============================================

    async register(email, password) {
        return this.request('/auth/register', 'POST', { email, password });
    }

    async login(email, password) {
        return this.request('/auth/login', 'POST', { email, password });
    }

    // ============================================
    // SAVINGS
    // ============================================

    async getBalance() {
        return this.request('/savings/balance');
    }

    async deposit(amount) {
        return this.request('/savings/deposit', 'POST', { amount });
    }

    async withdraw(amount) {
        return this.request('/savings/withdraw', 'POST', { amount });
    }

    async getHistory() {
        return this.request('/savings/history');
    }

    // ============================================
    // GROUPS
    // ============================================

    async createGroup(name, description, monthlyContribution) {
        return this.request('/groups/create', 'POST', { name, description, monthlyContribution });
    }

    async generateInviteCode(groupId) {
        return this.request(`/groups/invite/${groupId}`, 'POST');
    }

    async joinGroup(code) {
        return this.request('/groups/join', 'POST', { code });
    }

    async contributeToGroup(groupId, amount) {
        return this.request(`/groups/contribute/${groupId}`, 'POST', { amount });
    }

    async getGroupBalance(groupId) {
        return this.request(`/groups/${groupId}/balance`);
    }

    // ============================================
    // GOVERNANCE
    // ============================================

    async createProposal(groupId, title, description, amount) {
        return this.request('/governance/proposals', 'POST', { groupId, title, description, amount });
    }

    async castVote(proposalId, decision) {
        return this.request(`/governance/proposals/${proposalId}/vote`, 'POST', { decision });
    }

    async getPendingProposals(groupId) {
        return this.request(`/governance/groups/${groupId}/pending`);
    }
}

// Create global instance
const api = new ApiClient();