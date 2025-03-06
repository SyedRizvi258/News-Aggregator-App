import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import LoginForm from '../components/LoginForm';
import { useAuth } from '../contexts/AuthContext';


/*
* LoginPage
*
* A page that allows users to log in.
* It contains the LoginForm component.
*/
const LoginPage = () => {
    const { login } = useAuth(); // Destructure login function from AuthContext
    const [error, setError] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [success, setSuccess] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async (email, password) => {
        setError(null);
        setIsSubmitting(true);

        try {
            const response = await fetch(`${process.env.REACT_APP_BASE_URL}/api/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password }),
                credentials: 'include', // Required for cookies
            });

            const responseData = await response.json();

            if (!response.ok) {
                throw new Error(responseData.error || 'Login failed'); // Use the 'error' field from the backend
            }

            // Call the login function from the AuthContext
            login(responseData.username, responseData.userId);

            setSuccess(true); // Set the success state to true
            navigate('/home'); // Redirect to the home page (Welcome Page)

        } catch (err) {
            setError(err.message); // Display the error message
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="login-container">
            <LoginForm onSubmit={handleLogin} error={error} isSubmitting={isSubmitting} success={success} />
        </div>
    );
};

export default LoginPage;
