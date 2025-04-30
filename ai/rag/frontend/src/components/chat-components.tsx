"use client";

import { ChatSection } from "./chat";
import { ChatHistory } from "./chat-history";
import { useState } from "react";

interface ChatHistoryItem {
    wfRunId: string;
    history: string[];
}

export function ChatComponents({ chatHistories }: { chatHistories: ChatHistoryItem[] }) {
    const [currentChatIndex, setCurrentChatIndex] = useState(0);

    return <div className="flex w-full">
        <ChatHistory
            chatHistories={chatHistories}
            setCurrentChatIndex={setCurrentChatIndex}
        />
        <ChatSection
            chatHistory={chatHistories[currentChatIndex].history}
            wfRunId={chatHistories[currentChatIndex].wfRunId}
        />
    </div>;
}
