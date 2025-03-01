import React, { useState } from 'react';
import RegisterForm from '../components/SignUpForm';

const RegisterPage = () => {
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);
  
    const handleRegister = async (username, email, password) => {
      setError(null);
      setSuccess(false); // Reset success state on new submission
  
      try {
          const response = await fetch(`${process.env.REACT_APP_BASE_URL}/api/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password }),
          });

          const data = await response.json(); // Parse JSON response
    
          if (!response.ok) {
            setError(data);
          }
    
          // Use 'message' field for success messages from the backend
          setSuccess(data.message); // Update UI with success message

      } catch (err) {
        setError(err.message); // Set the error message to state
      }
    };
  
    return (
      <div className="register-container">
        <RegisterForm onSubmit={handleRegister} error={error} success={success} />
      </div>
  );
};
  
export default RegisterPage;