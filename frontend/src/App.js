import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import RegisterPage from './pages/RegisterPage';
import LoginPage from './pages/LoginPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import WelcomePage from './pages/WelcomePage';
import ProtectedRoute from './components/ProtectedRoute';


/*
* App Component
*
* This is the main application component.
* It sets up the routing for the application.
* The AuthProvider component wraps the entire application.
* This allows the AuthContext to be used in all components.
*/
function App() {
  return (
    <AuthProvider>
      <Router>
        <div>
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/reset-password" element={<ForgotPasswordPage />} />
            
            {/* Protected Route */}
            <Route path="/home" element={<ProtectedRoute element={<WelcomePage />} />} />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;