import { cn } from "@/lib/utils";
import { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import "tailwindcss/tailwind.css";
import { ThemeProvider } from "@/components/theme-provider";

export const metadata: Metadata = {
	title: {
		template: "%s — LittleHorse: User Tasks / IT Request Example",
		default: "LittleHorse: User Tasks / IT Request Example",
	},
	description: 'A demo showcasing LittleHorse User Tasks in an IT request workflow',
	icons: {
    icon: [
      { url: '/favicon-16x16.png', sizes: '16x16', type: 'image/png' },
      { url: '/favicon-32x32.png', sizes: '32x32', type: 'image/png' },
      { url: '/favicon.png', type: 'image/png' },
    ],
  },
};

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
			<body className="h-full w-full min-h-screen bg-black text-white">
				<ThemeProvider
					attribute="class"
					defaultTheme="system"
					enableSystem
					disableTransitionOnChange
				/>
				{children}
			</body>
		</html>
	);
}
