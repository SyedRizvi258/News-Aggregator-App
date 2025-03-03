import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [auth, setAuth] = useState({ isLoggedIn: false, username: null, userId: null });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const verifyUser = async () => {
      try {
        const response = await fetch(`${process.env.REACT_APP_BASE_URL}/api/auth/verify`, {
          method: "GET",
          credentials: "include", // Ensures HttpOnly cookie is sent
        });

        if (response.ok) {
          const data = await response.json();
          setAuth({ isLoggedIn: true, username: data.username, userId: data.userId });
        } else {
          setAuth({ isLoggedIn: false, username: null, userId: null });
        }
      } catch (error) {
        setAuth({ isLoggedIn: false, username: null, userId: null });
      } finally {
        setLoading(false);
      }
    };

    verifyUser();
  }, []);

  const login = (username, userId) => {
    setAuth({ isLoggedIn: true, username, userId });
  };

  const logout = async () => {
    await fetch(`${process.env.REACT_APP_BASE_URL}/api/auth/logout`, {
      method: "POST",
      credentials: "include",
    });

    setAuth({ isLoggedIn: false, username: null, userId: null });
  };

  return (
    <AuthContext.Provider value={{ auth, login, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
};
