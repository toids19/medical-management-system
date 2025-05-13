// Chat functionality
let stompClient = null
let username = null
let selectedUser = null
let activeTab = "chatTab"
let userRole = null

// Connect to WebSocket
function connect() {
    // Get username from meta tag or generate a random one
    username =
        document.querySelector('meta[name="username"]')?.content ||
        document.querySelector(".user-info")?.textContent?.trim() ||
        "User_" + Math.floor(Math.random() * 1000)

    // Get user role from meta tag
    userRole = document.querySelector('meta[name="userRole"]')?.content || "ROLE_USER"

    const socket = new SockJS("/ws")
    stompClient = Stomp.over(socket)

    stompClient.connect(
        {},
        (frame) => {
            console.log("Connected: " + frame)

            // Subscribe to public chat
            stompClient.subscribe("/topic/public", (message) => {
                showMessage(JSON.parse(message.body), "messageArea")

                // Show new message indicator if not on public chat tab
                if (activeTab !== "chatTab") {
                    document.querySelector("button[onclick=\"openTab('chatTab')\"]").classList.add("has-new")
                }
            })

            // Subscribe to private messages
            stompClient.subscribe("/user/queue/private", (message) => {
                const receivedMessage = JSON.parse(message.body)

                // Show in private chat if sender is selected user or message is from current user
                if (selectedUser === receivedMessage.sender || selectedUser === receivedMessage.recipient) {
                    showMessage(receivedMessage, "privateMessageArea")
                }

                // Show new message indicator if not on private chat tab
                if (activeTab !== "privateTab") {
                    document.querySelector("button[onclick=\"openTab('privateTab')\"]").classList.add("has-new")
                }
            })

            // Subscribe to announcements
            stompClient.subscribe("/topic/announcements", (message) => {
                showAnnouncement(JSON.parse(message.body))

                // Show new message indicator if not on announcements tab
                if (activeTab !== "announcementsTab") {
                    document.querySelector("button[onclick=\"openTab('announcementsTab')\"]").classList.add("has-new")
                }
            })

            // Subscribe to user status updates
            stompClient.subscribe("/topic/users", (message) => {
                updateUserList(JSON.parse(message.body))
            })

            // Subscribe to AI responses
            stompClient.subscribe("/user/queue/ai-responses", (message) => {
                const aiResponse = JSON.parse(message.body)

                // Remove loading indicator
                const loadingElements = document.querySelectorAll("#aiMessageArea .ai-message.loading")
                if (loadingElements.length > 0) {
                    loadingElements.forEach((el) => el.remove())
                }

                // Show AI response
                const aiResponseElement = document.createElement("div")
                aiResponseElement.classList.add("ai-message")
                aiResponseElement.innerHTML = `<p>${aiResponse.content}</p>`

                document.getElementById("aiMessageArea").appendChild(aiResponseElement)
                document.getElementById("aiMessageArea").scrollTop = document.getElementById("aiMessageArea").scrollHeight
            })

            // Send join message
            stompClient.send(
                "/app/chat.addUser",
                {},
                JSON.stringify({
                    sender: username,
                    type: "JOIN",
                    timestamp: new Date().toISOString(),
                }),
            )

            // Request current user list
            fetchActiveUsers()
        },
        (error) => {
            console.error("Error connecting to WebSocket:", error)
            showNotification("Could not connect to chat server. Please try again later.")
        },
    )
}

// Fetch active users
function fetchActiveUsers() {
    fetch("/api/users/active")
        .then((response) => response.json())
        .then((users) => {
            updateUserList(users)
        })
        .catch((error) => {
            console.error("Error fetching active users:", error)
        })
}

// Send public message
function sendMessage() {
    const messageInput = document.getElementById("message")
    const messageContent = messageInput.value.trim()

    if (messageContent && stompClient) {
        const chatMessage = {
            sender: username,
            content: messageContent,
            type: "CHAT",
            timestamp: new Date().toISOString(),
        }

        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage))
        messageInput.value = ""
    }
}

// Send private message
function sendPrivateMessage() {
    const messageInput = document.getElementById("privateMessage")
    const messageContent = messageInput.value.trim()

    if (messageContent && stompClient && selectedUser) {
        const chatMessage = {
            sender: username,
            content: messageContent,
            recipient: selectedUser,
            type: "PRIVATE",
            timestamp: new Date().toISOString(),
        }

        stompClient.send("/app/chat.sendPrivateMessage", {}, JSON.stringify(chatMessage))
        messageInput.value = ""
    }
}

// Send announcement
function sendAnnouncement() {
    const announcementInput = document.getElementById("announcement")
    const announcementContent = announcementInput.value.trim()

    if (announcementContent && stompClient) {
        const announcement = {
            sender: username,
            content: announcementContent,
            type: "ANNOUNCEMENT",
            timestamp: new Date().toISOString(),
        }

        stompClient.send("/app/chat.sendAnnouncement", {}, JSON.stringify(announcement))
        announcementInput.value = ""
    }
}

