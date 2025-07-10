import { Inter } from "next/font/google";
import { cookies } from "next/headers";
import { CartProvider } from "./contexts/CartContext";
import "./globals.css";

const inter = Inter({ subsets: ["latin"] });

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const email = cookies().get("email")?.value;
  const cartId = cookies().get("cartId")?.value;

  return (
    <html lang="en">
      <body className={inter.className}>
        <CartProvider cartId={cartId} email={email} >
          <main className="mt-6 flex items-center justify-center">
            {children}
          </main>
        </CartProvider>
      </body>
    </html>
  );
}
