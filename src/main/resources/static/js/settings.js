document.addEventListener("DOMContentLoaded", () => {
  // Theme buttons
  const themeButtons = document.querySelectorAll(".theme-btn")

  // Load saved theme preference
  const savedTheme = localStorage.getItem("theme") || "light"
  setActiveTheme(savedTheme)

  // Theme button click handlers - only select the theme, don't apply it immediately
  document.getElementById("theme-light").addEventListener("click", () => {
    setActiveTheme("light")
  })

  document.getElementById("theme-dark").addEventListener("click", () => {
    setActiveTheme("dark")
  })

  document.getElementById("theme-system").addEventListener("click", () => {
    setActiveTheme("system")
  })

  function setActiveTheme(theme) {
    // Remove active class from all theme buttons
    document.getElementById("theme-light").classList.remove("active")
    document.getElementById("theme-dark").classList.remove("active")
    document.getElementById("theme-system").classList.remove("active")

    // Reset borders
    document.getElementById("theme-light").style.border = "1px solid #ced4da"
    document.getElementById("theme-dark").style.border = "1px solid #ced4da"
    document.getElementById("theme-system").style.border = "1px solid #ced4da"

    // Add active class to selected theme
    const activeButton = document.getElementById(`theme-${theme}`)
    if (activeButton) {
      activeButton.classList.add("active")
      activeButton.style.border = "2px solid #4CAF50"
    }
  }

  function applyTheme(theme) {
    // Save theme to localStorage
    localStorage.setItem("theme", theme)

    // Apply selected theme
    if (theme === "dark") {
      document.documentElement.setAttribute("data-theme", "dark")
    } else if (theme === "system") {
      const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches
      document.documentElement.setAttribute("data-theme", prefersDark ? "dark" : "light")
    } else {
      document.documentElement.setAttribute("data-theme", "light")
    }
  }

  // Language selector
  const languageSelect = document.getElementById("language")

  // Load saved language preference from cookie
  const savedLanguage = getCookie("app-locale") || "ro"
  languageSelect.value = savedLanguage

  // Items per page
  const itemsPerPageSelect = document.getElementById("items-per-page")

  // Load saved items per page preference
  const savedItemsPerPage = localStorage.getItem("itemsPerPage") || "25"
  itemsPerPageSelect.value = savedItemsPerPage

  itemsPerPageSelect.addEventListener("change", function () {
    // Just store the selection, don't apply it yet
    localStorage.setItem("itemsPerPage", this.value)
  })

  // Form submissions
  document.getElementById("account-form").addEventListener("submit", (e) => {
    e.preventDefault()

    const email = document.getElementById("email").value
    const currentPassword = document.getElementById("current-password").value
    const newPassword = document.getElementById("new-password").value
    const confirmPassword = document.getElementById("confirm-password").value

    if (newPassword && newPassword !== confirmPassword) {
      alert("Parolele noi nu se potrivesc!")
      return
    }

    // Here you would typically send this data to the server
    // For demo purposes, we'll just show a success message
    alert("Setările contului au fost salvate cu succes!")
  })

  document.getElementById("notification-form").addEventListener("submit", (e) => {
    e.preventDefault()

    const emailNotifications = document.getElementById("email-notifications").checked
    const systemNotifications = document.getElementById("system-notifications").checked
    const medicationAlerts = document.getElementById("medication-alerts").checked

    // Save preferences to localStorage
    localStorage.setItem("emailNotifications", emailNotifications)
    localStorage.setItem("systemNotifications", systemNotifications)
    localStorage.setItem("medicationAlerts", medicationAlerts)

    alert("Preferințele de notificare au fost salvate cu succes!")
  })

  document.getElementById("display-form").addEventListener("submit", (e) => {
    e.preventDefault()

    // Get the selected theme
    let selectedTheme = "light"
    if (document.getElementById("theme-dark").classList.contains("active")) {
      selectedTheme = "dark"
    } else if (document.getElementById("theme-system").classList.contains("active")) {
      selectedTheme = "system"
    }

    // Apply the theme
    applyTheme(selectedTheme)

    // Get the selected language
    const language = languageSelect.value

    // Get the current language from cookie
    const currentLanguage = getCookie("app-locale") || "ro"

    // Only redirect if language has changed
    if (language !== currentLanguage) {
      // Direct language change - this is the most reliable method
      window.location.href = `/change-language?lang=${language}&redirect=/`
    } else {
      // Just show confirmation if no language change
      alert("Setările de afișare au fost aplicate cu succes!")
    }
  })

  // Load notification preferences
  const emailNotifications = localStorage.getItem("emailNotifications") !== "false"
  const systemNotifications = localStorage.getItem("systemNotifications") !== "false"
  const medicationAlerts = localStorage.getItem("medicationAlerts") !== "false"

  document.getElementById("email-notifications").checked = emailNotifications
  document.getElementById("system-notifications").checked = systemNotifications
  document.getElementById("medication-alerts").checked = medicationAlerts
})

// Helper function to get cookie value
function getCookie(name) {
  const value = `; ${document.cookie}`
  const parts = value.split(`; ${name}=`)
  if (parts.length === 2) return parts.pop().split(";").shift()
  return null
}

