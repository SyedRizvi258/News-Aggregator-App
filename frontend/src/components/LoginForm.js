import React, { useState, useEffect } from 'react';
import '../Form.css';

const LoginForm = ({ onSubmit, error, isSubmitting, success }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(email, password);
  };

  // Use useEffect to reset form fields when success changes to true
  useEffect(() => {
    if (success) {
      setEmail('');
      setPassword('');
    }
  }, [success]);

  return (
    <div className="container">
      <form onSubmit={(e) => handleSubmit(e)} className="form p-4 border rounded shadow">
      <h1 className="brand">QuickByte</h1>
        <h1 className="text-center mb-4">Login</h1>
        
        {error && (
          <div className="error-message text-danger">
            <p>{error}</p>
          </div>
        )}
  
        <div className="mb-3">
          <input
            type="email"
            className="form-control"
            placeholder="Email"
            aria-label="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
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
          
          <button type="submit" className="btn btn-primary w-100 mt-3" disabled={isSubmitting}>
            {isSubmitting ? 'Logging in...' : 'Login'}
          </button>
        </div>
  
        <p className="mt-3 text-center">
          Forgot password? <a href="/reset-password">Reset password</a>
        </p>
        <p className="mt-3 text-center">
          Don't have an account? <a href="/register">Sign up</a>
        </p>
      </form>
    </div>
  );
};

export default LoginForm;