import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Navbar from '@/components/Navbar'
import HomePage from '@/pages/HomePage'
import ReportIssuePage from '@/pages/ReportIssuePage'
import IssueDetailPage from '@/pages/IssueDetailPage'

export default function App() {
  return (
    <BrowserRouter>
      <Navbar />
      <main>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/report" element={<ReportIssuePage />} />
          <Route path="/issues/:id" element={<IssueDetailPage />} />
        </Routes>
      </main>
    </BrowserRouter>
  )
}
