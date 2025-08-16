import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'LittleHorse: User Tasks / IT Request Example',
  description: 'A demo showcasing LittleHorse User Tasks in an IT request workflow',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>
        <a className="skip-link" href="#main">Skip to main content</a>
        {children}
      </body>
    </html>
  );
}
