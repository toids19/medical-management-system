// Apply theme immediately to prevent flash of unstyled content
;(() => {
  // Get the saved theme from localStorage or default to light
  const savedTheme = localStorage.getItem("theme") || "light"

  // Apply the theme to the document element
  if (savedTheme === "dark") {
    document.documentElement.setAttribute("data-theme", "dark")
  } else if (savedTheme === "system") {
    const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches
    document.documentElement.setAttribute("data-theme", prefersDark ? "dark" : "light")
  } else {
    document.documentElement.setAttribute("data-theme", "light")
  }
})()

// Apply theme immediately to prevent flash of unstyled content
;(() => {
  // Get the saved theme from localStorage or default to light
  const savedTheme = localStorage.getItem("theme") || "light"

  // Apply the theme to the document element
  if (savedTheme === "dark") {
    document.documentElement.setAttribute("data-theme", "dark")
  } else if (savedTheme === "system") {
    const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches
    document.documentElement.setAttribute("data-theme", prefersDark ? "dark" : "light")
  } else {
    document.documentElement.setAttribute("data-theme", "light")
  }
})()

document.addEventListener("DOMContentLoaded", () => {
  // Sidebar toggle functionality
  const sidebarToggle = document.querySelector(".sidebar-toggle")
  const sidebar = document.querySelector(".sidebar")

  if (sidebarToggle && sidebar) {
    sidebarToggle.addEventListener("click", () => {
      sidebar.classList.toggle("collapsed")

      // Save sidebar state to localStorage
      const isCollapsed = sidebar.classList.contains("collapsed")
      localStorage.setItem("sidebarCollapsed", isCollapsed)
    })

    // Check localStorage for sidebar state
    const isCollapsed = localStorage.getItem("sidebarCollapsed") === "true"
    if (isCollapsed) {
      sidebar.classList.add("collapsed")
    }
  }

  // Mobile sidebar toggle
  const mobileSidebarToggle = document.querySelector(".mobile-sidebar-toggle")

  if (mobileSidebarToggle && sidebar) {
    mobileSidebarToggle.addEventListener("click", () => {
      sidebar.classList.toggle("mobile-open")
    })
  }

  // Close sidebar when clicking outside on mobile
  document.addEventListener("click", (event) => {
    const isMobile = window.innerWidth <= 768
    if (isMobile && sidebar && sidebar.classList.contains("mobile-open")) {
      const isClickInside =
        sidebar.contains(event.target) || (mobileSidebarToggle && mobileSidebarToggle.contains(event.target))

      if (!isClickInside) {
        sidebar.classList.remove("mobile-open")
      }
    }
  })

  // Set active menu item based on current URL
  const currentPath = window.location.pathname
  const menuLinks = document.querySelectorAll(".sidebar-menu-link")

  menuLinks.forEach((link) => {
    const href = link.getAttribute("href")
    if (href && currentPath.includes(href) && href !== "/") {
      link.classList.add("active")
    } else if (href === "/" && currentPath === "/") {
      link.classList.add("active")
    }
  })
})

