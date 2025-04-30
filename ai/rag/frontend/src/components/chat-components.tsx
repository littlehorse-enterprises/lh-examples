"use client";

import { createNewChat } from "@/app/actions";
import { PlusCircle } from "lucide-react";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { toast } from "sonner";
import { ChatSection } from "./chat";
import { ChatHistory } from "./chat-history";
import { Button } from "./ui/button";

interface ChatHistoryItem {
    wfRunId: string;
    history: string[];
}

const sampleQuestions = [
    "What are the current tariffs on aluminum imports?",
    "How have steel import taxes from China changed recently?",
    "What are the latest regulations on semiconductor exports?",
    "Tell me about recent changes in EV battery trade policies",
    "What are the current solar panel import duties?",
    "How do rare earth mineral trade restrictions work?",
];

export function ChatComponents({ chatHistories }: { chatHistories: ChatHistoryItem[] }) {
    const [currentChatIndex, setCurrentChatIndex] = useState(0);
    const [isCreatingChat, setIsCreatingChat] = useState(false);
    const router = useRouter();

    const handleNewChat = async () => {
        try {
            setIsCreatingChat(true);
            await createNewChat("How can I help you today?");
            router.refresh();
            toast.success("New chat created!");
        } catch (error) {
            console.error("Error creating chat:", error);
            toast.error("Failed to create new chat");
        } finally {
            setIsCreatingChat(false);
        }
    };

    const handleQuestionSelect = async (question: string) => {
        try {
            setIsCreatingChat(true);
            await createNewChat(question);
            router.refresh();
            toast.success("Started new chat with your question!");
        } catch (error) {
            console.error("Error starting chat with question:", error);
            toast.error("Failed to start chat with question");
        } finally {
            setIsCreatingChat(false);
        }
    };

    // If there are no chat histories, show empty state
    if (!chatHistories || chatHistories.length === 0) {
        return (
            <div className="flex w-full h-screen bg-[#0f1117]">
                {/* Sidebar */}
                <div className="w-[260px] bg-[#1a1c23] flex flex-col h-full border-r border-[#2e2f35]">
                    <Button
                        onClick={handleNewChat}
                        className="m-2 bg-[#2e2f35] hover:bg-[#3e3f45] text-gray-200 flex items-center gap-2 transition-colors border-0"
                        disabled={isCreatingChat}
                    >
                        <PlusCircle className="w-4 h-4" />
                        {isCreatingChat ? "Creating..." : "New Chat"}
                    </Button>
                    <div className="flex-1 overflow-y-auto">
                        <ChatHistory
                            chatHistories={[]}
                            setCurrentChatIndex={setCurrentChatIndex}
                        />
                    </div>
                </div>
                
                {/* Main Chat Area - Empty State */}
                <div className="flex-1 flex flex-col h-full bg-[#0f1117] text-gray-100">
                    <div className="flex items-center justify-center h-full">
                        <div className="text-center max-w-2xl mx-auto px-4">
                            <h1 className="text-2xl font-semibold mb-2">Welcome to RAG Chat</h1>
                            <p className="text-gray-400 mb-8">Upload documents or start asking questions about trade policies</p>
                            
                            <div className="grid grid-cols-2 gap-4">
                                {sampleQuestions.map((question, index) => (
                                    <button
                                        key={index}
                                        className="text-left p-4 rounded-lg bg-[#1a1c23] hover:bg-[#2e2f35] transition-colors border border-[#2e2f35] text-sm text-gray-300 disabled:opacity-50 disabled:cursor-not-allowed"
                                        onClick={() => handleQuestionSelect(question)}
                                        disabled={isCreatingChat}
                                    >
                                        {question}
                                    </button>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="flex w-full h-screen bg-[#0f1117]">
            {/* Sidebar */}
            <div className="w-[260px] bg-[#1a1c23] flex flex-col h-full border-r border-[#2e2f35]">
                <Button
                    onClick={handleNewChat}
                    className="m-2 bg-[#2e2f35] hover:bg-[#3e3f45] text-gray-200 flex items-center gap-2 transition-colors border-0"
                    disabled={isCreatingChat}
                >
                    <PlusCircle className="w-4 h-4" />
                    {isCreatingChat ? "Creating..." : "New Chat"}
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
