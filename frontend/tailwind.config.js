/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        dota: {
          bg: '#0e1014',
          panel: '#161a22',
          panel2: '#1d2230',
          border: '#2a3142',
          red: '#c8392c',
          redHover: '#e34a3c',
          gold: '#c8a857',
          goldBright: '#f0c75e',
          text: '#e7e9ee',
          muted: '#8a92a5',
          win: '#4caf50',
          loss: '#e84545',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        display: ['Rajdhani', 'Inter', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        glow: '0 0 20px rgba(200, 168, 87, 0.15)',
        redGlow: '0 0 24px rgba(200, 57, 44, 0.25)',
      },
    },
  },
  plugins: [],
};
