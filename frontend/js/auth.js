// js/auth.js
import { setToken } from './api.js';

export function logout() {
    setToken(null);
    localStorage.removeItem('token');
}