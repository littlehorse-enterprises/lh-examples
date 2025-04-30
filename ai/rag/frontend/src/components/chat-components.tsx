"use client";

import { createNewChat } from "@/app/actions";
import { Button } from "@/components/ui/button";
import { PlusCircle, Send } from "lucide-react";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { toast } from "sonner";
import { ChatSection } from "./chat";
import { ChatHistory } from "./chat-history";

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
    const [isNewChat, setIsNewChat] = useState(true);
    const [customQuestion, setCustomQuestion] = useState('');
    const router = useRouter();

    // Load prefilled question from localStorage on mount and ensure we start on new chat page
    useEffect(() => {
        // Set to true when the component mounts to ensure we start with new chat page
        setIsNewChat(true);
        
        const prefillQuestion = localStorage.getItem('prefillQuestion');
        if (prefillQuestion) {
            setCustomQuestion(prefillQuestion);
            localStorage.removeItem('prefillQuestion');
        }
    }, []);

    const handleNewChat = () => {
        setIsNewChat(true);
        setCurrentChatIndex(-1);
        setCustomQuestion('');
    };

    const handleCustomQuestionSubmit = async () => {
        const trimmedQuestion = customQuestion.trim();
        if (trimmedQuestion) {
            try {
                await createNewChat(trimmedQuestion);
                router.refresh();
                
                // Wait for the refresh to complete then select the latest chat
                setTimeout(() => {
                    setIsNewChat(false);
                    setCurrentChatIndex(0); // Select the first chat (most recent)
                }, 100);
                
                toast.success("Started new chat with your question!");
            } catch (error) {
                console.error("Error starting chat with question:", error);
                toast.error("Failed to start chat with question");
            }
        }
    };

    // Check if currentChatIndex is valid
    useEffect(() => {
        // If chatHistories is empty or currentChatIndex is out of bounds
        if ((!chatHistories || chatHistories.length === 0) && currentChatIndex !== -1) {
            // Reset to new chat view
            setIsNewChat(true);
            setCurrentChatIndex(-1);
        } else if (chatHistories && currentChatIndex >= chatHistories.length && currentChatIndex !== -1) {
            // If currentChatIndex is out of bounds but there are chats
            setCurrentChatIndex(chatHistories.length - 1);
        }
    }, [chatHistories, currentChatIndex]);

    // Show new chat page if explicitly set or no chats exist
    if (isNewChat || !chatHistories || chatHistories.length === 0) {
        return (
            <div className="flex w-full h-screen bg-[#0f1117]">
                {/* Sidebar */}
                <div className="w-[260px] bg-[#1a1c23] flex flex-col h-full border-r border-[#2e2f35]">
                    <Button
                        onClick={handleNewChat}
                        className="m-2 bg-[#2e2f35] hover:bg-[#3e3f45] text-gray-200 flex items-center gap-2 transition-colors border-0"
                    >
                        <PlusCircle className="w-4 h-4" />
                        New Chat
                    </Button>
                    <div className="flex-1 overflow-y-auto">
                        <ChatHistory
                            chatHistories={chatHistories || []}
                            setCurrentChatIndex={(index) => {
                                setCurrentChatIndex(index);
                                setIsNewChat(false);
                            }}
                            currentChatIndex={isNewChat ? -1 : currentChatIndex}
                        />
                    </div>
                </div>
                
                {/* Main Chat Area - Empty State */}
                <div className="flex-1 flex flex-col h-full bg-[#0f1117] text-gray-100">
                    <div className="flex items-center justify-center h-full">
                        <div className="text-center max-w-2xl mx-auto px-4">
                            <h1 className="text-2xl font-semibold mb-2">Welcome to RAG Chat</h1>
                            <p className="text-gray-400 mb-8">Upload documents or start asking questions about trade policies</p>
                            
                            <div className="relative mb-8">
                                <textarea
                                    placeholder="Type your question here..."
                                    className="w-full rounded-xl border border-[#2e2f35] bg-[#1a1c23] p-4 pr-12 text-gray-100 placeholder-gray-400 focus:border-[#3e3f45] focus:outline-none focus:ring-1 focus:ring-[#3e3f45]"
                                    rows={4}
                                    style={{ resize: 'none' }}
                                    value={customQuestion}
                                    onChange={(e) => setCustomQuestion(e.target.value)}
                                    onKeyDown={(e) => {
                                        if (e.key === 'Enter' && !e.shiftKey) {
                                            e.preventDefault();
                                            handleCustomQuestionSubmit();
                                        }
                                    }}
                                />
                                <button
                                    onClick={handleCustomQuestionSubmit}
                                    className="absolute right-2 bottom-2 p-2 rounded-lg text-gray-400 hover:text-gray-200 hover:bg-[#2e2f35] transition-colors"
                                >
                                    <Send className="h-5 w-5" />
                                </button>
                            </div>

                            <p className="text-gray-400 mb-4">Or choose a sample question:</p>
                            
                            <div className="grid grid-cols-2 gap-4">
                                {sampleQuestions.map((question, index) => (
                                    <button
                                        key={index}
                                        className="text-left p-4 rounded-lg bg-[#1a1c23] hover:bg-[#2e2f35] transition-colors border border-[#2e2f35] text-sm text-gray-300"
                                        onClick={() => {
                                            setCustomQuestion(question);
                                            handleCustomQuestionSubmit();
                                        }}
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
                >
                    <PlusCircle className="w-4 h-4" />
                    New Chat
                </Button>
                <div className="flex-1 overflow-y-auto">
                    <ChatHistory
                        chatHistories={chatHistories}
                        setCurrentChatIndex={(index) => {
                            setCurrentChatIndex(index);
                            setIsNewChat(false);
                        }}
                        currentChatIndex={isNewChat ? -1 : currentChatIndex}
                    />
                </div>
            </div>
            
            {/* Main Chat Area */}
            <div className="flex-1 flex flex-col h-full">
                {chatHistories && 
                 currentChatIndex >= 0 && 
                 currentChatIndex < chatHistories.length ? (
                    <ChatSection
                        chatHistory={chatHistories[currentChatIndex].history}
                        wfRunId={chatHistories[currentChatIndex].wfRunId}
                        isNewChat={isNewChat}
                        onFirstMessage={() => {
                            setIsNewChat(false);
                            router.refresh();
                        }}
                    />
                ) : (
                    // Fallback to empty chat section if current chat doesn't exist
                    <div className="flex items-center justify-center h-full text-gray-400">
                        <p>Select a chat from the sidebar or start a new one</p>
                    </div>
                )}
            </div>
        </div>
    );
}
