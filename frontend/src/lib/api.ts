import { User } from '@/types/user';

const API_URL = process.env.NEXT_PUBLIC_API_URL;

async function handleResponse(res: Response) {
    if (!res.ok) {
        const payload = await res.json().catch(() => null);
        throw new Error(payload?.detail || res.statusText);
    }
    return res.status === 204 ? null : res.json();
}

export function getCurrentUser(): Promise<User> {
    return fetch(`${API_URL}/api/users/me`, {
        credentials: 'include',
    }).then(handleResponse);
}

export function updateCurrentUser(data: { name: string; email: string }): Promise<User> {
    return fetch(`${API_URL}/api/users/me`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(data),
    }).then(handleResponse);
}

export function deleteCurrentUser(): Promise<void> {
    return fetch(`${API_URL}/api/users/me`, {
        method: 'DELETE',
        credentials: 'include',
    }).then(handleResponse);
}
