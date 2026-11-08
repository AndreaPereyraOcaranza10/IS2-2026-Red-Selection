import { useCallback, useEffect, useMemo, useState } from 'react';
import { AlertTriangle, Check, ImagePlus, LoaderCircle, Pencil, Plus, Search, Trash2, X } from 'lucide-react';
import '../styles/products-admin.css';

const money = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 2 });
const emptyProduct = () => ({ codigo: '', nombre: '', descripcion: '', marca: '', presentacion: 'BULTO', unidadesPorPresentacion: 1, stockMinimo: 0, enOferta: false, subCategoriaId: '', precioInicial: '', stockInicial: 0 });

function ProductDialog({ apiAdmin, product, categories, subcategories, onClose, onSave }) {
  const [form, setForm] = useState(() => product ? {
    codigo: product.codigo ?? '', nombre: product.nombre ?? '', descripcion: product.descripcion ?? '', marca: product.marca ?? '',
    presentacion: product.presentacion ?? 'BULTO', unidadesPorPresentacion: product.unidadesPorPresentacion ?? 1,
    stockMinimo: product.stockMinimo ?? 0, enOferta: Boolean(product.enOferta), subCategoriaId: product.subCategoriaId ?? '',
    precioInicial: product.precio ?? '', stockInicial: product.stockActual ?? 0,
  } : emptyProduct());
  const [file, setFile] = useState(null);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [categoryId, setCategoryId] = useState(product?.categoriaId ?? '');
  const options = subcategories.filter((subcategory) => !categoryId || subcategory.categoriaId === categoryId);
  const set = (key, value) => setForm((current) => ({ ...current, [key]: value }));
  const submit = async (event) => {
    event.preventDefault(); setSaving(true); setError('');
    try {
      let imageId = product?.imagenId ?? null;
      if (file) imageId = (await apiAdmin.uploadImage(file)).id;
      const payload = {
        ...form,
        codigo: form.codigo.trim(), nombre: form.nombre.trim(), descripcion: form.descripcion.trim(), marca: form.marca.trim(),
        unidadesPorPresentacion: Number(form.unidadesPorPresentacion), stockMinimo: Number(form.stockMinimo),
        subCategoriaId: form.subCategoriaId, imagenId: imageId,
        precioInicial: Number(form.precioInicial), stockInicial: Number(form.stockInicial),
      };
      await onSave(payload, product);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo guardar el producto.');
    } finally { setSaving(false); }
  };
  return <div className="product-dialog-backdrop" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
    <section className="product-dialog" role="dialog" aria-modal="true" aria-labelledby="product-dialog-title">
      <header><div><span className="section-kicker">INVENTARIO</span><h2 id="product-dialog-title">{product ? 'Editar producto' : 'Nuevo producto'}</h2></div><button className="product-icon-button" type="button" onClick={onClose} aria-label="Cerrar"><X /></button></header>
      <form onSubmit={submit}>
        <div className="product-form-grid">
          <label>Código<input required maxLength="60" value={form.codigo} disabled={Boolean(product)} onChange={(e) => set('codigo', e.target.value)} placeholder="DM-001" /></label>
          <label>Nombre<input required maxLength="160" value={form.nombre} onChange={(e) => set('nombre', e.target.value)} /></label>
          <label>Marca<input maxLength="100" value={form.marca} onChange={(e) => set('marca', e.target.value)} /></label>
          <label>Presentación<select value={form.presentacion} onChange={(e) => set('presentacion', e.target.value)}><option value="BULTO">Bulto</option><option value="PACK">Pack</option></select></label>
          <label>Unidades por presentación<input type="number" min="1" required value={form.unidadesPorPresentacion} onChange={(e) => set('unidadesPorPresentacion', e.target.value)} /></label>
          <label>Categoría<select required value={categoryId} onChange={(e) => { setCategoryId(e.target.value); set('subCategoriaId', ''); }}><option value="">Seleccioná una categoría</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.nombre}</option>)}</select></label>
          <label>Subcategoría<select required value={form.subCategoriaId} onChange={(e) => set('subCategoriaId', e.target.value)}><option value="">Seleccioná una subcategoría</option>{options.map((subcategory) => <option key={subcategory.id} value={subcategory.id}>{subcategory.nombre}</option>)}</select></label>
          <label>Precio mayorista (ARS)<input type="number" min="0.01" step="0.01" required value={form.precioInicial} onChange={(e) => set('precioInicial', e.target.value)} /></label>
          {!product && <label>Stock inicial<input type="number" min="0" step="1" required value={form.stockInicial} onChange={(e) => set('stockInicial', e.target.value)} /></label>}
          <label>Alerta de stock mínimo<input type="number" min="0" step="1" required value={form.stockMinimo} onChange={(e) => set('stockMinimo', e.target.value)} /></label>
          <label className="product-image-input"><span>Imagen del producto</span><span className="product-file-picker"><ImagePlus />{file?.name ?? (product?.imagenId ? 'Reemplazar imagen' : 'Elegir PNG, JPG o WEBP')}</span><input type="file" accept="image/png,image/jpeg,image/webp" onChange={(e) => setFile(e.target.files?.[0] ?? null)} /><small>Máximo 2 MB</small></label>
          <label className="product-description">Descripción<textarea rows="3" maxLength="1000" value={form.descripcion} onChange={(e) => set('descripcion', e.target.value)} /></label>
          <label className="product-checkbox"><input type="checkbox" checked={form.enOferta} onChange={(e) => set('enOferta', e.target.checked)} /> Destacar como oferta</label>
        </div>
        {error && <p className="product-form-error" role="alert">{error}</p>}
        <footer><button type="button" className="btn btn-s" onClick={onClose}>Cancelar</button><button className="btn btn-p" disabled={saving}>{saving ? <><LoaderCircle className="spin" /> Guardando…</> : <><Check /> Guardar producto</>}</button></footer>
      </form>
    </section>
  </div>;
}

