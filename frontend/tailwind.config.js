/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'amazon-yellow': '#f0c14b',
        'amazon-orange': '#ff9900',
        'amazon-dark': '#131921',
        'amazon-light': '#232f3e',
      }
    },
  },
  plugins: [],
}
