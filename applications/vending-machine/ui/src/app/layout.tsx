import { cn } from "@/lib/utils";
import { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import "tailwindcss/tailwind.css";
import { Toaster } from "@/components/ui/sonner";
import { ThemeProvider } from "@/components/theme-provider";

// Uncomment and fill out all metadata fields
export const metadata: Metadata = {
	// title: {
	// 	template: "%s — App",
	// 	default: "App",
	// },
	// description:
	// 	"Description",
	// category: "software",
	// metadataBase: new URL(""),
	// openGraph: {
	// 	type: "website",
	// 	locale: "en",
	// 	url: "",
	// 	siteName: "",
	// 	title: "",
	// 	description: "",
	// 	images: [
	// 		{
	// 			url: "/logo.svg",
	// 			width: 400,
	// 			height: 400,
	// 			alt: "",
	// 		},
	// 	],
	// },
	// twitter: {
	// 	creator: "@",
	// 	site: "",
	// 	card: "",
	// 	description: "",
	// 	images: [""],
	// },
	// creator: "",
	// colorScheme: "light",
	// themeColor: "#ffb400",
	// publisher: "",
	// keywords: [],
	// referrer: "no-referrer-when-downgrade",
	// applicationName: "",
	// icons: {
	// 	icon: "/icons/favicon.ico",
	// 	apple: "/icons/apple-touch-icon.png",
	// 	shortcut: "/icons/favicon.ico",
	// },
	// manifest: "/site.webmanifest",
	// viewport: {
	// 	width: "device-width",
	// 	initialScale: 1,
	// 	maximumScale: 1,
	// 	userScalable: false,
	// },
	// other: {
	// 	handheldFriendly: "true",
	// },
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
			<body className="h-full w-full">
				<ThemeProvider
					attribute="class"
					defaultTheme="system"
					enableSystem
					disableTransitionOnChange
				/>
				<Toaster />
				{children}
			</body>
		</html>
	);
}
