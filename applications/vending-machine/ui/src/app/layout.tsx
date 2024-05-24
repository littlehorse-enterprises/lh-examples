import { cn } from "@/lib/utils";
import { Inter } from "next/font/google";
import "./globals.css";
import "tailwindcss/tailwind.css";
import { Toaster } from "@/components/ui/sonner";
import { ThemeProvider } from "@/components/theme-provider";

const inter = Inter({
	variable: "--font-inter",
	display: "swap",
	subsets: ["latin", "latin-ext"],
	adjustFontFallback: true,
});

export default function RootLayout({
	children,
}: {
	children: React.ReactNode;
}) {
	return (
		<html lang="en" className={cn("h-screen w-screen", inter.className)}>
			<body className="h-full w-full">
				<ThemeProvider
					attribute="class"
					defaultTheme="system"
					enableSystem
					disableTransitionOnChange
				/>
				<Toaster position="top-center" richColors />

				{children}
			</body>
		</html>
	);
}
