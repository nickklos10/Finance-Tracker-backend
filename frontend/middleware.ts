import { NextResponse, type NextRequest } from "next/server";
import { auth0 } from "./src/lib/auth0";

// This function handles authenticated routes
export async function middleware(req: NextRequest) {
  // Use the middleware from Auth0Client
  const response = await auth0.middleware(req);

  // Add security headers to all responses
  if (response instanceof NextResponse) {
    // Add security headers
    response.headers.set("X-Frame-Options", "DENY");
    response.headers.set("X-Content-Type-Options", "nosniff");
    response.headers.set("Referrer-Policy", "strict-origin-when-cross-origin");
    response.headers.set(
      "Strict-Transport-Security",
      "max-age=31536000; includeSubDomains"
    );
  }

  return response;
}

// Apply middleware to secured routes
export const config = {
  matcher: [
    "/dashboard/:path*",
    "/api/transactions/:path*",
    "/api/categories/:path*",
    "/profile/:path*",
  ],
};
