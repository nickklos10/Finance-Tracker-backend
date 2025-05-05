import { User } from '@/types/user';

const API_URL = process.env.NEXT_PUBLIC_API_URL;

async function handleResponse(res: Response) {
    if (!res.ok) {
        const payload = await res.json().catch(() => null);
        throw new Error(payload?.detail || res.statusText);
    }
    return res.status === 204 ? null : res.json();
}

export async function getCurrentUser(): Promise<User> {
    const res = await fetch(`${API_URL}/api/users/me`, {
        credentials: 'include',
    });
    return handleResponse(res);
}

export async function updateCurrentUser(data: { name: string; email: string }): Promise<User> {
    const res = await fetch(`${API_URL}/api/users/me`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        credentials: 'include',
        body: JSON.stringify(data),
    });
    return handleResponse(res);
}

export async function deleteCurrentUser(): Promise<void> {
    const res = await fetch(`${API_URL}/api/users/me`, {
        method: 'DELETE',
        credentials: 'include',
    });
    return handleResponse(res);
}
