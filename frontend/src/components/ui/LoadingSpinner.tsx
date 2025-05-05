import React from "react";

interface LoadingSpinnerProps {
  size?: "sm" | "md" | "lg";
  fullscreen?: boolean;
  text?: string;
}

export function LoadingSpinner({
  size = "md",
  fullscreen = false,
  text = "Loading...",
}: LoadingSpinnerProps) {
  const sizeClasses = {
    sm: "w-4 h-4 border-2",
    md: "w-8 h-8 border-2",
    lg: "w-12 h-12 border-3",
  };

  const spinner = (
    <div
      className={`inline-flex flex-col items-center justify-center ${
        fullscreen ? "h-screen w-full fixed inset-0 bg-white/80 z-50" : ""
      }`}
    >
      <div
        className={`${sizeClasses[size]} rounded-full border-t-blue-500 border-blue-200 animate-spin`}
      ></div>
      {text && <p className="mt-2 text-gray-600">{text}</p>}
    </div>
  );

  return spinner;
}
