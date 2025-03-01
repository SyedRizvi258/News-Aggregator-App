import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import LoginForm from '../components/LoginForm';
import { useAuth } from '../contexts/AuthContext';
import Cookies from 'js-cookie';

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

            //login();  // Call the login function to update state
            
            // Fetch username from cookies
            const storedUsername = Cookies.get('username'); 

            // Update auth state with username
            login(storedUsername || "User", responseData.userId); 

            setSuccess(true); // Set the success state to true
            navigate('/home'); // Redirect to the home page

        } catch (err) {
            setError(err.message); // Display the error message to the user
        } finally {
            setIsSubmitting(false); // Reset the submission state
        }
    };

    return (
        <div className="login-container">
            <LoginForm onSubmit={handleLogin} error={error} isSubmitting={isSubmitting} success={success} />
        </div>
    );
};

export default LoginPage;