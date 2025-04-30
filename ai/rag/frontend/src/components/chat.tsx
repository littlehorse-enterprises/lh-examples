'use client'

import { postChatMessage, uploadPdf } from '@/app/actions'
import {
  ChatHandler,
  Message,
} from '@llamaindex/chat-ui'

import '@llamaindex/chat-ui/styles/markdown.css'
import { Bot, Send, Upload, User } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'

export function ChatSection({ chatHistory, wfRunId }: { chatHistory: string[], wfRunId: string }) {
  const handler = useChat(chatHistory, wfRunId)
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const [uploading, setUploading] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" })
  }

  useEffect(() => {
    scrollToBottom()
  }, [handler.messages])

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    try {
      setUploading(true)
      const formData = new FormData()
      formData.append('file', file)
      await uploadPdf(formData)
    } catch (error) {
      console.error('Error uploading file:', error)
      alert('Failed to upload PDF. Please try again.')
    } finally {
      setUploading(false)
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
    }
  }

  return (
    <div className="flex flex-col h-full w-full bg-[#0f1117] text-gray-100">
      <div className="flex-1 overflow-y-auto">
        <div className="max-w-3xl mx-auto">
          {handler.messages.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-[calc(100vh-180px)] text-gray-400">
              <h1 className="text-2xl font-semibold mb-2">ChatGPT</h1>
              <p className="text-sm">How can I help you today?</p>
            </div>
          ) : (
            handler.messages.map((message, index) => (
              <div
                key={index}
                className={`py-6 px-4 ${message.role === 'assistant' ? 'bg-[#1a1c23]' : ''}`}
              >
                <div className="max-w-3xl mx-auto flex gap-4 items-start">
                  <div className={`w-8 h-8 rounded-sm flex items-center justify-center ${
                    message.role === 'assistant' ? 'bg-[#10A37F]' : 'bg-[#5437DB]'
                  }`}>
                    {message.role === 'assistant' ? (
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
      </div>
      <div className="border-t border-[#2e2f35] bg-[#0f1117] p-4">
        <div className="max-w-3xl mx-auto">
          <div className="relative">
            <textarea
              value={handler.input}
              onChange={(e) => handler.setInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault()
                  if (handler.input.trim()) {
                    handler.append({ role: 'user', content: handler.input })
                    handler.setInput('')
                  }
                }
              }}
              placeholder="Message ChatGPT..."
              className="w-full rounded-xl border border-[#2e2f35] bg-[#1a1c23] p-4 pr-24 text-gray-100 placeholder-gray-400 focus:border-[#3e3f45] focus:outline-none focus:ring-1 focus:ring-[#3e3f45]"
              rows={1}
              style={{ resize: 'none', minHeight: '56px' }}
            />
            <div className="absolute right-3 top-[13px] flex gap-2">
              <input
                type="file"
                accept=".pdf"
                onChange={handleFileUpload}
                className="hidden"
                ref={fileInputRef}
              />
              <button
                onClick={() => fileInputRef.current?.click()}
                disabled={uploading}
                className="p-1 rounded-lg text-gray-400 hover:text-gray-200 hover:bg-[#2e2f35] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                title="Upload PDF"
              >
                <Upload className="h-5 w-5" />
              </button>
              <button
                onClick={() => {
                  if (handler.input.trim()) {
                    handler.append({ role: 'user', content: handler.input })
                    handler.setInput('')
                  }
                }}
                className="p-1 rounded-lg text-gray-400 hover:text-gray-200 hover:bg-[#2e2f35] transition-colors"
              >
                <Send className="h-5 w-5" />
              </button>
            </div>
          </div>
          <p className="mt-2 text-center text-xs text-gray-500">
            ChatGPT can make mistakes. Consider checking important information.
          </p>
        </div>
      </div>
    </div>
  )
}

function useChat(chatHistory: string[], wfRunId: string): ChatHandler {
  const [messages, setMessages] = useState<Message[]>(chatHistory.map((message, index) => ({
    content: message,
    role: index % 2 === 0 ? 'user' : 'assistant'
  })))

  const [input, setInput] = useState('')

  useEffect(() => {
    setMessages(chatHistory.map((message, index) => ({
      content: message,
      role: index % 2 === 0 ? 'user' : 'assistant'
    })))
  }, [chatHistory])

  const append = async (message: Message) => {
    setMessages(prev => [...prev, message])

    console.log(message)

    if (message.role === 'user') {
      console.log('posting message')
      postChatMessage(message.content, wfRunId)
    }

    return message.content
  }

  return {
    messages,
    input,
    setInput,
    isLoading: false,
    append,
  }
}
