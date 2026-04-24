import { createContext, useContext, useState } from 'react'

const LocationContext = createContext(null)

export function LocationProvider({ children }) {
  const [location, setLocationState] = useState(() => {
    try {
      const stored = localStorage.getItem('civic_location')
      return stored ? JSON.parse(stored) : null
    } catch {
      return null
    }
  })

  const setLocation = (loc) => {
    setLocationState(loc)
    localStorage.setItem('civic_location', JSON.stringify(loc))
  }

  const clearLocation = () => {
    setLocationState(null)
    localStorage.removeItem('civic_location')
  }

  return (
    <LocationContext.Provider value={{ location, setLocation, clearLocation }}>
      {children}
    </LocationContext.Provider>
  )
}

export const useLocationContext = () => useContext(LocationContext)
