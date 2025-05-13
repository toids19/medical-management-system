document.addEventListener("DOMContentLoaded", () => {
    // Toggle notifications menu
    const notificationsToggle = document.getElementById("notifications-toggle")
    const notificationsMenu = document.getElementById("notifications-menu")

    if (notificationsToggle && notificationsMenu) {
        notificationsToggle.addEventListener("click", (e) => {
            e.stopPropagation()
            notificationsMenu.classList.toggle("show")
        })

        // Close when clicking outside
        document.addEventListener("click", (e) => {
            if (!notificationsMenu.contains(e.target) && e.target !== notificationsToggle) {
                notificationsMenu.classList.remove("show")
            }
        })
    }

    // Mark single notification as read
    const markReadButtons = document.querySelectorAll(".mark-read-btn")
    markReadButtons.forEach((button) => {
        button.addEventListener("click", function (e) {
            e.stopPropagation()
            const notificationItem = this.closest(".notification-item")
            const notificationId = notificationItem.dataset.id

            fetch(`/notifications/mark-read/${notificationId}`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
            })
                .then((response) => {
                    if (response.ok) {
                        notificationItem.classList.add("read")
                        this.style.display = "none"

                        // Update badge count
                        const badge = document.querySelector(".notifications-badge")
                        if (badge) {
                            const currentCount = Number.parseInt(badge.textContent)
                            if (currentCount > 1) {
                                badge.textContent = currentCount - 1
                            } else {
                                badge.style.display = "none"
                            }
                        }
                    }
                })
                .catch((error) => console.error("Error marking notification as read:", error))
        })
    })

    // Mark all notifications as read
    const markAllReadButton = document.getElementById("mark-all-read")
    if (markAllReadButton) {
        markAllReadButton.addEventListener("click", function (e) {
            e.preventDefault()
            e.stopPropagation()

            fetch("/notifications/mark-all-read", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    // Add CSRF token if needed
                    // "X-CSRF-TOKEN": document.querySelector("meta[name='_csrf']").getAttribute("content")
                },
            })
                .then((response) => {
                    if (response.ok) {
                        // Mark all notification items as read
                        document.querySelectorAll(".notification-item").forEach((item) => {
                            item.classList.add("read")
                        })

                        // Hide all mark read buttons
                        document.querySelectorAll(".mark-read-btn").forEach((btn) => {
                            btn.style.display = "none"
                        })

                        // Hide the badge
                        const badge = document.querySelector(".notifications-badge")
                        if (badge) {
                            badge.style.display = "none"
                        }

                        // Hide the mark all read button itself
                        this.style.display = "none"

                        // Reload the page to reflect changes
                        window.location.reload()
                    }
                })
                .catch((error) => console.error("Error marking all notifications as read:", error))
        })
    }

    // Click on notification item to navigate
    document.querySelectorAll(".notification-item").forEach((item) => {
        item.addEventListener("click", function () {
            const link = this.dataset.link
            if (link) {
                window.location.href = link
            }
        })
    })

    // Generate test notifications - keep this functionality but hide the button
    const generateButton = document.getElementById("generate-notifications")
    if (generateButton) {
        generateButton.addEventListener("click", () => {
            fetch("/notifications/generate-sample", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
            })
                .then((response) => {
                    if (response.ok) {
                        // Reload the page to show new notifications
                        window.location.reload()
                    }
                })
                .catch((error) => console.error("Error generating notifications:", error))
        })
    }
})