function TaxonomyDialog({ kind, entry, categories, onClose, onSave }) {
  const [name, setName] = useState(entry?.nombre ?? '');
  const [categoryId, setCategoryId] = useState(entry?.categoriaId ?? categories[0]?.id ?? '');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const title = kind === 'category' ? 'categoría' : 'subcategoría';
  const submit = async (event) => {
    event.preventDefault(); setSaving(true); setError('');
    try { await onSave(kind, entry, kind === 'category' ? { nombre: name.trim() } : { nombre: name.trim(), categoriaId: categoryId }); }
    catch (cause) { setError(cause instanceof Error ? cause.message : `No se pudo guardar la ${title}.`); }
    finally { setSaving(false); }
  };
  return <div className="product-dialog-backdrop" onMouseDown={(event) => event.target === event.currentTarget && onClose()}><section className="product-dialog taxonomy-dialog" role="dialog" aria-modal="true" aria-labelledby="taxonomy-title">
    <header><div><span className="section-kicker">CATÁLOGO</span><h2 id="taxonomy-title">{entry ? 'Editar' : 'Nueva'} {title}</h2></div><button className="product-icon-button" type="button" onClick={onClose} aria-label="Cerrar"><X /></button></header>
    <form onSubmit={submit}><label>Nombre<input required maxLength="120" value={name} onChange={(e) => setName(e.target.value)} /></label>
      {kind === 'subcategory' && <label>Categoría<select required value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>{categories.map((category) => <option key={category.id} value={category.id}>{category.nombre}</option>)}</select></label>}
      {error && <p className="product-form-error" role="alert">{error}</p>}<footer><button className="btn btn-s" type="button" onClick={onClose}>Cancelar</button><button className="btn btn-p" disabled={saving}>{saving ? 'Guardando…' : 'Guardar'}</button></footer>
    </form>
  </section></div>;
}

