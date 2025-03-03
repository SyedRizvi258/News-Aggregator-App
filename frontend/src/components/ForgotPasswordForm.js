import React, { useState } from 'react';
import '../Form.css';

const ForgotPasswordForm = ({ onSubmit, error, isSubmitting, success, message }) => {
    const [email, setEmail] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [showNewPassword, setShowNewPassword] = useState(false);

    const resetFields = () => {
        setEmail('');
        setNewPassword('');
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        onSubmit(email, newPassword, resetFields);
    };

    return (
        <div className="container">
            <form onSubmit={(e) => handleSubmit(e)} className="form p-4 border rounded shadow">
                <h1 className="brand">QuickByte</h1>
                <h1 className="text-center mb-4">Reset Password</h1>

                {error && (
                    <div className="error-message text-danger">
                        <p>{error}</p>
                    </div>
                )}
                {message && <p className="success-message text-success">{message}</p>}

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

                    <button type="submit" className="btn btn-primary w-100 mt-3" disabled={isSubmitting}>
                        {isSubmitting ? 'Resetting...' : 'Reset Password'}
                    </button>
                </div>
                
                <p className="mt-3 text-center">
                    Go back to: <a href="/">Login</a>
                </p>
            </form>
        </div>
    );
};

export default ForgotPasswordForm;