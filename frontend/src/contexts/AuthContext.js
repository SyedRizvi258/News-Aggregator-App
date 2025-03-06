import React, { createContext, useContext, useState, useEffect } from 'react';


/*
* AuthContext
* 
* A context that provides information about the user's authentication status.
* It also provides functions to login and logout.
*/
const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext); // Custom hook to get the auth context

export const AuthProvider = ({ children }) => {
  const [auth, setAuth] = useState({ isLoggedIn: false, username: null, userId: null });
  const [loading, setLoading] = useState(true);

  // This function is called when the user logs in
  useEffect(() => {
    const verifyUser = async () => {
      try {
        const response = await fetch(`${process.env.REACT_APP_BASE_URL}/api/auth/verify`, {
          method: "GET",
          credentials: "include", // Required for cookies
        });

        // If the response is successful, set the user as logged in
        if (response.ok) {
          const data = await response.json();
          setAuth({ isLoggedIn: true, username: data.username, userId: data.userId });
        // If the response is not successful, set the user as not logged in
        } else {
          setAuth({ isLoggedIn: false, username: null, userId: null });
        }
      } catch (error) {
        setAuth({ isLoggedIn: false, username: null, userId: null });
      } finally {
        setLoading(false); // Set loading to false after verifying user
      }
    };

    verifyUser();
  }, []);

  // Function to log the user in
  const login = (username, userId) => {
    setAuth({ isLoggedIn: true, username, userId });
  };

  // Function to log the user out
  const logout = async () => {
    await fetch(`${process.env.REACT_APP_BASE_URL}/api/auth/logout`, {
      method: "POST",
      credentials: "include", // Required for cookies
    });

    setAuth({ isLoggedIn: false, username: null, userId: null });
  };

  return (
    <AuthContext.Provider value={{ auth, login, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
};
