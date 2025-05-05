import { Auth0Provider } from "@auth0/nextjs-auth0/client";
import "./globals.css";

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>
        <Auth0Provider
          domain="dev-ugadnr0ui0vziqee.us.auth0.com"
          clientId={process.env.NEXT_PUBLIC_AUTH0_CLIENT_ID || ""}
          authorizationParams={{
            redirect_uri:
              typeof window !== "undefined" ? window.location.origin : "",
            audience: "https://finsight-api",
          }}
        >
          {children}
        </Auth0Provider>
      </body>
    </html>
  );
}
