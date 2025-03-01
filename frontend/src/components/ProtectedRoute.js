import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

// ProtectedRoute Component
const ProtectedRoute = ({ element }) => {
    const { auth, loading } = useAuth();  // Get the auth state
  
    if (loading) {
      return <div>Loading...</div>; // Show a loading message instead of blank page
    }
  
    return auth.isLoggedIn ? element : <Navigate to="/" />;
  };
  
  export default ProtectedRoute;