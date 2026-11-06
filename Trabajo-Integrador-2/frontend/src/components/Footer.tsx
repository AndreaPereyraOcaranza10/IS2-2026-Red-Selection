import { Instagram, MapPin, Phone } from 'lucide-react';

export function Footer() {
  return <footer className="site-footer" id="ayuda"><div className="footer-main"><a className="brand footer-brand" href="#inicio"><span className="brand-mark">DM</span><span className="brand-copy"><strong>Dulce Mayor</strong><small>MAYORISTA B2B · MENDOZA</small></span></a><p>El socio mayorista de los kioscos<br />y comercios de Mendoza.</p><div className="footer-contact"><a href="tel:+542614250800"><Phone size={16} /> (0261) 425-0800</a><span><MapPin size={16} /> Mendoza, Argentina</span></div><a className="social" href="#ayuda" aria-label="Instagram"><Instagram size={19} /></a></div><div className="footer-bottom"><span>© 2025 Dulce Mayor. Todos los derechos reservados.</span><span>Hecho en Mendoza, con dulzura. <b>♥</b></span></div></footer>;
}
