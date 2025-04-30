"use client";

import { PlusCircle } from "lucide-react";
import { useState } from "react";
import { ChatSection } from "./chat";
import { ChatHistory } from "./chat-history";
import { Button } from "./ui/button";

interface ChatHistoryItem {
    wfRunId: string;
    history: string[];
}

export function ChatComponents({ chatHistories }: { chatHistories: ChatHistoryItem[] }) {
    const [currentChatIndex, setCurrentChatIndex] = useState(0);

    const handleNewChat = () => {
        // TODO: Implement new chat creation
        console.log("New chat clicked");
    };

    return (
        <div className="flex w-full h-screen bg-[#0f1117]">
            {/* Sidebar */}
            <div className="w-[260px] bg-[#1a1c23] flex flex-col h-full border-r border-[#2e2f35]">
                <Button
                    onClick={handleNewChat}
                    variant="outline"
                    className="m-2 border border-[#2e2f35] hover:bg-[#2e2f35] text-white flex items-center gap-2"
                >
                    <PlusCircle className="w-4 h-4" />
                    New Chat
                </Button>
                <div className="flex-1 overflow-y-auto">
                    <ChatHistory
                        chatHistories={chatHistories}
                        setCurrentChatIndex={setCurrentChatIndex}
                    />
                </div>
            </div>
            
            {/* Main Chat Area */}
            <div className="flex-1 flex flex-col h-full">
                <ChatSection
                    chatHistory={chatHistories[currentChatIndex].history}
                    wfRunId={chatHistories[currentChatIndex].wfRunId}
                />
            </div>
        </div>
    );
}
