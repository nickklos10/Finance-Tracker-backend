'use client';
import Link from 'next/link';
import { useUser } from '@auth0/nextjs-auth0';

export default function HomePage() {
  const { user } = useUser();

  return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50 dark:bg-gray-900 p-4">
        <h1 className="text-3xl font-bold mb-6 text-gray-900 dark:text-gray-100">Welcome to FinSight</h1>
        {user ? (
            <Link
                href="/dashboard"
                className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
            >
              Go to Dashboard
            </Link>
        ) : (
            <Link
                href="/api/auth/login"
                className="px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition"
            >
              Log In
            </Link>
        )}
      </div>
  );
}
