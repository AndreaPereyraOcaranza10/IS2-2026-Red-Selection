import { useState, type FormEvent } from 'react';
import { ArrowRight, Check, Eye, EyeOff, Headphones, MessageCircle, ShieldCheck } from 'lucide-react';
import { api } from '../lib/api';

export function LoginPage({ onSuccess, onStaff }: { onSuccess: (email: string, password: string) => Promise<void>; onStaff: () => void }) {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [visible, setVisible] = useState(false);
  const [notice, setNotice] = useState('');
  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setNotice('');
    const form = new FormData(event.currentTarget);
    const email = String(form.get('email') || '');
    const password = String(form.get('password') || '');
    try {
      if (mode === 'login') { await onSuccess(email, password); return; }
      const result = await api.register(email, password);
      setNotice(result.message);
    } catch (error) { setNotice(error instanceof Error ? error.message : 'No se pudo completar la solicitud.'); }
  };
  return <main className="login-page">
    <div className="login-topline">
      <span><i /> Portal para comercios registrados &middot; Cuyo</span>
      <span className="login-topline-info">Corte de pedidos: <b>13:30 hs</b></span>
    </div>
    <div className="login-layout">
      <section className="login-pitch">
        <span className="section-kicker">ABASTECIMIENTO DIRECTO</span>
        <h1>Portal B2B<br /><em>Mayorista.</em></h1>
        <p>Abastecimiento directo y listas de f&aacute;brica para kioscos, autoservicios y distribuidores en Gran Mendoza y San Juan.</p>
        <div className="login-benefits">
          <div><Check /> Facturaci&oacute;n fiscal autom&aacute;tica con validaci&oacute;n online de CUIT.</div>
          <div><Check /> Precios oficiales por bulto cerrado y pallets consolidados.</div>
          <div><Check /> Retiro prioritario en dep&oacute;sitos o reparto programado.</div>
        </div>
        <div className="login-pitch-bottom"><ShieldCheck /><span><b>Una cuenta para tu negocio.</b><small>Precios y condiciones pensados para comercios.</small></span></div>
      </section>
      <section className="login-card">
        <div className="login-tabs">
          <button className={mode === 'login' ? 'selected' : ''} onClick={() => { setMode('login'); setNotice(''); }}>Iniciar sesi&oacute;n</button>
          <button className={mode === 'register' ? 'selected' : ''} onClick={() => { setMode('register'); setNotice(''); }}>Crear cuenta mayorista</button>
        </div>
        {mode === 'login' ? <>
          <div className="login-card-heading"><h2>Ingreso de clientes</h2><p>Ingres&aacute; tu CUIT o correo comercial registrado.</p></div>
          <form onSubmit={submit}>
            <label>CUIT o correo<input name="email" type="email" required placeholder="comercio@correo.com" /></label>
            <label><span className="label-row">Contrase&ntilde;a <button type="button" onClick={() => setNotice('Solicit&aacute; la recuperaci&oacute;n de tu clave al equipo de Atenci&oacute;n Mayorista.')}>&iquest;Olvidaste tu clave?</button></span>
              <span className="password-field"><input name="password" required minLength={4} type={visible ? 'text' : 'password'} placeholder="••••••••" /><button type="button" aria-label={visible ? 'Ocultar contrasena' : 'Mostrar contrasena'} onClick={() => setVisible(!visible)}>{visible ? <EyeOff /> : <Eye />}</button></span>
            </label>
            <label className="remember"><input type="checkbox" /> Recordar en este dispositivo</label>
            <button className="login-submit">Ingresar al portal <ArrowRight /></button>
          </form>
          <button className="whatsapp-access" onClick={() => setNotice('Contacta a Atencion Mayorista para solicitar un acceso rapido.')}><MessageCircle /> Acceso rapido via WhatsApp</button>
          <p className="login-assurance">Habilitacion automatica tras validacion fiscal.</p>
          <button className="staff-access" onClick={onStaff}>&iquest;Trabajas en deposito? <b>Abrir centro de mando &rarr;</b></button>
        </> : <>
          <div className="login-card-heading"><h2>Alta comercial mayorista</h2><p>Completa los datos para habilitar precios por bulto.</p></div>
          <form onSubmit={submit}>
            <label>Razon social / fantasia<input required placeholder="Ej: Distribuidora Central" /></label>
            <div className="form-columns"><label>CUIT<input required inputMode="numeric" placeholder="30712345678" /></label><label>Rubro<select defaultValue="kiosco"><option value="kiosco">Kiosco / Drugstore</option><option value="autoservicio">Autoservicio / Almacen</option><option value="distribuidor">Distribuidor</option></select></label></div>
            <label>WhatsApp de contacto<input required type="tel" placeholder="+54 9 261 4123456" /></label>
            <label>Correo comercial<input name="email" required type="email" placeholder="nombre@comercio.com" /></label>
            <label>Contrasena<input name="password" required type="password" minLength={6} placeholder="Al menos 6 caracteres" /></label>
            <button className="login-submit">Solicitar alta mayorista <Check /></button>
          </form>
          <button className="staff-access" onClick={onStaff}>&iquest;Trabajas en deposito? <b>Abrir centro de mando &rarr;</b></button>
        </>}
        {notice && <p className="login-notice" role="status">{notice}</p>}
      </section>
    </div>
    <footer className="login-footer">
      <a className="brand" href="#inicio"><span className="brand-mark">DM</span><span className="brand-copy"><strong>Dulce Mayor</strong><small>MAYORISTA B2B &middot; MENDOZA</small></span></a>
      <span>&copy; 2025 Dulce Mayorista Mendoza S.A. &middot; CUIT 30-71234567-8</span>
      <a href="tel:+542614250800"><Headphones /> Soporte comercial</a>
    </footer>
  </main>;
}
