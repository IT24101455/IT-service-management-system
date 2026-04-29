import { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { login } from '../../api/api';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'react-toastify';
import { Eye, EyeOff, LogIn, User, Wrench } from 'lucide-react';
import AuthLayout from './AuthLayout';

export default function Login() {
    const { role: urlRole } = useParams();
    const [selectedRole, setSelectedRole] = useState(urlRole || 'USER');
    const [form, setForm] = useState({ email: '', password: '' });
    const [showPw, setShowPw] = useState(false);
    const [loading, setLoading] = useState(false);
    const { loginUser } = useAuth();
    const navigate = useNavigate();

    // Sync selectedRole with URL param if it changes
    useEffect(() => {
        if (urlRole) setSelectedRole(urlRole);
    }, [urlRole]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const res = await login({ ...form, role: selectedRole });
            loginUser(res.data);
            toast.success(`Welcome back, ${res.data.name}!`);
            const redirect = res.data.role === 'ADMIN' ? '/admin/dashboard'
                : res.data.role === 'TECHNICIAN' ? '/technician/dashboard'
                    : '/user/dashboard';
            navigate(redirect);
        } catch (err) {
            toast.error(err.response?.data || 'Invalid email or password');
        } finally {
            setLoading(false);
        }
    };

    return (
        <AuthLayout 
            title="Welcome back" 
            subtitle={`Please enter your details to sign in as ${selectedRole.toLowerCase()}.`}
        >

            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label className="form-label">Email Address</label>
                    <input
                        type="email"
                        className="form-control"
                        placeholder="you@example.com"
                        value={form.email}
                        onChange={e => setForm({ ...form, email: e.target.value })}
                        required
                    />
                </div>

                <div className="form-group">
                    <label className="form-label">Password</label>
                    <div style={{ position: 'relative' }}>
                        <input
                            type={showPw ? 'text' : 'password'}
                            className="form-control"
                            placeholder="Enter your password"
                            value={form.password}
                            onChange={e => setForm({ ...form, password: e.target.value })}
                            required
                            style={{ paddingRight: 42 }}
                        />
                        <button type="button" onClick={() => setShowPw(!showPw)}
                            style={{
                                position: 'absolute', right: 12, top: '50%', transform: 'translateY(-50%)',
                                background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)'
                            }}>
                            {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                        </button>
                    </div>
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: -8, marginBottom: 20 }}>
                    <Link to="/forgot-password" style={{ fontSize: 13, color: 'var(--auth-blue-primary)', textDecoration: 'none', fontWeight: 600 }}>
                        Forgot Password?
                    </Link>
                </div>

                <button type="submit" className="btn btn-primary btn-lg" style={{ width: '100%', borderRadius: '10px' }} disabled={loading}>
                    {loading ? 'Signing in...' : 'Sign In'}
                    {!loading && <LogIn size={18} style={{ marginLeft: 8 }} />}
                </button>
            </form>


            <div style={{ textAlign: 'center', marginTop: 32, fontSize: 14, color: 'var(--text-muted)' }}>
                Don't have an account?{' '}
                <Link to="/register" style={{ color: 'var(--auth-blue-primary)', fontWeight: 700, textDecoration: 'none' }}>
                    Create Account
                </Link>
            </div>

        </AuthLayout>
    );
}
