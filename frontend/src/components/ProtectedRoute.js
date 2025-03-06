import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';


/*
 * ProtectedRoute Component
 * 
 * Makes the Welcome Page only accessible to logged in users.
 * It checks if the user is logged in and redirects to the login page if not.
 */
const ProtectedRoute = ({ element }) => {
    const { auth, loading } = useAuth();  // Get the auth state
  
    if (loading) {
      return <div>Loading...</div>;
    }
  
    return auth.isLoggedIn ? element : <Navigate to="/" />; // Redirect to login page if not logged in
  };
  
  export default ProtectedRoute;
  