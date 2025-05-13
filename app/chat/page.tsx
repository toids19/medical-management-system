"use client"

import { useState, useEffect, useRef } from "react"
import { useWebSocket } from "@/lib/useWebSocket"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card } from "@/components/ui/card"
import { Loader2, Send, MessageSquare, AlertCircle, RefreshCw } from "lucide-react"
import { Alert, AlertDescription } from "@/components/ui/alert"

export default function ChatPage() {
    const { messages, send, connected, error } = useWebSocket()
    const [input, setInput] = useState("")
    const [isLoading, setIsLoading] = useState(false)
    const [aiMessages, setAiMessages] = useState<{ sender: string; content: string; timestamp?: string }[]>([])
    const messagesEndRef = useRef<HTMLDivElement | null>(null)

    // Handle AI responses separately from regular messages
    useEffect(() => {
        if (!messages.length) return

        // Process new messages
        const newAiMessages = messages
            .filter((m) => {
                try {
                    const parsed = JSON.parse(m.body)
                    return parsed.type === "AI_RESPONSE" || parsed.type === "AI_QUESTION"
                } catch (e) {
                    console.error("Error parsing message:", e, m.body)
                    return false
                }
            })
            .map((m) => {
                try {
                    return JSON.parse(m.body)
                } catch (e) {
                    console.error("Error parsing AI message:", e)
                    return null
                }
            })
            .filter(Boolean)

        if (newAiMessages.length > 0) {
            console.log("New AI messages:", newAiMessages)
            setIsLoading(false)

            // Update AI messages, avoiding duplicates
            setAiMessages((prev) => {
                const existingMap = new Map(prev.map((m) => [`${m.sender}-${m.content}-${m.timestamp || ""}`, true]))

                const uniqueNewMessages = newAiMessages.filter(
                    (m) => !existingMap.has(`${m.sender}-${m.content}-${m.timestamp || ""}`),
                )

                return [...prev, ...uniqueNewMessages]
            })
        }
    }, [messages])

    // Scroll to bottom when messages change
    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" })
    }, [aiMessages])

    const handleAskAI = () => {
        const text = input.trim()
        if (!text) return

        // Add user question to AI messages
        setAiMessages((prev) => [
            ...prev,
            {
                sender: "You",
                content: text,
                timestamp: new Date().toISOString(),
            },
        ])

        // Set loading state
        setIsLoading(true)

        // Send message with AI_QUESTION type
        send(text, "AI_QUESTION")
        setInput("")
    }

    const handleRefresh = () => {
        window.location.reload()
    }

    return (
        <div className="flex flex-col h-screen max-h-screen bg-slate-50">
            <div className="p-4 border-b bg-white shadow-sm">
                <h1 className="text-xl font-bold">Medical Management System Chat</h1>
                <p className="text-sm text-slate-500">
                    {connected ? (
                        <span className="text-green-500">Connected to chat server</span>
                    ) : (
                        <span className="text-red-500">Disconnected from chat server</span>
                    )}
                </p>
            </div>

            <div className="flex-1 overflow-hidden flex p-4 gap-4 flex-col">
                {error && (
                    <Alert variant="destructive" className="mb-4">
                        <AlertCircle className="h-4 w-4" />
                        <AlertDescription className="flex justify-between items-center">
                            <span>{error}</span>
                            <Button variant="outline" size="sm" onClick={handleRefresh}>
                                <RefreshCw className="h-4 w-4 mr-2" />
                                Refresh
                            </Button>
                        </AlertDescription>
                    </Alert>
                )}

                {/* AI Assistant */}
                <Card className="flex-1 flex flex-col overflow-hidden shadow-md">
                    <div className="p-4 border-b bg-gradient-to-r from-blue-500 to-blue-600">
                        <h2 className="text-lg font-semibold text-white flex items-center">
                            <MessageSquare className="mr-2 h-5 w-5" />
                            AI Medical Assistant
                        </h2>
                    </div>
                    <div className="flex-1 overflow-y-auto p-4 space-y-3 bg-white">
                        {aiMessages.length === 0 && !isLoading && (
                            <div className="text-center text-slate-500 mt-8">
                                <p>Ask the AI assistant a medical question</p>
                            </div>
                        )}

                        {aiMessages.map((m, i) => (
                            <Card
                                key={i}
                                className={`p-3 ${
                                    m.sender === "AI Assistant" ? "bg-blue-50 border-blue-100" : "bg-gray-50 border-gray-100"
                                }`}
                            >
                                <div className="font-semibold mb-1">{m.sender === "AI Assistant" ? "AI Assistant" : "You"}</div>
                                <div>{m.content}</div>
                                <div className="text-xs text-slate-500 mt-1">
                                    {m.timestamp ? new Date(m.timestamp).toLocaleTimeString() : ""}
                                </div>
                            </Card>
                        ))}

                        {isLoading && (
                            <div className="flex items-center space-x-2 p-3 bg-blue-50 rounded border border-blue-100">
                                <Loader2 className="h-4 w-4 animate-spin text-blue-500" />
                                <span>AI is thinking...</span>
                            </div>
                        )}

                        <div ref={messagesEndRef} />
                    </div>
                    <div className="p-4 border-t bg-gray-50 flex">
                        <Input
                            type="text"
                            className="flex-1 mr-2"
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            onKeyDown={(e) => e.key === "Enter" && !e.shiftKey && handleAskAI()}
                            placeholder="Ask the AI assistant..."
                            disabled={!connected || isLoading}
                        />
                        <Button
                            onClick={handleAskAI}
                            disabled={!connected || isLoading || !input.trim()}
                            className="bg-blue-600 hover:bg-blue-700"
                        >
                            {isLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Send className="h-4 w-4" />}
                        </Button>
                    </div>
                </Card>
            </div>
        </div>
    )
}
