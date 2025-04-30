"use client";

import { deleteChat } from "@/app/actions";
import { MessageSquare, Trash2 } from "lucide-react";
import { useEffect, useState } from "react";
import { toast } from "sonner";

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

    // Update selected index when chat histories change
    useEffect(() => {
        if (selectedIndex >= chatHistories.length && chatHistories.length > 0) {
            // If current selected index is out of bounds, select the last chat
            setSelectedIndex(chatHistories.length - 1);
            setCurrentChatIndex(chatHistories.length - 1);
        }
    }, [chatHistories, selectedIndex, setCurrentChatIndex]);

    const handleDelete = async (e: React.MouseEvent, chatId: string, index: number) => {
        e.stopPropagation();
        try {
            await deleteChat(chatId);
            toast.success("Chat deleted");
            
            // If we're deleting the currently selected chat
            if (index === selectedIndex) {
                // If there are chats before this one, select the previous chat
                if (index > 0) {
                    setSelectedIndex(index - 1);
                    setCurrentChatIndex(index - 1);
                }
                // If this is the first chat and there are more chats, select the next one
                else if (chatHistories.length > 1) {
                    setSelectedIndex(0);
                    setCurrentChatIndex(0);
                }
            }
            // If we're deleting a chat before the selected one, update the index
            else if (index < selectedIndex) {
                setSelectedIndex(prev => prev - 1);
                setCurrentChatIndex(prev => prev - 1);
            }
        } catch (error) {
            console.error("Error deleting chat:", error);
            toast.error("Failed to delete chat");
        }
    };

    return (
        <div className="flex flex-col h-full">
            <div className="flex-1 overflow-y-auto">
                <div className="space-y-1 p-2">
                    {chatHistories.map((chat, index) => (
                        <div
                            key={chat.wfRunId}
                            className={`w-full rounded-lg p-3 text-left flex items-center gap-3 group transition-colors cursor-pointer ${
                                selectedIndex === index
                                    ? "bg-[#2e2f35] text-gray-100"
                                    : "text-gray-300 hover:bg-[#2e2f35]/50"
                            }`}
                            onClick={() => {
                                setSelectedIndex(index);
                                setCurrentChatIndex(index);
                            }}
                            role="button"
                            tabIndex={0}
                            onKeyDown={(e) => {
                                if (e.key === 'Enter' || e.key === ' ') {
                                    setSelectedIndex(index);
                                    setCurrentChatIndex(index);
                                }
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
                                onClick={(e) => handleDelete(e, chat.wfRunId, index)}
                                aria-label="Delete chat"
                            >
                                <Trash2 className="w-4 h-4 text-gray-400 hover:text-gray-200" />
                            </button>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}
