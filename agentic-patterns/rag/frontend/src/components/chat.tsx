"use client";

import { createNewChat, postChatMessage, uploadPdf } from "@/app/actions";
import { ChatHandler, Message } from "@llamaindex/chat-ui";

import "@llamaindex/chat-ui/styles/markdown.css";
import { Bot, Send, Upload, User, X } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { toast } from "sonner";

export function ChatSection({
  chatHistory,
  wfRunId,
  isNewChat = false,
  onFirstMessage,
}: {
  chatHistory: string[];
  wfRunId: string;
  isNewChat?: boolean;
  onFirstMessage?: () => void;
}) {
  const handler = useChat(chatHistory, wfRunId, isNewChat, onFirstMessage);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [handler.messages]);

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
      for (const file of selectedFiles) {
        const formData = new FormData();
        formData.append("file", file);
        await uploadPdf(formData);
      }

      toast.success("PDFs have been uploaded and currently being processed.");

      setSelectedFiles([]); // Clear files after successful upload
    } catch (error) {
      console.error("Error uploading files:", error);
      toast.error("Failed to upload PDFs. Please try again.");
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="flex flex-col h-full w-full bg-[#0f1117] text-gray-100">
      <div className="flex-1 overflow-y-auto">
        {handler.messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-[calc(100vh-180px)] text-gray-400">
            <h1 className="text-2xl font-semibold mb-2">RAG Chat</h1>
            <p className="text-sm">How can I help you today?</p>
          </div>
        ) : (
          handler.messages.map((message, index) => (
            <div
              key={index}
              className={`py-6 px-4 ${
                message.role === "assistant" ? "bg-[#1a1c23]" : ""
              }`}
            >
              <div className="max-w-3xl mx-auto flex gap-4 items-start">
                <div
                  className={`w-8 h-8 rounded-sm flex items-center justify-center ${
                    message.role === "assistant"
                      ? "bg-[#10A37F]"
                      : "bg-[#5437DB]"
                  }`}
                >
                  {message.role === "assistant" ? (
                    <Bot className="w-5 h-5 text-white" />
                  ) : (
                    <User className="w-5 h-5 text-white" />
                  )}
                </div>
                <div className="prose prose-invert flex-1 prose-p:leading-relaxed">
                  {message.content}
                </div>
              </div>
            </div>
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      <div className="max-w-3xl mx-auto">
        {selectedFiles.length > 0 && (
          <div className="mb-4 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-400">Selected Files:</span>
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
        <div className="relative">
          <textarea
            value={handler.input}
            onChange={(e) => handler.setInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter" && !e.shiftKey) {
                e.preventDefault();
                if (handler.input.trim()) {
                  handler.append({ role: "user", content: handler.input });
                  handler.setInput("");
                }
              }
            }}
            placeholder="Type your message here..."
            className="w-full rounded-xl border border-[#2e2f35] bg-[#1a1c23] p-4 pr-24 text-gray-100 placeholder-gray-400 focus:border-[#3e3f45] focus:outline-none focus:ring-1 focus:ring-[#3e3f45]"
            rows={1}
            style={{ resize: "none", minHeight: "56px" }}
          />

          <div className="absolute inset-y-0 right-0 flex items-center justify-center pr-3 gap-2">
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
              onClick={() => {
                if (handler.input.trim()) {
                  handler.append({ role: "user", content: handler.input });
                  handler.setInput("");
                }
              }}
              className="p-1 rounded-lg text-gray-400 hover:text-gray-200 hover:bg-[#2e2f35] transition-colors"
            >
              <Send className="h-5 w-5" />
            </button>
          </div>
        </div>
        <p className="mt-2 text-center text-xs text-gray-500">
          AI responses may contain inaccuracies. Verify important information.
        </p>
      </div>
    </div>
  );
}

function useChat(
  chatHistory: string[],
  wfRunId: string,
  isNewChat: boolean = false,
  onFirstMessage?: () => void,
): ChatHandler {
  const [messages, setMessages] = useState<Message[]>(
    chatHistory.map((message, index) => ({
      content: message,
      role: index % 2 === 0 ? "user" : "assistant",
    })),
  );

  const [input, setInput] = useState("");
  const [isFirstMessage, setIsFirstMessage] = useState(true);

  useEffect(() => {
    setMessages(
      chatHistory.map((message, index) => ({
        content: message,
        role: index % 2 === 0 ? "user" : "assistant",
      })),
    );
  }, [chatHistory]);

  // Check for prefill question on mount
  useEffect(() => {
    const prefillQuestion = localStorage.getItem("prefillQuestion");
    if (prefillQuestion) {
      setInput(prefillQuestion);
      localStorage.removeItem("prefillQuestion");
    }
  }, []);

  const append = async (message: Message) => {
    setMessages((prev) => [...prev, message]);

    if (message.role === "user") {
      if (isNewChat && isFirstMessage) {
        try {
          // Create new chat with first message
          const newWfRunId = await createNewChat(message.content);
          // Update the wfRunId for subsequent messages
          wfRunId = newWfRunId;
          setIsFirstMessage(false);
          onFirstMessage?.();
        } catch (error) {
          console.error("Error creating new chat:", error);
          toast.error("Failed to create new chat");
          return;
        }
      } else {
        // Post message to existing chat
        await postChatMessage(message.content, wfRunId);
      }
    }

    return message.content;
  };

  return {
    messages,
    input,
    setInput,
    isLoading: false,
    append,
  };
}
