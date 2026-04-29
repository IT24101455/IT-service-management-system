import { Award, MapPin, Mail, Calendar, Briefcase, ExternalLink, X } from 'lucide-react';

export default function TechnicianProfileModal({ isOpen, onClose, technician, onMessage }) {
    if (!isOpen || !technician) return null;

    return (
        <div className="modal-overlay" onClick={onClose} style={{ zIndex: 1100 }}>
            <div className="modal-card" onClick={e => e.stopPropagation()} style={{ 
                width: '100%', 
                maxWidth: '600px', 
                padding: '0', 
                overflow: 'hidden',
                borderRadius: '24px'
            }}>
                {/* Header / Hero */}
                <div style={{ 
                    background: 'linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%)', 
                    padding: '40px 32px', 
                    color: 'white',
                    position: 'relative'
                }}>
                    <button 
                        onClick={onClose}
                        style={{ 
                            position: 'absolute', 
                            top: '20px', 
                            right: '20px', 
                            background: 'rgba(255, 255, 255, 0.2)', 
                            border: 'none', 
                            borderRadius: '12px', 
                            width: '36px', 
                            height: '36px', 
                            color: 'white',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center'
                        }}
                    >
                        <X size={20} />
                    </button>

                    <div style={{ display: 'flex', gap: '24px', alignItems: 'center' }}>
                        <div className="user-avatar" style={{ 
                            width: '80px', 
                            height: '80px', 
                            fontSize: '32px', 
                            borderRadius: '24px',
                            background: 'rgba(255, 255, 255, 0.25)',
                            backdropFilter: 'blur(10px)',
                            border: '2px solid rgba(255, 255, 255, 0.5)',
                            color: 'white'
                        }}>
                            {technician.name?.charAt(0).toUpperCase()}
                        </div>
                        <div>
                            <h2 style={{ fontSize: '24px', fontWeight: '800', marginBottom: '4px' }}>{technician.name}</h2>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', opacity: 0.9 }}>
                                <Briefcase size={16} />
                                <span style={{ fontSize: '14px', fontWeight: '600' }}>{technician.specialization || 'Support Specialist'}</span>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Content */}
                <div style={{ padding: '32px', display: 'flex', flexDirection: 'column', gap: '32px' }}>
                    
                    {/* Basic Info & Experience */}
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                <div style={{ width: '32px', height: '32px', background: 'var(--bg)', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
                                    <MapPin size={16} />
                                </div>
                                <div style={{ display: 'flex', flexDirection: 'column' }}>
                                    <span style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: '600', textTransform: 'uppercase' }}>Location</span>
                                    <span style={{ fontSize: '14px', fontWeight: '600' }}>{technician.province}, {technician.district}</span>
                                </div>
                            </div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                <div style={{ width: '32px', height: '32px', background: 'var(--bg)', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
                                    <Mail size={16} />
                                </div>
                                <div style={{ display: 'flex', flexDirection: 'column' }}>
                                    <span style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: '600', textTransform: 'uppercase' }}>Contact</span>
                                    <span style={{ fontSize: '14px', fontWeight: '600' }}>{technician.email}</span>
                                </div>
                            </div>
                        </div>

                        <div style={{ 
                            background: 'var(--primary-50)', 
                            borderRadius: '20px', 
                            padding: '20px', 
                            display: 'flex', 
                            flexDirection: 'column', 
                            alignItems: 'center', 
                            justifyContent: 'center',
                            border: '1px solid var(--primary-100)'
                        }}>
                            <Award size={32} style={{ color: 'var(--primary)', marginBottom: '8px' }} />
                            <span style={{ fontSize: '24px', fontWeight: '800', color: 'var(--primary-dark)' }}>{technician.experienceYears || '0'}+</span>
                            <span style={{ fontSize: '12px', color: 'var(--primary)', fontWeight: '700', textTransform: 'uppercase' }}>Years Experience</span>
                        </div>
                    </div>

                    {/* Qualifications */}
                    <div>
                        <h3 style={{ fontSize: '18px', fontWeight: '800', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <Award size={20} className="text-secondary" />
                            Qualifications & Certifications
                        </h3>
                        
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                            {!technician.qualifications || technician.qualifications.length === 0 ? (
                                <div style={{ padding: '24px', textAlign: 'center', background: 'var(--bg)', borderRadius: '16px', border: '1px dashed var(--border)' }}>
                                    <p style={{ fontSize: '13px', color: 'var(--text-muted)' }}>No certifications listed yet.</p>
                                </div>
                            ) : (
                                technician.qualifications.map(q => (
                                    <div key={q.id} style={{ 
                                        display: 'flex', 
                                        justifyContent: 'space-between', 
                                        alignItems: 'center', 
                                        padding: '16px', 
                                        background: 'var(--bg)', 
                                        borderRadius: '16px', 
                                        border: '1px solid var(--border-light)' 
                                    }}>
                                        <div>
                                            <h4 style={{ fontSize: '14px', fontWeight: '700', marginBottom: '2px' }}>{q.title}</h4>
                                            <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{q.institution} • {q.year}</p>
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                        <button className="btn btn-primary" onClick={() => {
                            onClose();
                            if (onMessage) onMessage(technician);
                        }} style={{ borderRadius: '12px' }}>
                            Message Technician
                        </button>
                        <button className="btn btn-secondary" onClick={onClose} style={{ borderRadius: '12px' }}>
                            Close
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
