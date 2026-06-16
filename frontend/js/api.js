// js/api.js

const API_BASE = 'http://localhost:8080';

// Store token in memory or localStorage
let authToken = localStorage.getItem('token') || null;

export function setToken(token) {
    authToken = token;
    if (token) {
        localStorage.setItem('token', token);
    } else {
        localStorage.removeItem('token');
    }
}

export function getToken() {
    return authToken;
}

// Generic fetch with auth header
export async function apiFetch(endpoint, options = {}) {
    const url = API_BASE + endpoint;
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
    };
    if (authToken) {
        headers['Authorization'] = `Bearer ${authToken}`;
    }

    const config = {
        ...options,
        headers,
    };

    const response = await fetch(url, config);
    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.message || 'API error');
    }
    return data;
}

// Specific API calls
export function login(phoneNumber, password) {
    return apiFetch('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ phoneNumber, password }),
    });
}

export function register(userData) {
    return apiFetch('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify(userData),
    });
}

export function getGroups() {
    return apiFetch('/api/groups');
}

export function createGroup(groupData) {
    return apiFetch('/api/groups', {
        method: 'POST',
        body: JSON.stringify(groupData),
    });
}

// Add more endpoints as needed...