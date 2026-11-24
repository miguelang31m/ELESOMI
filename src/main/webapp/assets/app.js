"use strict";
const API = "api/pasajeros";
const $ = id => document.getElementById(id);
let pagina = 1;
let total = 0;
const tamano = 10;
let temporizadorToast;
let temporizadorBusqueda;

async function solicitar(url, opciones = {}) {
  const respuesta = await fetch(url, {headers: {"Accept": "application/json", ...opciones.headers}, ...opciones});
  if (!respuesta.ok) {
    let mensaje = `Error HTTP ${respuesta.status}`;
    try { const error = await respuesta.json(); mensaje = error.mensaje || mensaje; } catch (_) { /* respuesta no JSON */ }
    throw new Error(mensaje);
  }
  if (respuesta.status === 204) return null;
  return respuesta.json();
}
function mostrarToast(texto) {
  const t = $("toast"); t.textContent = texto; t.hidden = false;
  clearTimeout(temporizadorToast); temporizadorToast = setTimeout(() => t.hidden = true, 3500);
}
function celda(tr, contenido, fuerte = false) {
  const td = document.createElement("td");
  if (fuerte) { const b = document.createElement("strong"); b.textContent = contenido; td.append(b); }
  else td.textContent = contenido || "—";
  tr.append(td);
  return td;
}
function boton(texto, clase, simbolo, accion) {
  const b = document.createElement("button"); b.type = "button";
  b.className = `icon-button ${clase}`; b.title = texto; b.setAttribute("aria-label", texto);
  const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
  svg.setAttribute("class", "icon"); svg.setAttribute("aria-hidden", "true");
  const use = document.createElementNS("http://www.w3.org/2000/svg", "use");
  use.setAttribute("href", `assets/icons.svg#${simbolo}`); svg.append(use); b.append(svg);
  b.addEventListener("click", accion); return b;
}
async function cargar() {
  const tbody = $("filas"); tbody.replaceChildren();
  const trCargando = document.createElement("tr"); const td = celda(trCargando, "Cargando registros…");
  td.colSpan = 6; td.className = "empty"; tbody.append(trCargando);
  try {
    const q = encodeURIComponent($("busqueda").value.trim());
    const datos = await solicitar(`${API}?q=${q}&pagina=${pagina}&tamano=${tamano}`);
    total = datos.total;
    $("resumen").textContent = `${datos.items.length} DE ${total} PASAJEROS`;
    $("pagina").textContent = `${pagina.toString().padStart(2,"0")} / ${Math.max(1, Math.ceil(total / tamano)).toString().padStart(2,"0")}`;
    $("anterior").disabled = pagina <= 1;
    $("siguiente").disabled = pagina * tamano >= total;
    tbody.replaceChildren();
    if (!datos.items.length) {
      const tr = document.createElement("tr"); const vacio = celda(tr, "No se encontraron pasajeros.");
      vacio.colSpan = 6; vacio.className = "empty"; tbody.append(tr); return;
    }
    for (const p of datos.items) {
      const tr = document.createElement("tr");
      celda(tr, `${p.nombres} ${p.apellidos}`, true);
      celda(tr, `${p.tipoDocumento} · ${p.numeroDocumento}`);
      celda(tr, p.correo); celda(tr, p.telefono); celda(tr, p.nacionalidad);
      const acciones = document.createElement("td"); acciones.className = "actions";
      const grupo = document.createElement("div"); grupo.className = "action-group";
      grupo.append(boton(`Editar a ${p.nombres} ${p.apellidos}`, "", "edit", () => editar(p.id)));
      grupo.append(boton(`Desactivar a ${p.nombres} ${p.apellidos}`, "danger", "disable", () => desactivar(p.id, `${p.nombres} ${p.apellidos}`)));
      acciones.append(grupo);
      tr.append(acciones); tbody.append(tr);
    }
  } catch (e) {
    tbody.replaceChildren(); const tr = document.createElement("tr");
    const td = celda(tr, `No se pudieron cargar los registros: ${e.message}`);
    td.colSpan = 6; td.className = "empty"; tbody.append(tr);
    mostrarToast(e.message);
  }
}
function abrir() { $("modal").hidden = false; document.body.style.overflow = "hidden"; }
function cerrar() { $("modal").hidden = true; document.body.style.overflow = ""; $("form-error").hidden = true; }
function nuevo() {
  $("formulario").reset(); $("id").value = "";
  $("modal-titulo").textContent = "Nuevo pasajero"; $("guardar").textContent = "Guardar pasajero";
  abrir();
}
async function editar(id) {
  try {
    const p = await solicitar(`${API}/${id}`);
    $("formulario").reset(); $("id").value = p.id;
    for (const clave of ["nombres", "apellidos", "tipoDocumento", "numeroDocumento", "fechaNacimiento", "correo", "telefono", "nacionalidad"]) {
      $("formulario").elements.namedItem(clave).value = p[clave] || "";
    }
    $("modal-titulo").textContent = "Editar pasajero";
    $("guardar").textContent = "Guardar cambios"; abrir();
  } catch (e) { mostrarToast(e.message); }
}
async function guardar(evento) {
  evento.preventDefault();
  const form = $("formulario");
  if (!form.reportValidity()) return;
  const datos = Object.fromEntries(new FormData(form).entries());
  const id = $("id").value;
  const botonGuardar = $("guardar"); botonGuardar.disabled = true;
  try {
    await solicitar(id ? `${API}/${id}` : API, {
      method: id ? "PUT" : "POST",
      headers: {"Content-Type": "application/json"}, body: JSON.stringify(datos)
    });
    cerrar(); pagina = 1; await cargar(); mostrarToast(id ? "Pasajero actualizado." : "Pasajero registrado.");
  } catch (e) { $("form-error").textContent = e.message; $("form-error").hidden = false; }
  finally { botonGuardar.disabled = false; }
}
async function desactivar(id, nombre) {
  if (!confirm(`¿Desactivar a ${nombre}? El registro dejará de aparecer en el directorio.`)) return;
  try { await solicitar(`${API}/${id}`, {method: "DELETE"}); await cargar(); mostrarToast("Pasajero desactivado."); }
  catch (e) { mostrarToast(e.message); }
}
$("nuevo").addEventListener("click", nuevo);
$("cerrar").addEventListener("click", cerrar);
$("cancelar").addEventListener("click", cerrar);
$("modal").addEventListener("click", e => { if (e.target === $("modal")) cerrar(); });
document.addEventListener("keydown", e => { if (e.key === "Escape") cerrar(); });
$("formulario").addEventListener("submit", guardar);
$("busqueda").addEventListener("input", () => { clearTimeout(temporizadorBusqueda); temporizadorBusqueda = setTimeout(() => { pagina = 1; cargar(); }, 300); });
$("anterior").addEventListener("click", () => { if (pagina > 1) { pagina--; cargar(); } });
$("siguiente").addEventListener("click", () => { if (pagina * tamano < total) { pagina++; cargar(); } });
cargar();
