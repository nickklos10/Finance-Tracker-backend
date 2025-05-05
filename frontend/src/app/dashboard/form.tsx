"use client";
import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { updateCurrentUser, deleteCurrentUser } from "@/lib/api";
import { User } from "@/types/user";
import { useApi } from "@/hooks/useApi";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner";
import { ApiErrorBoundary } from "@/components/ui/ApiErrorBoundary";

interface UserFormProps {
  initialUser: User;
}

export default function UserForm({ initialUser }: UserFormProps) {
  const router = useRouter();
  const [formData, setFormData] = useState({
    name: initialUser.name,
    email: initialUser.email,
  });

  const updateApi = useApi(updateCurrentUser);
  const deleteApi = useApi(deleteCurrentUser);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      await updateApi.execute(formData);
      router.refresh();
    } catch (error) {
      // Error is handled by the useApi hook
      console.error("Failed to update user profile");
    }
  };

  const handleDelete = async () => {
    if (
      window.confirm(
        "Are you sure you want to delete your account? This action cannot be undone."
      )
    ) {
      try {
        await deleteApi.execute();
        router.push("/api/auth/logout");
      } catch (error) {
        // Error is handled by the useApi hook
        console.error("Failed to delete user account");
      }
    }
  };

  if (updateApi.isLoading || deleteApi.isLoading) {
    return <LoadingSpinner text="Processing your request..." />;
  }

  return (
    <ApiErrorBoundary>
      <div className="bg-white p-6 rounded-lg shadow-md">
        <h2 className="text-xl font-semibold mb-4">Edit Profile</h2>

        {(updateApi.error || deleteApi.error) && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded text-red-700">
            {updateApi.error?.message ||
              deleteApi.error?.message ||
              "An error occurred"}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label
              htmlFor="name"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              Name
            </label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="mb-4">
            <label
              htmlFor="email"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              Email
            </label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="flex justify-between mt-6">
            <button
              type="submit"
              disabled={updateApi.isLoading}
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              Save Changes
            </button>

            <button
              type="button"
              onClick={handleDelete}
              disabled={deleteApi.isLoading}
              className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500"
            >
              Delete Account
            </button>
          </div>
        </form>
      </div>
    </ApiErrorBoundary>
  );
}
