"use client";

import { MessageSquare, Trash2 } from "lucide-react";
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
        <div className="flex flex-col h-full">
            <div className="flex-1 overflow-y-auto">
                <div className="space-y-1 p-2">
                    {chatHistories.map((chat, index) => (
                        <button
                            key={chat.wfRunId}
                            className={`w-full rounded-lg p-3 text-left flex items-center gap-3 group transition-colors ${
                                selectedIndex === index
                                    ? "bg-[#2e2f35] text-gray-100"
                                    : "text-gray-300 hover:bg-[#2e2f35]/50"
                            }`}
                            onClick={() => {
                                setSelectedIndex(index);
                                setCurrentChatIndex(index);
                            }}
                        >
                            <MessageSquare className="w-4 h-4 flex-shrink-0" />
                            <div className="flex-1 min-w-0">
                                <div className="truncate text-sm">
                                    {chat.history[0] || "New Chat"}
                                </div>
                                <div className="text-xs text-gray-500">
                                    {chat.history.length} messages
                                </div>
                            </div>
                            <button
                                className="opacity-0 group-hover:opacity-100 p-1 hover:bg-[#3e3f45] rounded transition-all"
                                onClick={(e) => {
                                    e.stopPropagation();
                                    // TODO: Implement delete functionality
                                    console.log("Delete chat", chat.wfRunId);
                                }}
                            >
                                <Trash2 className="w-4 h-4 text-gray-400 hover:text-gray-200" />
                            </button>
                        </button>
                    ))}
                </div>
            </div>
        </div>
    );
}
