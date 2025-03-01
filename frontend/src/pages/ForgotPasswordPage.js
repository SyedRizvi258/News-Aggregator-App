import React, { useState } from 'react';
import ForgotPasswordForm from '../components/ForgotPasswordForm';

const ForgotPasswordPage = () => {
    const [error, setError] = useState(null);
    const [message, setMessage] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [success, setSuccess] = useState(false);

    const handlePasswordReset = async (email, newPassword, resetFields) => {
        setError(null);
        setMessage(null);
        setIsSubmitting(true);

        try {
            const response = await fetch(`${process.env.REACT_APP_BASE_URL}/api/auth/change-password`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, newPassword }),
            });

            const responseData = await response.json();

            if (!response.ok) {
                throw new Error(responseData.error || 'Failed to change password.');
            }

            setMessage(responseData.message);
            setSuccess(true);

            resetFields();

        } catch (err) {
            setError(err.message);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="forgot-password-container">
            <ForgotPasswordForm 
                onSubmit={handlePasswordReset} 
                error={error} 
                message={message} 
                isSubmitting={isSubmitting} 
                success={success}
            />
        </div>
    );
};

export default ForgotPasswordPage;