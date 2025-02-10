document.addEventListener('DOMContentLoaded', () => {
    const themeToggle = document.getElementById('theme-toggle');

    // Function to update button symbol
    function updateButtonSymbol() {
        if (document.documentElement.getAttribute('data-theme') === 'dark') {
            themeToggle.textContent = '🌞'; // Sun for dark mode
        } else {
            themeToggle.textContent = '🌙'; // Moon for light mode
        }
    }

    // Check for saved theme
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme) {
        document.documentElement.setAttribute('data-theme', savedTheme);
        updateButtonSymbol(); // Update button symbol based on saved theme
    }

    // Toggle theme only when the button is clicked
    themeToggle.addEventListener('click', () => {
        const currentTheme = document.documentElement.getAttribute('data-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';

        document.documentElement.setAttribute('data-theme', newTheme);
        localStorage.setItem('theme', newTheme);
        updateButtonSymbol(); // Update button symbol after theme change
    });

    // Initial button symbol update
    updateButtonSymbol();
});