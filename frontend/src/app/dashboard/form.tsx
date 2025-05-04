'use client';
import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { updateCurrentUser, deleteCurrentUser } from '@/lib/api';
import { User } from '@/types/user';

interface UserFormProps {
    initialUser: User;
}

export default function UserForm({ initialUser }: UserFormProps) {
    const [name, setName] = useState(initialUser.name);
    const [email, setEmail] = useState(initialUser.email);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const router = useRouter();

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setLoading(true);
        setError(null);
        try {
            await updateCurrentUser({ name, email });
            // Optionally refetch or show a toast
        } catch (err: any) {
            setError(err.message || 'Update failed');
        } finally {
            setLoading(false);
        }
    }

    async function handleDelete() {
        if (!confirm('Delete your account? This cannot be undone.')) return;
        setLoading(true);
        try {
            await deleteCurrentUser();
            router.push('/');
        } catch (err: any) {
            setError(err.message || 'Delete failed');
        }
    }

    return (
        <form onSubmit={handleSubmit} className="max-w-md space-y-4">
            {error && <p className="text-red-500">{error}</p>}
            <div>
                <label htmlFor="name" className="block text-sm font-medium">Name</label>
                <input
                    id="name"
                    type="text"
                    className="mt-1 w-full p-2 border rounded"
                    value={name}
                    onChange={e => setName(e.target.value)}
                    required
                    maxLength={100}
                />
            </div>
            <div>
                <label htmlFor="email" className="block text-sm font-medium">Email</label>
                <input
                    id="email"
                    type="email"
                    className="mt-1 w-full p-2 border rounded"
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                    required
                    maxLength={100}
                />
            </div>
            <div className="flex space-x-2">
                <button
                    type="submit"
                    disabled={loading}
                    className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
                >
                    {loading ? 'Saving…' : 'Save'}
                </button>
                <button
                    type="button"
                    onClick={handleDelete}
                    disabled={loading}
                    className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition"
                >
                    {loading ? 'Deleting…' : 'Delete Account'}
                </button>
            </div>
        </form>
    );
}
