"use client";

import { useState } from "react";

interface ChatHistoryItem {
    wfRunId: string;
    history: string[];
}

interface ChatHistoryProps {
    chatHistories: ChatHistoryItem[];
    setCurrentChatIndex: (index: number) => void;
}

export function ChatHistory({ chatHistories, setCurrentChatIndex }: ChatHistoryProps) {
    const [selectedIndex, setSelectedIndex] = useState(0);

    return (
        <div className="w-64 border-r border-gray-200 p-4">
            <h2 className="mb-4 text-lg font-semibold">Chat History</h2>
            <div className="space-y-2">
                {chatHistories.map((chat, index) => (
                    <button
                        key={chat.wfRunId}
                        className={`w-full rounded-lg p-2 text-left ${selectedIndex === index
                            ? "bg-blue-100 text-blue-700"
                            : "hover:bg-gray-100"
                            }`}
                        onClick={() => {
                            setSelectedIndex(index);
                            setCurrentChatIndex(index);
                        }}
                    >
                        <div className="truncate">
                            {chat.history[0] || "New Chat"}
                        </div>
                        <div className="text-xs text-gray-500">
                            {chat.history.length} messages
                        </div>
                    </button>
                ))}
            </div>
        </div>
    );
}
