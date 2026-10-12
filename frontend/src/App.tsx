import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { LandingPage } from './pages/LandingPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { DashboardPage } from './pages/DashboardPage';
import { AnalyticsPage } from './pages/AnalyticsPage';
import { ContestsPage } from './pages/ContestsPage';
import { LeaderboardPage } from './pages/LeaderboardPage';
import { ProfilePage } from './pages/ProfilePage';
import { CodingProfilesPage } from './pages/CodingProfilesPage';
import { PublicProfilePage } from './pages/PublicProfilePage';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { PublicOnlyRoute } from './components/auth/PublicOnlyRoute';
import { ThemeToggle } from './components/common/ThemeToggle';
import { RouteMetadata } from './components/seo/RouteMetadata';

export const App: React.FC = () => {
  return (
    <>
      <RouteMetadata />
      <Routes>
      {/* Public landing route */}
      <Route path="/" element={<LandingPage />} />

      {/* Public developer profile route */}
      <Route path="/u/:username" element={<><ThemeToggle className="route-theme-toggle" /><PublicProfilePage /></>} />

      {/* Guest-only routes */}
      <Route
        path="/login"
        element={
          <PublicOnlyRoute>
            <><ThemeToggle className="route-theme-toggle" /><LoginPage /></>
          </PublicOnlyRoute>
        }
      />
      <Route
        path="/register"
        element={
          <PublicOnlyRoute>
            <><ThemeToggle className="route-theme-toggle" /><RegisterPage /></>
          </PublicOnlyRoute>
        }
      />

      {/* Protected routes */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/analytics"
        element={
          <ProtectedRoute>
            <AnalyticsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/contests"
        element={
          <ProtectedRoute>
            <ContestsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/leaderboard"
        element={
          <ProtectedRoute>
            <LeaderboardPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/profile"
        element={
          <ProtectedRoute>
            <ProfilePage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/coding-profiles"
        element={
          <ProtectedRoute>
            <CodingProfilesPage />
          </ProtectedRoute>
        }
      />

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
};

export default App;
