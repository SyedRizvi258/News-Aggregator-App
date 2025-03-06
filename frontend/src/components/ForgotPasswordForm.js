import React, { useState } from 'react';
import '../Form.css';


/*
* ForgotPasswordForm Component
*
* A form component that allows users to reset their password.
* It displays input fields for email and new password.
* It also displays error/success messages.
*/
const ForgotPasswordForm = ({ onSubmit, error, isSubmitting, success, message }) => {
    const [email, setEmail] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [showNewPassword, setShowNewPassword] = useState(false);

    const resetFields = () => {
        setEmail('');
        setNewPassword('');
    };

    // Handle form submission
    const handleSubmit = (e) => {
        e.preventDefault();
        onSubmit(email, newPassword, resetFields);
    };

    return (
        <div className="container">
            <form onSubmit={(e) => handleSubmit(e)} className="form p-4 border rounded shadow">
                <h1 className="brand">QuickByte</h1>
                <h1 className="text-center mb-4">Reset Password</h1>

                {/* Display message based on if the request was successful or not */}
                {error && (
                    <div className="error-message text-danger">
                        <p>{error}</p>
                    </div>
                )}
                {message && <p className="success-message text-success">{message}</p>}

                {/* Input fields for email and new password */}
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
                            type={showNewPassword ? "text" : "password"}
                            className="form-control"
                            placeholder="New password"
                            value={newPassword}
                            onChange={(e) => setNewPassword(e.target.value)}
                            required
                            aria-label="New Password"
                        />
                        <button
                            type="button"
                            className="password-toggle"
                            onClick={() => setShowNewPassword(!showNewPassword)}
                        >
                            {showNewPassword ? "Hide" : "Show"}
                        </button>
                    </div>
            
                    {error?.newPassword && <p className="error-message text-danger">{error.newPassword}</p>}

                    {/* Reset Password button */}
                    <button type="submit" className="btn btn-primary w-100 mt-3" disabled={isSubmitting}>
                        {isSubmitting ? 'Resetting...' : 'Reset Password'}
                    </button>
                </div>
                
                {/* Link to go back to login page */}
                <p className="mt-3 text-center">
                    Go back to: <a href="/">Login</a>
                </p>
            </form>
        </div>
    );
};

export default ForgotPasswordForm;
