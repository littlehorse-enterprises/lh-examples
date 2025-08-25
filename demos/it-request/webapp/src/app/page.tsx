import { redirect } from 'next/navigation';

export default function HomePage() {
  redirect('/workflow/new/step/1'); // keep URL consistency
}
