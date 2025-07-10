"use client";

import { createNewChat, uploadPdf } from "@/app/actions";
import { Button } from "@/components/ui/button";
import { PlusCircle, Send, Upload, X } from "lucide-react";
import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
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

export function ChatComponents({
  chatHistories,
}: {
  chatHistories: ChatHistoryItem[];
}) {
  const [currentChatIndex, setCurrentChatIndex] = useState(0);
  const [isNewChat, setIsNewChat] = useState(true);
  const [customQuestion, setCustomQuestion] = useState("");
  const [uploading, setUploading] = useState(false);
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const router = useRouter();

  // Load prefilled question from localStorage on mount and ensure we start on new chat page
  useEffect(() => {
    // Set to true when the component mounts to ensure we start with new chat page
    setIsNewChat(true);

    const prefillQuestion = localStorage.getItem("prefillQuestion");
    if (prefillQuestion) {
      setCustomQuestion(prefillQuestion);
      localStorage.removeItem("prefillQuestion");
    }
  }, []);

  const handleNewChat = () => {
    setIsNewChat(true);
    setCurrentChatIndex(-1);
    setCustomQuestion("");
  };

  const handleCustomQuestionSubmit = async () => {
    const trimmedQuestion = customQuestion.trim();
    if (trimmedQuestion) {
      try {
        // createNewChat returns the chatId
        const newChatId = await createNewChat(trimmedQuestion);

        // Store the new chat ID in sessionStorage
        sessionStorage.setItem("selectChatId", newChatId);

        router.refresh();
        toast.success("Started new chat with your question!");
      } catch (error) {
        console.error("Error starting chat with question:", error);
        toast.error("Failed to start chat with question");
      }
    }
  };

  const handleSampleQuestionClick = async (question: string) => {
    try {
      // Create new chat directly with the sample question
      const newChatId = await createNewChat(question);

      // Store the new chat ID in sessionStorage
      sessionStorage.setItem("selectChatId", newChatId);

      router.refresh();
      toast.success("Started new chat with your question!");
    } catch (error) {
      console.error("Error starting chat with question:", error);
      toast.error("Failed to start chat with question");
    }
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files) return;

    const newFiles = Array.from(files).filter(
      (file) => file.type === "application/pdf",
    );
    if (newFiles.length === 0) {
      toast.error("Please select PDF files only");
      return;
    }

    setSelectedFiles((prev) => [...prev, ...newFiles]);

    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const removeFile = (index: number) => {
    setSelectedFiles((prev) => {
      const newFiles = prev.filter((_, i) => i !== index);
      if (newFiles.length === 0) {
        toast.info("All files removed");
      }
      return newFiles;
    });
  };

  const uploadFiles = async () => {
    if (selectedFiles.length === 0) return;

    try {
      setUploading(true);
      let processedCount = 0;
      let alreadyProcessedCount = 0;
      let failedCount = 0;

      for (const file of selectedFiles) {
        try {
          const formData = new FormData();
          formData.append("file", file);
          await uploadPdf(formData);
          processedCount++;
        } catch (error: unknown) {
          // Check if this is an "already exists" error
          if (
            error instanceof Error &&
            error.message.includes("ALREADY_EXISTS")
          ) {
            console.log(`File ${file.name} was already processed`);
            alreadyProcessedCount++;
          } else {
            console.error("Error uploading file:", file.name, error);
            failedCount++;
          }
        }
      }

      // Show appropriate messages based on results
      if (alreadyProcessedCount > 0) {
        toast.info(
          `${alreadyProcessedCount} PDF${
            alreadyProcessedCount > 1 ? "s were" : " was"
          } already processed.`,
        );
      }

      if (processedCount > 0) {
        toast.success(
          `${processedCount} PDF${
            processedCount > 1 ? "s" : ""
          } uploaded and being processed.`,
        );
      }

      if (failedCount > 0) {
        toast.error(
          `Failed to upload ${failedCount} PDF${failedCount > 1 ? "s" : ""}.`,
        );
      }

      setSelectedFiles([]);
    } catch (error) {
      console.error("Error in batch upload process:", error);
      toast.error("Failed to upload PDFs. Please try again.");
    } finally {
      setUploading(false);
    }
  };

  // Check if currentChatIndex is valid
  useEffect(() => {
    // If chatHistories is empty or currentChatIndex is out of bounds
    if (
      (!chatHistories || chatHistories.length === 0) &&
      currentChatIndex !== -1
    ) {
      // Reset to new chat view
      setIsNewChat(true);
      setCurrentChatIndex(-1);
    } else if (
      chatHistories &&
      currentChatIndex >= chatHistories.length &&
      currentChatIndex !== -1
    ) {
      // If currentChatIndex is out of bounds but there are chats
      setCurrentChatIndex(chatHistories.length - 1);
    }
  }, [chatHistories, currentChatIndex]);

  // Check for the flag to select specific chat on initial render
  useEffect(() => {
    if (chatHistories && chatHistories.length > 0) {
      const chatIdToSelect = sessionStorage.getItem("selectChatId");

      if (chatIdToSelect) {
        // Find the index of the chat with this ID
        const indexToSelect = chatHistories.findIndex(
          (chat) => chat.wfRunId === chatIdToSelect,
        );

        if (indexToSelect !== -1) {
          // If found, select that chat
          setIsNewChat(false);
          setCurrentChatIndex(indexToSelect);
        } else {
          // If not found (which is unusual), select the first chat
          setIsNewChat(false);
          setCurrentChatIndex(0);
        }

        // Clear the session storage so we don't reselect on subsequent renders
        sessionStorage.removeItem("selectChatId");
      }
    }
  }, [chatHistories]);

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
              <h1 className="text-2xl font-semibold mb-2">
                Welcome to RAG Chat
              </h1>
              <p className="text-gray-400 mb-8">
                Upload documents or start asking questions about trade policies
              </p>

              {selectedFiles.length > 0 && (
                <div className="mb-8 space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-400">
                      Selected Files:
                    </span>
                    <button
                      onClick={uploadFiles}
                      disabled={uploading}
                      className="text-sm px-3 py-1 bg-[#10A37F] text-white rounded hover:bg-[#0d8e6c] disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {uploading ? "Processing..." : "Process All"}
                    </button>
                  </div>
                  <div className="space-y-2">
                    {selectedFiles.map((file, index) => (
                      <div
                        key={index}
                        className="flex items-center justify-between bg-[#1a1c23] p-2 rounded"
                      >
                        <span className="text-sm truncate">{file.name}</span>
                        <button
                          onClick={() => removeFile(index)}
                          className="p-1 hover:bg-[#2e2f35] rounded transition-colors"
                          disabled={uploading}
                        >
                          <X className="w-4 h-4 text-gray-400 hover:text-gray-200" />
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <div className="relative mb-8">
                <textarea
                  placeholder="Type your question here..."
                  className="w-full rounded-xl border border-[#2e2f35] bg-[#1a1c23] p-4 pr-24 text-gray-100 placeholder-gray-400 focus:border-[#3e3f45] focus:outline-none focus:ring-1 focus:ring-[#3e3f45]"
                  rows={4}
                  style={{ resize: "none" }}
                  value={customQuestion}
                  onChange={(e) => setCustomQuestion(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter" && !e.shiftKey) {
                      e.preventDefault();
                      handleCustomQuestionSubmit();
                    }
                  }}
                />
                <div className="absolute right-2 bottom-2 flex gap-2">
                  <input
                    type="file"
                    accept=".pdf"
                    onChange={handleFileUpload}
                    className="hidden"
                    ref={fileInputRef}
                    multiple
                  />
                  <button
                    onClick={() => fileInputRef.current?.click()}
                    disabled={uploading}
                    className="p-1 rounded-lg text-gray-400 hover:text-gray-200 hover:bg-[#2e2f35] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    title="Upload PDFs"
                  >
                    <Upload className="h-5 w-5" />
                  </button>
                  <button
                    onClick={handleCustomQuestionSubmit}
                    className="p-1 rounded-lg text-gray-400 hover:text-gray-200 hover:bg-[#2e2f35] transition-colors"
                  >
                    <Send className="h-5 w-5" />
                  </button>
                </div>
              </div>

              <p className="text-gray-400 mb-4">Or choose a sample question:</p>

              <div className="grid grid-cols-2 gap-4">
                {sampleQuestions.map((question, index) => (
                  <button
                    key={index}
                    className="text-left cursor-pointer p-4 rounded-lg bg-[#1a1c23] hover:bg-[#2e2f35] transition-colors border border-[#2e2f35] text-sm text-gray-300"
                    onClick={() => handleSampleQuestionClick(question)}
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
