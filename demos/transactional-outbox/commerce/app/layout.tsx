import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import Error from "./error";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "Commerce Littlehorse",
  description: "Simple commerce app",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <main className="mt-6 flex items-center justify-center">{children}</main>
      </body>
    </html>
  );
}