// Send message to AI assistant
function askAI() {
    const aiMessageInput = document.getElementById("aiMessage")
    const question = aiMessageInput.value.trim()

    if (question) {
        // Show user's question
        const aiMessageArea = document.getElementById("aiMessageArea")
        const userMessageElement = document.createElement("div")
        userMessageElement.classList.add("message", "sent")

        userMessageElement.innerHTML = `
            <div class="message-content">${question}</div>
            <div class="message-time">${formatTime(new Date())}</div>
        `

        aiMessageArea.appendChild(userMessageElement)
        aiMessageArea.scrollTop = aiMessageArea.scrollHeight

        // Show loading indicator
        const loadingElement = document.createElement("div")
        loadingElement.classList.add("ai-message", "loading")
        loadingElement.innerHTML = "<p>Thinking...</p>"
        aiMessageArea.appendChild(loadingElement)
        aiMessageArea.scrollTop = aiMessageArea.scrollHeight

        // Send to server
        const chatMessage = {
            sender: username,
            content: question,
            type: "AI_QUESTION",
            timestamp: new Date().toISOString(),
        }

        stompClient.send("/app/chat.ai", {}, JSON.stringify(chatMessage))
        aiMessageInput.value = ""
    }
}

// Display message in specified container
function showMessage(message, containerId) {
    const messageContainer = document.getElementById(containerId)

    if (!messageContainer) return

    if (message.type === "JOIN") {
        showNotification(`${message.sender} a intrat în chat`, containerId)
    } else if (message.type === "LEAVE") {
        showNotification(`${message.sender} a părăsit chatul`, containerId)
    } else {
        const messageElement = document.createElement("div")
        const isSentByMe = message.sender === username || message.isSentByMe

        messageElement.classList.add("message", isSentByMe ? "sent" : "received")

        let messageTime
        try {
            // Try to parse the timestamp
            messageTime = formatTime(new Date(message.timestamp || message.time))
        } catch (e) {
            // If parsing fails, use current time
            messageTime = formatTime(new Date())
        }

        const messageContent = `
            ${!isSentByMe ? `<div class="message-sender">${message.sender}</div>` : ""}
            <div class="message-content">${message.content}</div>
            <div class="message-time">${messageTime}</div>
        `

        messageElement.innerHTML = messageContent
        messageContainer.appendChild(messageElement)
    }

    messageContainer.scrollTop = messageContainer.scrollHeight
}

// Display announcement
function showAnnouncement(announcement) {
    const announcementArea = document.getElementById("announcementArea")
    if (!announcementArea) return

    const announcementElement = document.createElement("div")

    announcementElement.classList.add("announcement")

    let announcementTime
    try {
        // Try to parse the timestamp
        announcementTime = formatTime(new Date(announcement.timestamp || announcement.time))
    } catch (e) {
        // If parsing fails, use current time
        announcementTime = formatTime(new Date())
    }

    announcementElement.innerHTML = `
        <div class="sender">${announcement.sender}</div>
        <div class="content">${announcement.content}</div>
        <div class="time">${announcementTime}</div>
    `

    announcementArea.appendChild(announcementElement)
    announcementArea.scrollTop = announcementArea.scrollHeight
}

// Display notification
function showNotification(message, containerId = "messageArea") {
    const container = document.getElementById(containerId)
    if (!container) return

    const notificationElement = document.createElement("div")

    notificationElement.classList.add("notification")
    notificationElement.textContent = message

    container.appendChild(notificationElement)
    container.scrollTop = container.scrollHeight
}

// Update user list
function updateUserList(users) {
    const userListContainer = document.getElementById("userList")
    const recipientSelect = document.getElementById("recipientSelect")

    if (!userListContainer || !recipientSelect) return

    // Clear current lists
    userListContainer.innerHTML = ""

    // Keep only the first option in the select
    const firstOption = recipientSelect.options[0]
    recipientSelect.innerHTML = ""
    recipientSelect.appendChild(firstOption)

    // Add users to both lists
    users.forEach((user) => {
        if (user === username || user.username === username) return

        const userDisplayName = user.username || user

        // Add to sidebar
        const userElement = document.createElement("div")
        userElement.classList.add("user-item")
        userElement.setAttribute("data-username", userDisplayName)

        userElement.innerHTML = `
            <div class="user-avatar">
                ${userDisplayName.charAt(0).toUpperCase()}
                <span class="user-status online"></span>
            </div>
            <div class="user-details">
                <div class="user-name">${userDisplayName}</div>
                <div class="user-role">Online</div>
            </div>
        `

        userElement.addEventListener("click", () => {
            selectUser(userDisplayName)
        })

        userListContainer.appendChild(userElement)

        // Add to dropdown
        const option = document.createElement("option")
        option.value = userDisplayName
        option.textContent = userDisplayName
        recipientSelect.appendChild(option)
    })
}