export function ProductsAdminPage({ apiAdmin, onStockChange, stockOnly = false }) {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [subcategories, setSubcategories] = useState([]);
  const [query, setQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [dialog, setDialog] = useState(null);
  const [taxonomyDialog, setTaxonomyDialog] = useState(null);
  const [busyId, setBusyId] = useState('');
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [loading, setLoading] = useState(true);
  const [stockDraft, setStockDraft] = useState({});

  const load = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const [productRows, categoryRows, subcategoryRows] = await Promise.all([apiAdmin.productRecords(), apiAdmin.categories(), apiAdmin.subcategories()]);
      setProducts(productRows); setCategories(categoryRows); setSubcategories(subcategoryRows);
    } catch (cause) { setError(cause instanceof Error ? cause.message : 'No se pudo cargar el catálogo.'); }
    finally { setLoading(false); }
  }, []);
  useEffect(() => { void load(); }, [load]);

  const visible = useMemo(() => products.filter((product) => {
    const searchText = `${product.codigo} ${product.nombre} ${product.marca ?? ''} ${product.categoria} ${product.subCategoria}`.toLocaleLowerCase();
    const categoryMatches = !categoryFilter || product.categoriaId === categoryFilter;
    const stockMatches = !stockOnly || Number(product.stockActual) <= Number(product.stockMinimo);
    return categoryMatches && stockMatches && searchText.includes(query.toLocaleLowerCase());
  }), [products, subcategories, categoryFilter, stockOnly, query]);

  const saveProduct = async (payload, current) => {
    if (current) {
      await apiAdmin.updateProduct(current.id, { ...payload, codigo: current.codigo, precioInicial: null, stockInicial: 0 });
      if (Number(payload.precioInicial) !== Number(current.precio)) await apiAdmin.updatePrice(current.id, payload.precioInicial);
      setNotice('Producto actualizado.');
    } else {
      await apiAdmin.createProduct(payload);
      setNotice('Producto creado y agregado al catálogo.');
    }
    setDialog(null); await load();
  };

  const saveTaxonomy = async (kind, entry, payload) => {
    const methods = kind === 'category'
      ? { create: apiAdmin.createCategory, update: apiAdmin.updateCategory }
      : { create: apiAdmin.createSubcategory, update: apiAdmin.updateSubcategory };
    await (entry ? methods.update(entry.id, payload) : methods.create(payload));
    setTaxonomyDialog(null); setNotice(`${kind === 'category' ? 'Categoría' : 'Subcategoría'} ${entry ? 'actualizada' : 'creada'}.`); await load();
  };

  const remove = async (kind, entry) => {
    const label = kind === 'product' ? `el producto «${entry.nombre}»` : `«${entry.nombre}»`;
    if (!window.confirm(`¿Dar de baja ${label}?`)) return;
    setBusyId(entry.id); setError('');
    try {
      if (kind === 'product') await apiAdmin.deleteProduct(entry.id);
      else if (kind === 'category') await apiAdmin.deleteCategory(entry.id);
      else await apiAdmin.deleteSubcategory(entry.id);
      setNotice('Registro dado de baja.'); await load();
    } catch (cause) { setError(cause instanceof Error ? cause.message : 'No se pudo dar de baja.'); }
    finally { setBusyId(''); }
  };

  const saveStock = async (product) => {
    const value = Number(stockDraft[product.id] ?? product.stockActual);
    if (!Number.isInteger(value) || value < 0) { setError('El stock debe ser un número entero igual o mayor a cero.'); return; }
    if (value === product.stockActual) return;
    setBusyId(product.id); setError('');
    try {
      await onStockChange({ id: product.id, name: product.nombre }, value);
      setNotice(value === 0 ? `${product.nombre} quedó sin stock y se oculta del catálogo público.` : 'Stock actualizado.');
      setStockDraft((current) => { const next = { ...current }; delete next[product.id]; return next; }); await load();
    } catch (cause) { setError(cause instanceof Error ? cause.message : 'No se pudo actualizar el stock.'); }
    finally { setBusyId(''); }
  };

  const subcategoriesFor = (categoryId) => subcategories.filter((item) => item.categoriaId === categoryId);

  return <div className="product-admin-page">
    <section className="product-admin-panel">
      <header className="product-admin-heading"><div><span className="section-kicker">INVENTARIO</span><h2>{stockOnly ? 'Stock y alertas' : 'Productos y categorías'}</h2><p>{stockOnly ? 'Controlá existencias y niveles mínimos.' : 'Gestioná el catálogo, las imágenes y la disponibilidad.'}</p></div>
        <div className="product-admin-actions">{!stockOnly && <><button className="btn btn-s" onClick={() => setTaxonomyDialog({ kind: 'category' })}><Plus /> Categoría</button><button className="btn btn-s" onClick={() => setTaxonomyDialog({ kind: 'subcategory' })}><Plus /> Subcategoría</button><button className="btn btn-p" onClick={() => setDialog({ product: null })}><Plus /> Nuevo producto</button></>}</div>
      </header>
      {notice && <p className="product-admin-notice" role="status"><Check />{notice}<button onClick={() => setNotice('')} aria-label="Cerrar aviso"><X /></button></p>}
      {error && <p className="product-admin-error" role="alert"><AlertTriangle />{error}<button onClick={() => void load()}>Reintentar</button></p>}
      <div className="product-admin-toolbar"><label><Search /><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Buscar por código, nombre o marca…" /></label><select aria-label="Filtrar por categoría" value={categoryFilter} onChange={(e) => setCategoryFilter(e.target.value)}><option value="">Todas las categorías</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.nombre}</option>)}</select><span>{visible.length} {visible.length === 1 ? 'producto' : 'productos'}</span></div>
      {loading ? <div className="product-admin-loading"><LoaderCircle className="spin" /> Cargando catálogo…</div> : visible.length ? <div className="product-admin-list">{visible.map((product) => {
        const zero = Number(product.stockActual) === 0;
        const low = !zero && Number(product.stockActual) <= Number(product.stockMinimo);
        const imageUrl = product.imagenId ? apiAdmin.imageUrl(product.imagenId) : '';
        return <article className="product-admin-card" key={product.id}>
          <div className="product-admin-image">{imageUrl ? <img src={imageUrl} alt="" loading="lazy" /> : <ImagePlus aria-hidden="true" />}</div>
          <div className="product-admin-main"><div className="product-admin-title"><h3>{product.nombre}</h3><span className="product-code">{product.codigo}</span></div><p>{product.marca || 'Sin marca'} · {product.categoria} / {product.subCategoria}</p><small>{product.presentacion === 'PACK' ? 'Pack' : 'Bulto'} x {product.unidadesPorPresentacion} unidades {product.enOferta && <b className="product-offer">Oferta</b>}</small></div>
          <div className="product-admin-price"><small>Precio</small><b>{product.precio == null ? 'Sin precio' : money.format(product.precio)}</b></div>
          <div className="product-admin-stock"><small>Existencias · mínimo {product.stockMinimo}</small><div><input aria-label={`Stock de ${product.nombre}`} type="number" min="0" step="1" value={stockDraft[product.id] ?? product.stockActual} onChange={(e) => setStockDraft((current) => ({ ...current, [product.id]: e.target.value }))} /><button className="btn btn-s" disabled={busyId === product.id || Number(stockDraft[product.id] ?? product.stockActual) === product.stockActual} onClick={() => void saveStock(product)}>{busyId === product.id ? '…' : 'Guardar'}</button></div><span className={`product-stock-state ${zero ? 'zero' : low ? 'low' : 'ok'}`}>{zero ? 'Sin stock · oculto del sitio' : low ? 'Stock bajo' : 'Disponible'}</span></div>
          <div className="product-admin-controls"><button className="product-icon-button" title="Editar producto" aria-label={`Editar ${product.nombre}`} onClick={() => setDialog({ product })}><Pencil /></button><button className="product-icon-button danger" title="Dar de baja" aria-label={`Dar de baja ${product.nombre}`} disabled={busyId === product.id} onClick={() => void remove('product', product)}><Trash2 /></button></div>
        </article>;
      })}</div> : <div className="product-admin-empty">{products.length ? 'No hay productos que coincidan con estos filtros.' : 'Todavía no hay productos registrados.'}</div>}
    </section>
    {!stockOnly && <details className="taxonomy-panel"><summary>Administrar categorías y subcategorías <span>{categories.length} categorías</span></summary><div className="taxonomy-list">{categories.map((category) => <article key={category.id}><div><b>{category.nombre}</b><small>{subcategoriesFor(category.id).length} subcategorías</small></div><div className="taxonomy-controls"><button className="product-icon-button" aria-label={`Editar ${category.nombre}`} onClick={() => setTaxonomyDialog({ kind: 'category', entry: category })}><Pencil /></button><button className="product-icon-button danger" aria-label={`Dar de baja ${category.nombre}`} disabled={busyId === category.id} onClick={() => void remove('category', category)}><Trash2 /></button><button className="btn btn-s" onClick={() => setTaxonomyDialog({ kind: 'subcategory', entry: { categoriaId: category.id } })}><Plus /> Subcategoría</button></div><div className="taxonomy-subcategories">{subcategoriesFor(category.id).map((subcategory) => <span key={subcategory.id}>{subcategory.nombre}<button aria-label={`Editar ${subcategory.nombre}`} onClick={() => setTaxonomyDialog({ kind: 'subcategory', entry: subcategory })}><Pencil /></button><button aria-label={`Dar de baja ${subcategory.nombre}`} onClick={() => void remove('subcategory', subcategory)}><Trash2 /></button></span>)}</div></article>)}</div></details>}
    {dialog && <ProductDialog apiAdmin={apiAdmin} product={dialog.product} categories={categories} subcategories={subcategories} onClose={() => setDialog(null)} onSave={saveProduct} />}
    {taxonomyDialog && <TaxonomyDialog kind={taxonomyDialog.kind} entry={taxonomyDialog.entry} categories={categories} onClose={() => setTaxonomyDialog(null)} onSave={saveTaxonomy} />}
  </div>;
}
