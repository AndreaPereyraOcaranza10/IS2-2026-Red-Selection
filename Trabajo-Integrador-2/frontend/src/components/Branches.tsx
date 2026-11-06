import { useEffect, useState } from 'react';
import { ImagePlus, LoaderCircle, MapPin } from 'lucide-react';
import { api, type CompanyBranch } from '../lib/api';

export function Branches() {
  const [branches, setBranches] = useState<CompanyBranch[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.publicBranches().then(setBranches).catch(() => setBranches([])).finally(() => setLoading(false));
  }, []);

  return <section className="branches-section" id="sucursales">
    <div className="section-heading branch-heading">
      <div><span className="section-kicker">CERCA DE TU NEGOCIO</span><h2>Nuestras sucursales.</h2></div>
    </div>
    {loading ? <div className="branch-loading"><LoaderCircle className="spin" /> Cargando sucursales...</div> : branches.length ?
      <div className="public-branch-grid">{branches.map(branch => <article className="public-branch-card" key={branch.id}>
        <div className="branch-photo-slot" aria-label="Espacio reservado para la foto de la sucursal"><ImagePlus size={28} /><strong>Foto de la sucursal</strong><span>Espacio reservado</span></div>
        <div className="public-branch-copy"><h3>{branch.razonSocial}</h3><p><MapPin size={15} /> {branch.calle} {branch.numeracion}{branch.barrio ? `, ${branch.barrio}` : ''}</p><small>{branch.localidad}, {branch.departamento}, {branch.provincia}</small>{branch.email && <a href={`mailto:${branch.email}`}>{branch.email}</a>}</div>
      </article>)}</div> :
      <article className="branch-placeholder">
        <div className="branch-photo-slot" aria-label="Espacio reservado para la foto de la sucursal"><ImagePlus size={30} /><strong>Foto de la sucursal</strong><span>Espacio reservado</span></div>
        <div className="branch-placeholder-copy"><span className="branch-pending"><MapPin size={14} /> Datos pendientes</span><h3>{'Proximamente'}</h3><p>{'Publicaremos las sucursales cuando se registren sus direcciones y horarios.'}</p></div>
      </article>}
  </section>;
}
