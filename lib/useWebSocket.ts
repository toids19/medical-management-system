"use client"

import { useEffect, useRef, useState, useCallback } from "react"
import { Client, type IMessage } from "@stomp/stompjs"
import SockJS from "sockjs-client"

export function useWebSocket() {
    const [messages, setMessages] = useState<IMessage[]>([])
    const [connected, setConnected] = useState(false)
    const stompClientRef = useRef<Client | null>(null)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        // Create and configure the STOMP client
        const client = new Client({
            // Use SockJS for the WebSocket connection
            webSocketFactory: () => new SockJS("http://localhost:8080/ws"),

            // Configure the client
            connectHeaders: {},
            debug: (str) => {
                console.log("STOMP Debug:", str)
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,

            // Handle successful connection
            onConnect: () => {
                console.log("Connected to STOMP broker")
                setConnected(true)
                setError(null)

                // Subscribe to public messages
                client.subscribe("/topic/public", (message) => {
                    console.log("Received public message:", message)
                    setMessages((prev) => [...prev, message])
                })

                // Subscribe to AI responses - this is the key subscription
                client.subscribe("/user/queue/ai-responses", (message) => {
                    console.log("Received AI response:", message)
                    setMessages((prev) => [...prev, message])
                })

                // Subscribe to private messages
                client.subscribe("/user/queue/private", (message) => {
                    console.log("Received private message:", message)
                    setMessages((prev) => [...prev, message])
                })

                // Subscribe to debug messages
                client.subscribe("/topic/ai-debug", (message) => {
                    console.log("AI Debug:", message.body)
                })

                // Send a join message to register the user
                const username = localStorage.getItem("username") || `User_${Math.floor(Math.random() * 1000)}`
                localStorage.setItem("username", username)

                client.publish({
                    destination: "/app/chat.addUser",
                    body: JSON.stringify({
                        sender: username,
                        type: "JOIN",
                        content: username + " joined the chat",
                        timestamp: new Date().toISOString(),
                    }),
                })
            },

            // Handle connection errors
            onStompError: (frame) => {
                console.error("STOMP Error:", frame.headers["message"], frame.body)
                setError(`Connection error: ${frame.headers["message"]}`)
            },

            // Handle disconnection
            onDisconnect: () => {
                console.log("Disconnected from STOMP broker")
                setConnected(false)
            },
        })

        // Store the client in the ref
        stompClientRef.current = client

        // Activate the connection
        try {
            client.activate()
            console.log("Activating STOMP client...")
        } catch (err) {
            console.error("Error activating STOMP client:", err)
            setError(`Connection error: ${err instanceof Error ? err.message : String(err)}`)
        }

        // Clean up on unmount
        return () => {
            console.log("Cleaning up STOMP client...")
            if (client.active) {
                client.deactivate()
            }
        }
    }, [])

    // Function to send messages
    const send = useCallback((text: string, type = "CHAT") => {
        const client = stompClientRef.current
        if (!client?.active) {
            console.error("Cannot send message, STOMP client not active")
            setError("Connection lost. Please refresh the page.")
            return
        }

        const username = localStorage.getItem("username") || `User_${Math.floor(Math.random() * 1000)}`

        // Determine the destination based on message type
        let destination = "/app/chat.sendMessage"
        if (type === "AI_QUESTION") {
            destination = "/app/chat.ai"
            console.log("Sending AI question:", text)
        }

        // Send the message
        try {
            client.publish({
                destination: destination,
                body: JSON.stringify({
                    sender: username,
                    content: text,
                    type: type,
                    timestamp: new Date().toISOString(),
                }),
            })
            console.log("Message sent to", destination)
        } catch (err) {
            console.error("Error sending message:", err)
            setError(`Failed to send message: ${err instanceof Error ? err.message : String(err)}`)
        }
    }, [])

    return { messages, connected, send, error }
}
