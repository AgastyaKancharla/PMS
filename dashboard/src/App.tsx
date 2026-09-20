import { Navigate, Route, Routes } from 'react-router-dom'
import { RequireAdmin } from './auth/RequireAdmin'
import { AppLayout } from './components/AppLayout'
import { ClinicsListPage } from './pages/ClinicsListPage'
import { ClinicDetailPage } from './pages/ClinicDetailPage'
import { ProvisionAdminPage } from './pages/ProvisionAdminPage'

export function App() {
  return (
    <RequireAdmin>
      <Routes>
        <Route element={<AppLayout />}>
          <Route index element={<Navigate to="/clinics" replace />} />
          <Route path="clinics" element={<ClinicsListPage />} />
          <Route path="clinics/:clinicId" element={<ClinicDetailPage />} />
          <Route path="provision-admin" element={<ProvisionAdminPage />} />
          <Route path="*" element={<Navigate to="/clinics" replace />} />
        </Route>
      </Routes>
    </RequireAdmin>
  )
}
