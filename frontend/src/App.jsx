import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Navbar from '@/components/Navbar'
import LoginPage from '@/pages/LoginPage'
import HomePage from '@/pages/HomePage'
import ReportIssuePage from '@/pages/ReportIssuePage'
import IssueDetailPage from '@/pages/IssueDetailPage'
import MyPostsPage from '@/pages/MyPostsPage'
import SettingsPage from '@/pages/SettingsPage'
import UserProfilePage from '@/pages/UserProfilePage'
import ProtectedRoute from '@/components/ProtectedRoute'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public */}
        <Route path="/login" element={<LoginPage />} />

        {/* Protected — Navbar only shown inside the app shell */}
        <Route
          path="/*"
          element={
            <ProtectedRoute>
              <Navbar />
              <main>
                <Routes>
                  <Route path="/home" element={<HomePage />} />
                  <Route path="/report" element={<ReportIssuePage />} />
                  <Route path="/issues/:id" element={<IssueDetailPage />} />
                  <Route path="/my-posts" element={<MyPostsPage />} />
                  <Route path="/settings" element={<SettingsPage />} />
                  <Route path="/user/:userId" element={<UserProfilePage />} />
                  <Route path="*" element={<Navigate to="/home" replace />} />
                </Routes>
              </main>
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  )
}
