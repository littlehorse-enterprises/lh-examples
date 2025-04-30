'use client';

import { getChatHistories } from "./actions";
import { ChatComponents } from "@/components/chat-components";
import useSWR from 'swr';

export default function Home() {

  const { data: chatHistories, error } = useSWR(
    'chat-histories',
    getChatHistories,
    {
      refreshInterval: 1000,
      revalidateOnFocus: true,
    }
  );

  if (error) return <div>Failed to load chat histories</div>;
  if (!chatHistories) return <div>Loading...</div>;

  return (
    <div className="flex h-full">
      <ChatComponents chatHistories={chatHistories} />
    </div>
  );
}
