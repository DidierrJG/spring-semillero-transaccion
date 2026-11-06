/* =====================================================
   CONFIGURACIÓN: cambia por la URL de tu API
   ===================================================== */
const API_URL = '/api/semilleros';

/* ---------- Utilidades ---------- */
const $ = (s, el = document) => el.querySelector(s);
const $$ = (s, el = document) => [...el.querySelectorAll(s)];

function toast(msg, type = '') {
    const t = $('#toast');
    t.textContent = msg;
    t.className = 'show ' + type;
    clearTimeout(t._t);
    t._t = setTimeout(() => (t.className = ''), 3200);
}

function escapeHtml(str) {
    return String(str ?? '').replace(/[&<>"']/g, c => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
}

/* ---------- Líneas dinámicas ---------- */
function addLinea(value = '') {
    const row = document.createElement('div');
    row.className = 'row-item';
    row.innerHTML = `
    <input class="linea-input" placeholder="Ej: Aplicaciones Web" value="${escapeHtml(value)}">
    <button type="button" class="btn btn-icon" title="Quitar línea" aria-label="Quitar línea">✕</button>`;
    $('button', row).onclick = () => row.remove();
    $('#lineasContainer').appendChild(row);
}

/* ---------- Integrantes dinámicos ---------- */
function addIntegrante() {
    const box = document.createElement('div');
    box.className = 'integrante';
    box.innerHTML = `
    <div class="top-row"><b>Integrante</b>
      <button type="button" class="btn btn-icon" title="Quitar integrante" aria-label="Quitar integrante">✕</button>
    </div>
    <div class="grid-2">
      <div class="field"><label>Código estudiantil</label><input class="i-codigo" placeholder="2026001"></div>
      <div class="field"><label>Nombre</label><input class="i-nombre" placeholder="Ana Ejemplo"></div>
    </div>
    <div class="field"><label>Programa académico</label><input class="i-programa" placeholder="Ingeniería de Sistemas"></div>
    <label class="lider-label"><input type="radio" name="lider" class="i-lider"> Líder del semillero</label>`;
    $('button', box).onclick = () => { box.remove(); renumber(); };
    $('#integrantesContainer').appendChild(box);
    renumber();
}

function renumber() {
    const boxes = $$('.integrante');
    boxes.forEach((b, i) => ($('.top-row b', b).textContent = `Integrante #${i + 1}`));
    // siempre debe haber exactamente un líder marcado
    if (boxes.length && !boxes.some(b => $('.i-lider', b).checked)) {
        $('.i-lider', boxes[0]).checked = true;
    }
}

/* ---------- Recoger datos (misma forma que SemilleroRequest) ---------- */
function collectData() {
    return {
        nombre: $('#nombre').value.trim(),
        sigla: $('#sigla').value.trim(),
        grupoInvestigacion: $('#grupoInvestigacion').value.trim(),
        mision: $('#mision').value.trim(),
        vision: $('#vision').value.trim(),
        objetivoGeneral: $('#objetivoGeneral').value.trim(),
        director: {
            nombre: $('#dirNombre').value.trim(),
            correo: $('#dirCorreo').value.trim()
        },
        lineas: $$('.linea-input').map(i => i.value.trim()).filter(Boolean),
        integrantes: $$('.integrante').map(b => ({
            codigo: $('.i-codigo', b).value.trim(),
            nombre: $('.i-nombre', b).value.trim(),
            programa: $('.i-programa', b).value.trim(),
            esLider: $('.i-lider', b).checked
        }))
    };
}

/* ---------- Validación en cliente ---------- */
function validate(d) {
    let ok = true;
    const mark = (id, bad) => {
        $('#' + id).closest('.field').classList.toggle('invalid', bad);
        if (bad) ok = false;
    };
    mark('nombre', !d.nombre);
    mark('sigla', !d.sigla);
    mark('grupoInvestigacion', !d.grupoInvestigacion);
    mark('dirNombre', !d.director.nombre);
    mark('dirCorreo', !d.director.correo);

    const lErr = d.lineas.length === 0;
    $('#lineasError').style.display = lErr ? 'block' : 'none';

    let iMsg = '';
    if (d.integrantes.length === 0) iMsg = 'Debe existir al menos un integrante';
    else if (d.integrantes.some(i => !i.codigo || !i.nombre || !i.programa))
        iMsg = 'Completa código, nombre y programa de cada integrante';
    else if (d.integrantes.filter(i => i.esLider).length !== 1)
        iMsg = 'Debe haber exactamente un líder';
    const iErr = iMsg !== '';
    $('#integrantesError').textContent = iMsg;
    $('#integrantesError').style.display = iErr ? 'block' : 'none';

    return ok && !lErr && !iErr;
}

/* ---------- POST: crear semillero ---------- */
async function crearSemillero(e) {
    e.preventDefault();
    const data = collectData();
    if (!validate(data)) { toast('Revisa los campos obligatorios', 'err'); return; }

    const btn = $('#submitBtn');
    btn.disabled = true; btn.textContent = 'Registrando…';
    try {
        const res = await fetch(API_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            console.error('Respuesta del servidor:', res.status, err);
            // ErrorResponse del backend: { mensaje, detalles: [] }
            const detalle = Array.isArray(err.detalles) && err.detalles.length
                ? ' — ' + err.detalles.join('; ') : '';
            throw new Error((err.mensaje || `Error ${res.status}`) + detalle);
        }
        toast('Semillero registrado correctamente', 'ok');
        resetForm();
        cargarSemilleros();
    } catch (err) {
        console.error(err);
        toast(err.message === 'Failed to fetch' ? 'No se pudo conectar con la API' : err.message, 'err');
    } finally {
        btn.disabled = false; btn.textContent = 'Registrar semillero';
    }
}

function resetForm() {
    $('#semilleroForm').reset();
    $('#lineasContainer').innerHTML = '';
    $('#integrantesContainer').innerHTML = '';
    $$('.field.invalid').forEach(f => f.classList.remove('invalid'));
    addLinea(); addIntegrante();
}

/* ---------- GET: listar semilleros ---------- */
let semilleros = [];

async function cargarSemilleros() {
    try {
        const res = await fetch(API_URL);
        if (!res.ok) throw new Error(`Error ${res.status}`);
        semilleros = await res.json();
        renderSemilleros();
    } catch (err) {
        $('#semillerosList').innerHTML = `<div class="empty"><span class="big">⚠️</span>No se pudo cargar la lista</div>`;
        $('#countLabel').textContent = 'Sin conexión con la API';
    }
}

/* ---------- Emblema por área ----------
   Se detecta el área por palabras clave en la sigla (ej. "IA-01", "SAW-02").
   Agrega las tuyas en AREAS. */
const ICONOS = {
    ia: '<circle cx="6" cy="6" r="2"/><circle cx="18" cy="6" r="2"/><circle cx="12" cy="11" r="2"/><circle cx="12" cy="19" r="2"/><path d="M7.6 7.4l3 2.4M16.4 7.4l-3 2.4M12 13v4"/>',
    web: '<path d="M8 8l-5 4 5 4M16 8l5 4-5 4M14 5l-4 14"/>',
    datos: '<ellipse cx="12" cy="6" rx="8" ry="3"/><path d="M4 6v6c0 1.7 3.6 3 8 3s8-1.3 8-3V6M4 12v6c0 1.7 3.6 3 8 3s8-1.3 8-3v-6"/>',
    redes: '<circle cx="12" cy="5" r="2"/><circle cx="5" cy="19" r="2"/><circle cx="19" cy="19" r="2"/><path d="M12 7v5M12 12l-6 5M12 12l6 5"/>',
    seguridad: '<path d="M12 3l8 3v6c0 4.5-3.2 8-8 9-4.8-1-8-4.5-8-9V6z"/><path d="M9 12l2 2 4-4"/>',
    robotica: '<rect x="5" y="8" width="14" height="11" rx="2"/><path d="M12 8V4M9 13h.01M15 13h.01M9 16h6"/>',
    ciencia: '<path d="M9 3h6M10 3v6l-5 9a2 2 0 0 0 2 3h10a2 2 0 0 0 2-3l-5-9V3M8 15h8"/>'
};
const AREAS = [
    ['ia', ['IA', 'AI', 'ML', 'SIA', 'INTELIG']],
    ['web', ['WEB', 'SAW', 'WWW', 'SW', 'DEV', 'SOFT']],
    ['datos', ['BD', 'DATA', 'DATOS', 'BIG']],
    ['redes', ['RED', 'NET', 'IOT', 'TEL']],
    ['seguridad', ['SEG', 'SEC', 'CIBER']],
    ['robotica', ['ROB', 'MEC', 'AUTO']]
];

function areaDe(sigla) {
    const tokens = String(sigla).toUpperCase().split(/[^A-ZÁÉÍÓÚÑ]+/).filter(Boolean);
    for (const [area, claves] of AREAS) {
        if (tokens.some(t => claves.some(c => t === c || (c.length > 3 && t.includes(c))))) return area;
    }
    return 'ciencia';
}

// Matiz estable a partir de la sigla: cada semillero siempre tiene su color
function matizDe(texto) {
    let h = 0;
    for (const ch of String(texto)) h = (h * 31 + ch.charCodeAt(0)) >>> 0;
    return 170 + (h % 160); // del verde azulado al magenta
}

function renderSemilleros() {
    const list = $('#semillerosList');
    const q = $('#buscar').value.trim().toLowerCase();
    const items = semilleros.filter(s => String(s.sigla).toLowerCase().includes(q));

    const totalInt = semilleros.reduce((n, s) => n + (Number(s.totalIntegrantes) || 0), 0);
    $('#statSem').textContent = semilleros.length;
    $('#statInt').textContent = totalInt;
    $('#countLabel').textContent = q
        ? `${items.length} de ${semilleros.length} semilleros`
        : `${semilleros.length} semillero${semilleros.length === 1 ? '' : 's'}`;

    if (!semilleros.length) {
        list.innerHTML = `<div class="empty"><span class="big">🔬</span>Aún no hay semilleros. Registra el primero con el formulario.</div>`;
        return;
    }
    if (!items.length) {
        list.innerHTML = `<div class="empty"><span class="big">🔎</span>Ningún semillero coincide con “${escapeHtml(q)}”</div>`;
        return;
    }
    list.innerHTML = items.map(s => `
    <article class="sem-card" tabindex="0" role="button" aria-label="Ver detalle de ${escapeHtml(s.sigla)}" data-id="${escapeHtml(s.id)}" data-sigla="${escapeHtml(s.sigla)}" style="--h:${matizDe(s.sigla)}">
      <div class="emblem">
        <span class="sem-id">#${escapeHtml(s.id)}</span>
        <button type="button" class="btn-delete" data-id="${escapeHtml(s.id)}" title="Eliminar semillero" aria-label="Eliminar semillero ${escapeHtml(s.sigla)}">
          <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
          </svg>
        </button>
        <svg class="icono" viewBox="0 0 24 24" aria-hidden="true">${ICONOS[areaDe(s.sigla)]}</svg>
      </div>
      <div class="sem-body">
        <h3 class="sem-sigla">${escapeHtml(s.sigla)}</h3>
        <div class="sem-meta">
          <span class="chip">👥 ${escapeHtml(s.totalIntegrantes)} integrantes</span>
          <span class="badge">${escapeHtml(s.estado)}</span>
        </div>
      </div>
    </article>`).join('');
}

/* ---------- DELETE: eliminar semillero ---------- */
async function eliminarSemillero(id, btn) {
    if (!confirm('¿Eliminar este semillero? Esta acción no se puede deshacer.')) return;
    btn.disabled = true;
    try {
        const res = await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            console.error('Respuesta del servidor:', res.status, err);
            throw new Error(err.mensaje || `Error ${res.status}`);
        }
        toast('Semillero eliminado', 'ok');
        cargarSemilleros();
    } catch (err) {
        console.error(err);
        toast(err.message === 'Failed to fetch' ? 'No se pudo conectar con la API' : err.message, 'err');
        btn.disabled = false;
    }
}

/* ---------- GET por id: detalle del semillero ----------
   Usa GET {API_URL}/{id}. Se leen los mismos nombres de campo que envía el
   formulario (nombre, grupoInvestigacion, mision, vision, objetivoGeneral,
   director, lineas, integrantes). Si tu respuesta usa otros, ajusta aquí. */
const dlg = $('#detalle');
const txt = v => (v === undefined || v === null || v === '') ? '<span class="d-vacio">Sin información</span>' : escapeHtml(v);
const textoDe = x => typeof x === 'string' ? x : (x?.nombre ?? x?.descripcion ?? x?.titulo ?? '');

function bloque(titulo, contenido) {
    return `<section class="d-sec"><h3>${titulo}</h3>${contenido}</section>`;
}

function renderDetalle(d) {
    const dir = d.director ?? d.docenteDirector ?? {};
    const lineas = (d.lineas ?? d.lineasInvestigacion ?? []).map(textoDe).filter(Boolean);
    const integrantes = d.integrantes ?? [];

    $('#detalleBody').innerHTML = `
    <div class="d-nombre">${txt(d.nombre)}</div>
    <div class="d-meta">
      <span>${txt(d.grupoInvestigacion)}</span>
      ${d.estado ? `<span class="badge">${escapeHtml(d.estado)}</span>` : ''}
    </div>
    ${bloque('Misión', `<p>${txt(d.mision)}</p>`)}
    ${bloque('Visión', `<p>${txt(d.vision)}</p>`)}
    ${bloque('Objetivo general', `<p>${txt(d.objetivoGeneral)}</p>`)}
    ${bloque('Docente director', `
      <div class="d-dir"><b>${txt(dir.nombre)}</b>
      ${dir.correo ? `<a href="mailto:${escapeHtml(dir.correo)}">${escapeHtml(dir.correo)}</a>` : ''}</div>`)}
    ${bloque('Líneas de investigación', lineas.length
        ? `<ul class="d-lineas">${lineas.map(l => `<li>${escapeHtml(l)}</li>`).join('')}</ul>`
        : '<p class="d-vacio">Sin líneas registradas</p>')}
    ${bloque(`Integrantes (${integrantes.length})`, integrantes.length ? `
      <table class="d-tabla">
        <thead><tr><th>Código</th><th>Nombre</th><th>Programa</th></tr></thead>
        <tbody>${integrantes.map(i => `
          <tr><td>${txt(i.codigo)}</td>
          <td>${txt(i.nombre)}${(i.esLider ?? i.lider) ? '<span class="d-lider">Líder</span>' : ''}</td>
          <td>${txt(i.programa)}</td></tr>`).join('')}
        </tbody>
      </table>` : '<p class="d-vacio">Sin integrantes registrados</p>')}`;
}

async function verDetalle(card) {
    dlg.style.setProperty('--h', card.style.getPropertyValue('--h'));
    $('#detalleTitulo').textContent = card.dataset.sigla;
    $('#detalleBody').innerHTML = '<div class="loading">Cargando información…</div>';
    dlg.showModal();
    try {
        const res = await fetch(`${API_URL}/${card.dataset.id}`);
        if (!res.ok) throw new Error(`Error ${res.status}`);
        const d = await res.json();
        console.log('Detalle del semillero:', d);
        renderDetalle(d);
    } catch (err) {
        console.error(err);
        $('#detalleBody').innerHTML = `<div class="empty"><span class="big">⚠️</span>No se pudo cargar el detalle (${escapeHtml(err.message)}).<br>Revisa que exista GET ${escapeHtml(API_URL)}/{id}.</div>`;
    }
}

// Un solo listener para borrar y abrir detalle (las tarjetas se redibujan)
$('#semillerosList').addEventListener('click', e => {
    const btn = e.target.closest('.btn-delete');
    if (btn) { eliminarSemillero(btn.dataset.id, btn); return; }
    const card = e.target.closest('.sem-card');
    if (card) verDetalle(card);
});
$('#semillerosList').addEventListener('keydown', e => {
    if (e.key !== 'Enter' || e.target.closest('.btn-delete')) return;
    const card = e.target.closest('.sem-card');
    if (card) verDetalle(card);
});

// Cerrar el detalle: botón, clic fuera o Esc (nativo)
$('#cerrarDetalle').onclick = () => dlg.close();
dlg.addEventListener('click', e => { if (e.target === dlg) dlg.close(); });

/* ---------- Init ---------- */
$('#addLinea').onclick = () => addLinea();
$('#addIntegrante').onclick = addIntegrante;
$('#refreshBtn').onclick = cargarSemilleros;
$('#buscar').addEventListener('input', renderSemilleros);
$('#semilleroForm').addEventListener('submit', crearSemillero);

addLinea();
addIntegrante();
cargarSemilleros();