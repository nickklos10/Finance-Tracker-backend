'use client';
import React, { useEffect, useState } from 'react';
import { getCurrentUser } from '@/lib/api';
import { User } from '@/types/user';
import UserForm from './form';

export default function DashboardPage() {
    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        getCurrentUser()
            .then(setUser)
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, []);

    if (loading) return <p className="p-4">Loading...</p>;
    if (error) return <p className="p-4 text-red-500">Error: {error}</p>;

    return (
        <div className="container mx-auto p-4">
            <h2 className="text-2xl font-semibold mb-4">Your Profile</h2>
            <UserForm initialUser={user!} />
        </div>
    );
}
