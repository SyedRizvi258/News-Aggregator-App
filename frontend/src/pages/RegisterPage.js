import React, { useState } from 'react';
import RegisterForm from '../components/SignUpForm';


/*
* RegisterPage
*
* A page that allows users to register.
* It contains the SignUpForm component.
*/
const RegisterPage = () => {
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);
  
    const handleRegister = async (username, email, password) => {
      setError(null);
      setSuccess(false);
  
      try {
          const response = await fetch(`${process.env.REACT_APP_BASE_URL}/api/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password }),
          });

          const data = await response.json();
    
          if (!response.ok) {
            setError(data);
          }
    
          // Use 'message' field for success messages from the backend
          setSuccess(data.message); 

      } catch (err) {
        setError(err.message); // Display the error message
      }
    };
  
    return (
      <div className="register-container">
        <RegisterForm onSubmit={handleRegister} error={error} success={success} />
      </div>
  );
};
  
export default RegisterPage;