// Select user for private chat
function selectUser(username) {
    selectedUser = username

    // Update UI
    document.querySelectorAll(".user-item").forEach((item) => {
        item.classList.remove("active")
    })

    const userItem = document.querySelector(`.user-item[data-username="${username}"]`)
    if (userItem) {
        userItem.classList.add("active")
    }

    // Update dropdown
    document.getElementById("recipientSelect").value = username

    // Enable private messaging
    document.getElementById("privateMessage").disabled = false
    document.getElementById("sendPrivateButton").disabled = false

    // Switch to private tab
    openTab("privateTab")

    // Clear "new message" indicator
    document.querySelector("button[onclick=\"openTab('privateTab')\"]").classList.remove("has-new")

    // Clear private message area
    document.getElementById("privateMessageArea").innerHTML = ""

    // Load previous messages
    loadPrivateMessages(username)
}

// Load private messages
function loadPrivateMessages(recipient) {
    fetch(`/api/chat/private/${recipient}`)
        .then((response) => response.json())
        .then((messages) => {
            const privateMessageArea = document.getElementById("privateMessageArea")
            privateMessageArea.innerHTML = ""

            if (messages.length === 0) {
                const emptyMessage = document.createElement("div")
                emptyMessage.classList.add("center-message")
                emptyMessage.textContent = "No previous messages"
                privateMessageArea.appendChild(emptyMessage)
            } else {
                messages.forEach((message) => {
                    showMessage(
                        {
                            ...message,
                            isSentByMe: message.sender === username,
                        },
                        "privateMessageArea",
                    )
                })
            }
        })
        .catch((error) => {
            console.error("Error loading private messages:", error)
            showNotification("Could not load previous messages", "privateMessageArea")
        })
}

// Switch between tabs
function openTab(tabId) {
    // Hide all tabs
    document.querySelectorAll(".tab-content").forEach((tab) => {
        tab.classList.remove("active")
    })

    // Show selected tab
    document.getElementById(tabId).classList.add("active")

    // Update active tab buttons
    document.querySelectorAll(".tab-button").forEach((button) => {
        button.classList.remove("active")
    })

    document.querySelector(`button[onclick="openTab('${tabId}')"]`).classList.add("active")

    // Remove new message indicator
    document.querySelector(`button[onclick="openTab('${tabId}')"]`).classList.remove("has-new")

    // Update active tab
    activeTab = tabId
}

// Format time
function formatTime(date) {
    if (!(date instanceof Date) || isNaN(date)) {
        return "Just now"
    }
    return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
}

// Event listeners
document.addEventListener("DOMContentLoaded", () => {
    //Import SockJS and Stomp
    const SockJS = window.SockJS
    const Stomp = window.Stomp

    // Connect to WebSocket
    connect()

    // Send message on button click
    document.getElementById("sendButton")?.addEventListener("click", sendMessage)

    // Send message on Enter key
    document.getElementById("message")?.addEventListener("keypress", (e) => {
        if (e.key === "Enter") {
            sendMessage()
        }
    })

    // Send private message on button click
    document.getElementById("sendPrivateButton")?.addEventListener("click", sendPrivateMessage)

    // Send private message on Enter key
    document.getElementById("privateMessage")?.addEventListener("keypress", (e) => {
        if (e.key === "Enter") {
            sendPrivateMessage()
        }
    })

    // Send announcement on button click
    const announceButton = document.getElementById("announceButton")
    if (announceButton) {
        announceButton.addEventListener("click", sendAnnouncement)
    }

    // Send announcement on Enter key
    const announcementInput = document.getElementById("announcement")
    if (announcementInput) {
        announcementInput.addEventListener("keypress", (e) => {
            if (e.key === "Enter") {
                sendAnnouncement()
            }
        })
    }

    // Ask AI on button click
    document.getElementById("askAiButton")?.addEventListener("click", askAI)

    // Ask AI on Enter key
    document.getElementById("aiMessage")?.addEventListener("keypress", (e) => {
        if (e.key === "Enter") {
            askAI()
        }
    })

    // Handle recipient selection
    document.getElementById("recipientSelect")?.addEventListener("change", function () {
        selectUser(this.value)
    })

    // Handle window close/refresh
    window.addEventListener("beforeunload", () => {
        if (stompClient) {
            stompClient.send(
                "/app/chat.removeUser",
                {},
                JSON.stringify({
                    sender: username,
                    type: "LEAVE",
                    timestamp: new Date().toISOString(),
                }),
            )
        }
    })
})
