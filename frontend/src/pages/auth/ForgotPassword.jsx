import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { forgotPassword, resetPassword } from '../../api/api';
import { toast } from 'react-toastify';
import { Mail, KeyRound, Lock, ArrowRight, ArrowLeft } from 'lucide-react';
import logo from '../../assets/logo.png';

export default function ForgotPassword() {
    const navigate = useNavigate();

    // Step 1: Request OTP, Step 2: Reset Password
    const [step, setStep] = useState(1);

    const [email, setEmail] = useState('');
    const [otp, setOtp] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');

    const [loading, setLoading] = useState(false);

    const handleRequestOtp = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            await forgotPassword({ email });
            toast.success(`OTP sent to ${email}`);
            setStep(2);
        } catch (err) {
            toast.error(err.response?.data || 'Failed to send OTP. Is the email correct?');
        } finally {
            setLoading(false);
        }
    };

    const handleResetPassword = async (e) => {
        e.preventDefault();
        if (newPassword !== confirmPassword) {
            toast.error('Passwords do not match');
            return;
        }

        setLoading(true);
        try {
            await resetPassword({ email, otp, newPassword });
            toast.success('Password reset successfully!');
            navigate('/login/user'); // Assuming mostly users forget. They can click AuthChoice if technician.
        } catch (err) {
            toast.error(err.response?.data || 'Failed to reset password. Invalid OTP?');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-box">
                <div style={{ textAlign: 'center', marginBottom: 30 }}>
                    <img src={logo} alt="TechNova" style={{ width: 60, height: 60, marginBottom: 15 }} />
                    <h2 style={{ fontSize: 24, fontWeight: 700, color: 'var(--text)' }}>
                        {step === 1 ? 'Reset Password' : 'Create New Password'}
                    </h2>
                    <p style={{ color: 'var(--text-muted)', marginTop: 5 }}>
                        {step === 1
                            ? 'Enter your email address and we will send you a 6-digit OTP.'
                            : `Enter the OTP sent to ${email} and your new password.`}
                    </p>
                </div>

                {step === 1 && (
                    <form onSubmit={handleRequestOtp}>
                        <div className="form-group" style={{ marginBottom: 20 }}>
                            <label className="form-label" style={{ fontWeight: 600 }}>Email Address</label>
                            <div className="input-with-icon">
                                <Mail size={18} className="input-icon" />
                                <input
                                    type="email"
                                    className="form-control"
                                    placeholder="Enter your registered email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                    style={{ height: 48 }}
                                />
                            </div>
                        </div>
                        <button type="submit" className="btn btn-primary" style={{ width: '100%', height: 48, fontSize: 16 }} disabled={loading}>
                            {loading ? 'Sending...' : 'Send OTP'} <ArrowRight size={18} />
                        </button>
                    </form>
                )}

                {step === 2 && (
                    <form onSubmit={handleResetPassword}>
                        <div className="form-group" style={{ marginBottom: 20 }}>
                            <label className="form-label" style={{ fontWeight: 600 }}>OTP Code</label>
                            <div className="input-with-icon">
                                <KeyRound size={18} className="input-icon" />
                                <input
                                    type="text"
                                    className="form-control"
                                    placeholder="Enter 6-digit OTP"
                                    value={otp}
                                    onChange={(e) => setOtp(e.target.value)}
                                    required
                                    style={{ height: 48 }}
                                />
                            </div>
                        </div>

                        <div className="form-group" style={{ marginBottom: 20 }}>
                            <label className="form-label" style={{ fontWeight: 600 }}>New Password</label>
                            <div className="input-with-icon">
                                <Lock size={18} className="input-icon" />
                                <input
                                    type="password"
                                    className="form-control"
                                    placeholder="Enter new password"
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                    required
                                    style={{ height: 48 }}
                                />
                            </div>
                        </div>

                        <div className="form-group" style={{ marginBottom: 20 }}>
                            <label className="form-label" style={{ fontWeight: 600 }}>Confirm New Password</label>
                            <div className="input-with-icon">
                                <Lock size={18} className="input-icon" />
                                <input
                                    type="password"
                                    className="form-control"
                                    placeholder="Confirm new password"
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    required
                                    style={{ height: 48 }}
                                />
                            </div>
                        </div>

                        <button type="submit" className="btn btn-primary" style={{ width: '100%', height: 48, fontSize: 16 }} disabled={loading}>
                            {loading ? 'Resetting...' : 'Reset Password'} <ArrowRight size={18} />
                        </button>
                    </form>
                )}

                <div style={{ textAlign: 'center', marginTop: 20 }}>
                    <Link to="/auth-choice" style={{ color: 'var(--text-muted)', textDecoration: 'none', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 5 }}>
                        <ArrowLeft size={16} /> Back to Login
                    </Link>
                </div>
            </div>
        </div>
    );
}
