import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

// Provide IANA timezone to Strato's user-preferences SDK so that
// TimeframeSelector and other date-aware components get a valid timezone
// instead of an offset string like "UTC+02:00" which breaks Intl.DateTimeFormat.
const ianaTimezone = Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC';
(globalThis as any).dtRuntime = {
  userPreferences: {
    getTimezone: () => ianaTimezone,
    getLanguage: () => navigator.language || 'en',
  },
};
import variables from '@dynatrace/strato-design-tokens/variables'
import variablesDark from '@dynatrace/strato-design-tokens/variables-dark'
import './index.css'
import { AppRoot } from '@dynatrace/strato-components/core'
import { ToastContainer } from '@dynatrace/strato-components/notifications'
import App from './App.tsx'

// Inject Strato design tokens as CSS custom properties
const lightStyle = document.createElement('style')
lightStyle.textContent = `:root { ${Object.entries(variables as Record<string, string>).map(([k, v]) => `${k}: ${v};`).join(' ')} }`
document.head.appendChild(lightStyle)

const darkStyle = document.createElement('style')
darkStyle.textContent = `@media (prefers-color-scheme: dark) { :root { ${Object.entries(variablesDark as Record<string, string>).map(([k, v]) => `${k}: ${v};`).join(' ')} } }`
document.head.appendChild(darkStyle)

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AppRoot>
      <App />
      <ToastContainer />
    </AppRoot>
  </StrictMode>,
)