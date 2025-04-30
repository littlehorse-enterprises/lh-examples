'use client'

import { postChatMessage } from '@/app/actions'
import {
  ChatHandler,
  ChatSection as ChatSectionUI,
  Message,
} from '@llamaindex/chat-ui'

import '@llamaindex/chat-ui/styles/markdown.css'
import { useState, useEffect } from 'react'

export function ChatSection({ chatHistory, wfRunId }: { chatHistory: string[], wfRunId: string }) {
  // You can replace the handler with a useChat hook from Vercel AI SDK
  const handler = useChat(chatHistory, wfRunId)

  return (
    <div className="flex max-h-[80vh] flex-col gap-6 overflow-y-auto">
      <ChatSectionUI handler={handler} />
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
