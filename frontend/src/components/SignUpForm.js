import React, { useState, useEffect } from 'react';
import '../Form.css';


/*
* SignUpForm Component
*
* A form component that allows users to sign up.
* It displays input fields for username, email, and password.
* It also displays error/success messages.
*/
const SignUpForm = ({ onSubmit, error, success }) => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false); 

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    await onSubmit(username, email, password);
    setIsSubmitting(false);
  };

  // Clear username, email, and password fields on successful sign up
  useEffect(() => {
    if (success) {
      setUsername('');
      setEmail('');
      setPassword('');
    }
  }, [success]);

  return (
    <div className="container">
      <form onSubmit={handleSubmit} className="form p-4 border rounded shadow">
        <h1 className="brand">QuickByte</h1>
        <h1 className="text-center mb-4">Sign Up</h1>
        
        {/* Display message based on if the request was successful or not */}
        {error && !error.username && !error.email && !error.password && (
          <div className="error-message text-danger">
            {Object.keys(error).map((key) => (
              <p key={key}>{error[key]}</p>
            ))}
          </div>
        )}
        {success && <p className="success-message text-success">{success}</p>}
        
        {/* Input fields for username, email, and password */}
        <div className="mb-3">
          <input
            type="text"
            className="form-control"
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            aria-label="Username"
          />
          {error?.username && <p className="error-message text-danger">{error.username}</p>}
        </div>
        
        <div className="mb-3">
          <input
            type="email"
            className="form-control"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            aria-label="Email"
          />
          {error?.email && <p className="error-message text-danger">{error.email}</p>}
        </div>
        
        <div className="mb-3">
          <div className="password-container">
            <input
              type={showPassword ? "text" : "password"}
              className="form-control"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              aria-label="Password"
            />
            <button
              type="button"
              className="password-toggle"
              onClick={() => setShowPassword(!showPassword)}
            >
              {showPassword ? "Hide" : "Show"}
            </button>
          </div>
          {error?.password && <p className="error-message text-danger">{error.password}</p>}
        
          {/* Register button */}
          <button type="submit" className="btn btn-primary w-100 mt-3" disabled={isSubmitting}>
            {isSubmitting ? "Registering..." : "Register"}
          </button>
        </div>
        
        {/* Link to go back to login page */}
        <p className="mt-3 text-center">
          Already have an account? <a href="/">Log in</a>
        </p>
      </form>
    </div>
  );
};

export default SignUpForm;
