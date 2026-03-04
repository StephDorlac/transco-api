import { useState, useEffect, useCallback, useRef, useMemo } from "react";

const API_BASE = "/api/v1";

// ─── Theme ────────────────────────────────────────────────────────────────────
const themes = {
  light: {
    bg: "#F5F4F0",
    surface: "#FFFFFF",
    surfaceAlt: "#EEEDE8",
    border: "#D8D6CF",
    text: "#1A1916",
    textMuted: "#7A7870",
    accent: "#2D5BE3",
    accentLight: "#EEF2FD",
    danger: "#D63B3B",
    dangerLight: "#FDF0F0",
    success: "#1E8C5A",
    successLight: "#EDF7F2",
    warning: "#C47A1E",
    warningLight: "#FDF5E8",
    shadow: "0 1px 3px rgba(0,0,0,0.08), 0 4px 16px rgba(0,0,0,0.04)",
    shadowHover: "0 4px 12px rgba(0,0,0,0.12), 0 8px 24px rgba(0,0,0,0.06)",
  },
  dark: {
    bg: "#0F0F0E",
    surface: "#1A1917",
    surfaceAlt: "#242320",
    border: "#2E2D29",
    text: "#F0EFE8",
    textMuted: "#6B6A64",
    accent: "#4F7EFF",
    accentLight: "#1A2340",
    danger: "#E05555",
    dangerLight: "#2A1515",
    success: "#3AAD76",
    successLight: "#0F2A1E",
    warning: "#E09040",
    warningLight: "#2A1E0A",
    shadow: "0 1px 3px rgba(0,0,0,0.4), 0 4px 16px rgba(0,0,0,0.3)",
    shadowHover: "0 4px 12px rgba(0,0,0,0.5), 0 8px 24px rgba(0,0,0,0.4)",
  },
};

// ─── API ──────────────────────────────────────────────────────────────────────
const makeApi = (apiKey) => {
  const h = (extra = {}) => ({ "X-API-Key": apiKey, ...extra });
  return {
    getAll: () => fetch(`${API_BASE}/transco-rules`, { headers: h() })
      .then(r => { if (!r.ok) throw r; return r.json(); }),
    create: body => fetch(`${API_BASE}/transco-rules`, {
      method: "POST", headers: h({ "Content-Type": "application/json" }),
      body: JSON.stringify(body),
    }).then(r => { if (!r.ok) throw r; return r.json(); }),
    update: (id, body) => fetch(`${API_BASE}/transco-rules/${id}`, {
      method: "PUT", headers: h({ "Content-Type": "application/json" }),
      body: JSON.stringify(body),
    }).then(r => { if (!r.ok) throw r; return r.json(); }),
    delete: id => fetch(`${API_BASE}/transco-rules/${id}`, { method: "DELETE", headers: h() })
      .then(r => { if (!r.ok) throw r; }),
    resolve: body => fetch(`${API_BASE}/transco-rules/resolve`, {
      method: "POST", headers: h({ "Content-Type": "application/json" }),
      body: JSON.stringify(body),
    }).then(r => { if (!r.ok) throw r; return r.json(); }),
    importExcel: file => {
      const fd = new FormData();
      fd.append("file", file);
      return fetch(`${API_BASE}/transco-rules/import`, { method: "POST", headers: h(), body: fd })
        .then(r => { if (!r.ok) throw r; return r.json(); });
    },
  };
};

// ─── Icons ────────────────────────────────────────────────────────────────────
const Icon = ({ name, size = 16 }) => {
  const icons = {
    sun: <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41"/></svg>,
    moon: <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>,
    plus: <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>,
    edit: <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>,
    trash: <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/></svg>,
    search: <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>,
    x: <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>,
    zap: <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>,
    refresh: <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>,
    chevronDown: <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="6 9 12 15 18 9"/></svg>,
    database: <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/></svg>,
    check: <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg>,
    alertCircle: <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>,
    upload: <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="16 16 12 12 8 16"/><line x1="12" y1="12" x2="12" y2="21"/><path d="M20.39 18.39A5 5 0 0 0 18 9h-1.26A8 8 0 1 0 3 16.3"/></svg>,
    download: <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>,
    fileText: <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>,
    info: <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>,
    logOut: <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>,
    key: <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"/></svg>,
  };
  return icons[name] || null;
};

// ─── Toast ────────────────────────────────────────────────────────────────────
function Toast({ toasts, removeToast, t }) {
  return (
    <div style={{ position: "fixed", top: 20, right: 20, zIndex: 9999, display: "flex", flexDirection: "column", gap: 8 }}>
      {toasts.map(toast => (
        <div key={toast.id} style={{
          display: "flex", alignItems: "center", gap: 10, padding: "12px 16px",
          borderRadius: 10, minWidth: 280, maxWidth: 380,
          background: toast.type === "error" ? t.danger : toast.type === "success" ? t.success : t.accent,
          color: "#fff", boxShadow: "0 4px 20px rgba(0,0,0,0.25)",
          animation: "slideIn 0.2s ease", fontSize: 14, fontFamily: "inherit",
        }}>
          <Icon name={toast.type === "error" ? "alertCircle" : "check"} size={16} />
          <span style={{ flex: 1 }}>{toast.message}</span>
          <button onClick={() => removeToast(toast.id)} style={{ background: "none", border: "none", color: "rgba(255,255,255,0.7)", cursor: "pointer", padding: 2 }}>
            <Icon name="x" size={14} />
          </button>
        </div>
      ))}
    </div>
  );
}

// ─── Modal ────────────────────────────────────────────────────────────────────
function Modal({ open, onClose, title, children, t, maxWidth = 580 }) {
  if (!open) return null;
  return (
    <div onClick={onClose} style={{
      position: "fixed", inset: 0, zIndex: 1000,
      background: "rgba(0,0,0,0.5)", backdropFilter: "blur(4px)",
      display: "flex", alignItems: "center", justifyContent: "center", padding: 20,
    }}>
      <div onClick={e => e.stopPropagation()} style={{
        background: t.surface, borderRadius: 16, width: "100%", maxWidth,
        maxHeight: "90vh", overflow: "hidden", display: "flex", flexDirection: "column",
        boxShadow: "0 20px 60px rgba(0,0,0,0.3)", border: `1px solid ${t.border}`,
        animation: "modalIn 0.2s ease",
      }}>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "20px 24px", borderBottom: `1px solid ${t.border}` }}>
          <h2 style={{ margin: 0, fontSize: 18, fontWeight: 700, color: t.text, fontFamily: "'Syne', sans-serif" }}>{title}</h2>
          <button onClick={onClose} style={{ background: t.surfaceAlt, border: "none", borderRadius: 8, padding: 8, cursor: "pointer", color: t.textMuted, display: "flex" }}>
            <Icon name="x" size={16} />
          </button>
        </div>
        <div style={{ padding: 24, overflow: "auto", flex: 1 }}>{children}</div>
      </div>
    </div>
  );
}

// ─── JsonEditor ───────────────────────────────────────────────────────────────
function JsonEditor({ value, onChange, t, label }) {
  const [pairs, setPairs] = useState(() => {
    try { return Object.entries(typeof value === "string" ? JSON.parse(value || "{}") : (value || {})); }
    catch { return []; }
  });

  const sync = (newPairs) => {
    setPairs(newPairs);
    const obj = Object.fromEntries(newPairs.filter(([k]) => k.trim()));
    onChange(JSON.stringify(obj));
  };

  const add = () => sync([...pairs, ["", ""]]);
  const remove = i => sync(pairs.filter((_, idx) => idx !== i));
  const update = (i, ki, val) => sync(pairs.map((p, idx) => idx === i ? (ki === 0 ? [val, p[1]] : [p[0], val]) : p));

  const inputStyle = {
    background: t.bg, border: `1px solid ${t.border}`, borderRadius: 8,
    padding: "8px 10px", color: t.text, fontSize: 13,
    fontFamily: "'JetBrains Mono', monospace", outline: "none",
  };

  return (
    <div>
      <label style={{ fontSize: 13, fontWeight: 600, color: t.textMuted, display: "block", marginBottom: 8 }}>{label}</label>
      <div style={{ border: `1px solid ${t.border}`, borderRadius: 10, overflow: "hidden", background: t.surfaceAlt }}>
        {pairs.length === 0 && (
          <div style={{ padding: "12px 16px", color: t.textMuted, fontSize: 13, fontStyle: "italic" }}>Aucun critère — catch-all</div>
        )}
        {pairs.map(([k, v], i) => (
          <div key={i} style={{ display: "flex", gap: 8, alignItems: "center", padding: "8px 12px", borderBottom: i < pairs.length - 1 ? `1px solid ${t.border}` : "none" }}>
            <input value={k} onChange={e => update(i, 0, e.target.value)} placeholder="clé" style={{ ...inputStyle, flex: 1 }} />
            <span style={{ color: t.textMuted, fontSize: 12 }}>:</span>
            <input value={v} onChange={e => update(i, 1, e.target.value)} placeholder="valeur" style={{ ...inputStyle, flex: 2 }} />
            <button onClick={() => remove(i)} style={{ background: "none", border: "none", cursor: "pointer", color: t.danger, padding: 4, borderRadius: 4, display: "flex" }}>
              <Icon name="x" size={14} />
            </button>
          </div>
        ))}
        <div style={{ padding: "8px 12px", borderTop: pairs.length > 0 ? `1px solid ${t.border}` : "none" }}>
          <button onClick={add} style={{ background: "none", border: "none", cursor: "pointer", color: t.accent, fontSize: 13, display: "flex", alignItems: "center", gap: 6, padding: 0 }}>
            <Icon name="plus" size={14} /> Ajouter un critère
          </button>
        </div>
      </div>
    </div>
  );
}

// ─── RuleForm ─────────────────────────────────────────────────────────────────
function RuleForm({ initial, onSubmit, onCancel, loading, t }) {
  const [form, setForm] = useState({
    context: initial?.context || "",
    inputs: typeof initial?.inputs === "object" ? JSON.stringify(initial.inputs) : initial?.inputs || "{}",
    outputValue: initial?.outputValue || "",
    priority: initial?.priority ?? 0,
  });
  const field = (name, val) => setForm(f => ({ ...f, [name]: val }));

  const inputStyle = {
    width: "100%", background: t.surfaceAlt, border: `1px solid ${t.border}`,
    borderRadius: 10, padding: "10px 14px", color: t.text, fontSize: 14,
    fontFamily: "inherit", outline: "none", boxSizing: "border-box",
  };

  const handleSubmit = () => {
    try {
      const parsed = JSON.parse(form.inputs || "{}");
      onSubmit({ ...form, inputs: parsed, priority: Number(form.priority) });
    } catch { alert("JSON invalide dans les inputs"); }
  };

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 18 }}>
      <div>
        <label style={{ fontSize: 13, fontWeight: 600, color: t.textMuted, display: "block", marginBottom: 8 }}>Contexte *</label>
        <input value={form.context} onChange={e => field("context", e.target.value)} placeholder="ex: tarif, pays_vers_devise…" style={inputStyle} />
      </div>
      <JsonEditor value={form.inputs} onChange={v => field("inputs", v)} t={t} label="Critères (inputs)" />
      <div>
        <label style={{ fontSize: 13, fontWeight: 600, color: t.textMuted, display: "block", marginBottom: 8 }}>Valeur de sortie *</label>
        <input value={form.outputValue} onChange={e => field("outputValue", e.target.value)} placeholder="Valeur retournée…" style={inputStyle} />
      </div>
      <div>
        <label style={{ fontSize: 13, fontWeight: 600, color: t.textMuted, display: "block", marginBottom: 8 }}>Priorité</label>
        <input type="number" value={form.priority} onChange={e => field("priority", e.target.value)} style={{ ...inputStyle, width: 120 }} />
      </div>
      <div style={{ display: "flex", gap: 10, justifyContent: "flex-end", paddingTop: 8, borderTop: `1px solid ${t.border}` }}>
        <button onClick={onCancel} disabled={loading} style={{ padding: "10px 20px", borderRadius: 10, border: `1px solid ${t.border}`, background: "none", color: t.text, cursor: "pointer", fontSize: 14, fontFamily: "inherit" }}>Annuler</button>
        <button onClick={handleSubmit} disabled={loading} style={{ padding: "10px 20px", borderRadius: 10, border: "none", background: t.accent, color: "#fff", cursor: "pointer", fontSize: 14, fontFamily: "inherit", fontWeight: 600, display: "flex", alignItems: "center", gap: 8, opacity: loading ? 0.7 : 1 }}>
          {loading ? <><Icon name="refresh" size={14} /> Enregistrement…</> : <><Icon name="check" size={14} /> Enregistrer</>}
        </button>
      </div>
    </div>
  );
}

// ─── ImportModal ──────────────────────────────────────────────────────────────
function ImportModal({ open, onClose, onSuccess, t, api }) {
  const [file, setFile] = useState(null);
  const [dragging, setDragging] = useState(false);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const fileRef = useRef();

  const reset = () => { setFile(null); setResult(null); };
  const handleClose = () => { reset(); onClose(); };

  const handleFile = (f) => {
    if (!f) return;
    if (!f.name.toLowerCase().endsWith(".xlsx")) {
      alert("Seul le format .xlsx est accepté");
      return;
    }
    setFile(f);
    setResult(null);
  };

  const handleDrop = (e) => {
    e.preventDefault(); setDragging(false);
    handleFile(e.dataTransfer.files[0]);
  };

  const handleImport = async () => {
    if (!file) return;
    setLoading(true);
    try {
      const res = await api.importExcel(file);
      setResult(res);
      if (res.inserted > 0) onSuccess();
    } catch {
      setResult({ inserted: 0, skipped: 0, rejected: 0, errors: ["Erreur de communication avec l'API"] });
    } finally { setLoading(false); }
  };

  // Génération d'un fichier modèle
  const downloadTemplate = () => {
    const csv = "context,output_value,priority,pays,canal,client\ntarif,TARIF_A,30,FR,web,premium\ntarif,TARIF_B,20,FR,web,\ntarif,TARIF_C,10,FR,,\ntarif,TARIF_DEFAULT,0,,,";
    const blob = new Blob([csv], { type: "text/csv" });
    const a = document.createElement("a");
    a.href = URL.createObjectURL(blob);
    a.download = "transco-template.csv";
    a.click();
  };

  if (!open) return null;

  return (
    <Modal open={open} onClose={handleClose} title="Importer depuis Excel" t={t} maxWidth={620}>
      <div style={{ display: "flex", flexDirection: "column", gap: 20 }}>

        {/* Info format */}
        <div style={{ background: t.accentLight, border: `1px solid ${t.accent}30`, borderRadius: 10, padding: 14, display: "flex", gap: 12 }}>
          <div style={{ color: t.accent, flexShrink: 0, marginTop: 1 }}><Icon name="info" size={15} /></div>
          <div style={{ fontSize: 13, color: t.text, lineHeight: 1.6 }}>
            <strong>Format attendu :</strong> colonnes fixes <code style={{ background: t.surfaceAlt, padding: "1px 5px", borderRadius: 4, fontSize: 12 }}>context</code>, <code style={{ background: t.surfaceAlt, padding: "1px 5px", borderRadius: 4, fontSize: 12 }}>output_value</code>, <code style={{ background: t.surfaceAlt, padding: "1px 5px", borderRadius: 4, fontSize: 12 }}>priority</code> — toutes les autres colonnes sont des critères d'entrée. Une cellule vide = critère absent.
            <button onClick={downloadTemplate} style={{ background: "none", border: "none", color: t.accent, cursor: "pointer", fontSize: 13, padding: 0, marginLeft: 8, display: "inline-flex", alignItems: "center", gap: 4, textDecoration: "underline" }}>
              <Icon name="download" size={12} /> Télécharger le modèle CSV
            </button>
          </div>
        </div>

        {/* Drop zone */}
        <div
          onDragOver={e => { e.preventDefault(); setDragging(true); }}
          onDragLeave={() => setDragging(false)}
          onDrop={handleDrop}
          onClick={() => fileRef.current?.click()}
          style={{
            border: `2px dashed ${dragging ? t.accent : file ? t.success : t.border}`,
            borderRadius: 12, padding: "36px 24px", textAlign: "center", cursor: "pointer",
            background: dragging ? t.accentLight : file ? t.successLight : t.surfaceAlt,
            transition: "all 0.2s",
          }}
        >
          <input ref={fileRef} type="file" accept=".xlsx" style={{ display: "none" }} onChange={e => handleFile(e.target.files[0])} />
          {file ? (
            <>
              <div style={{ color: t.success, marginBottom: 8, display: "flex", justifyContent: "center" }}><Icon name="fileText" size={32} /></div>
              <div style={{ fontWeight: 600, color: t.text, fontSize: 15 }}>{file.name}</div>
              <div style={{ color: t.textMuted, fontSize: 13, marginTop: 4 }}>{(file.size / 1024).toFixed(1)} Ko</div>
              <button onClick={e => { e.stopPropagation(); reset(); }} style={{ marginTop: 10, background: "none", border: "none", color: t.danger, cursor: "pointer", fontSize: 13, display: "inline-flex", alignItems: "center", gap: 4 }}>
                <Icon name="x" size={12} /> Changer de fichier
              </button>
            </>
          ) : (
            <>
              <div style={{ color: t.textMuted, marginBottom: 10, display: "flex", justifyContent: "center" }}><Icon name="upload" size={32} /></div>
              <div style={{ fontWeight: 600, color: t.text, fontSize: 15 }}>Glisser-déposer un fichier .xlsx</div>
              <div style={{ color: t.textMuted, fontSize: 13, marginTop: 4 }}>ou cliquer pour parcourir</div>
            </>
          )}
        </div>

        {/* Résultat */}
        {result && (
          <div style={{ borderRadius: 12, border: `1px solid ${t.border}`, overflow: "hidden" }}>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", borderBottom: result.errors?.length ? `1px solid ${t.border}` : "none" }}>
              {[
                { label: "Insérées", value: result.inserted, color: t.success, bg: t.successLight },
                { label: "Ignorées (doublons)", value: result.skipped, color: t.warning, bg: t.warningLight },
                { label: "Rejetées", value: result.rejected, color: t.danger, bg: t.dangerLight },
              ].map(({ label, value, color, bg }) => (
                <div key={label} style={{ padding: "16px 12px", textAlign: "center", background: bg }}>
                  <div style={{ fontSize: 28, fontWeight: 800, color, fontFamily: "'Syne', sans-serif" }}>{value}</div>
                  <div style={{ fontSize: 11, color, fontWeight: 600, marginTop: 2, textTransform: "uppercase", letterSpacing: "0.05em" }}>{label}</div>
                </div>
              ))}
            </div>
            {result.errors?.length > 0 && (
              <div style={{ padding: 14, background: t.dangerLight, maxHeight: 160, overflow: "auto" }}>
                <div style={{ fontSize: 12, fontWeight: 700, color: t.danger, marginBottom: 8, display: "flex", alignItems: "center", gap: 6 }}>
                  <Icon name="alertCircle" size={12} /> DÉTAIL DES ERREURS
                </div>
                {result.errors.map((e, i) => (
                  <div key={i} style={{ fontSize: 12, color: t.text, padding: "3px 0", borderBottom: i < result.errors.length - 1 ? `1px solid ${t.border}` : "none" }}>
                    {e}
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Actions */}
        <div style={{ display: "flex", gap: 10, justifyContent: "flex-end", paddingTop: 4 }}>
          <button onClick={handleClose} style={{ padding: "10px 20px", borderRadius: 10, border: `1px solid ${t.border}`, background: "none", color: t.text, cursor: "pointer", fontSize: 14, fontFamily: "inherit" }}>
            {result ? "Fermer" : "Annuler"}
          </button>
          {!result && (
            <button onClick={handleImport} disabled={!file || loading} style={{
              padding: "10px 20px", borderRadius: 10, border: "none",
              background: t.accent, color: "#fff", cursor: "pointer", fontSize: 14,
              fontFamily: "inherit", fontWeight: 600, display: "flex", alignItems: "center", gap: 8,
              opacity: (!file || loading) ? 0.6 : 1,
            }}>
              {loading ? <><Icon name="refresh" size={14} /> Import en cours…</> : <><Icon name="upload" size={14} /> Lancer l'import</>}
            </button>
          )}
        </div>
      </div>
    </Modal>
  );
}

// ─── ResolvePanel ─────────────────────────────────────────────────────────────
function ResolvePanel({ t, api }) {
  const [context, setContext] = useState("");
  const [inputs, setInputs] = useState("{}");
  const [withFallback, setWithFallback] = useState(true);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const resolve = async () => {
    setLoading(true); setError(""); setResult(null);
    try {
      const parsed = JSON.parse(inputs || "{}");
      const res = await api.resolve({ context, inputs: parsed, withFallback });
      setResult(res);
    } catch {
      setError("Aucune règle trouvée pour ces critères");
    } finally { setLoading(false); }
  };

  const inputStyle = {
    width: "100%", background: t.surfaceAlt, border: `1px solid ${t.border}`,
    borderRadius: 10, padding: "10px 14px", color: t.text, fontSize: 14,
    fontFamily: "inherit", outline: "none", boxSizing: "border-box",
  };

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
        <div>
          <label style={{ fontSize: 13, fontWeight: 600, color: t.textMuted, display: "block", marginBottom: 8 }}>Contexte</label>
          <input value={context} onChange={e => setContext(e.target.value)} placeholder="ex: tarif" style={inputStyle} />
        </div>
        <div style={{ display: "flex", alignItems: "flex-end", paddingBottom: 2 }}>
          <label style={{ display: "flex", alignItems: "center", gap: 10, cursor: "pointer", userSelect: "none" }}>
            <div onClick={() => setWithFallback(v => !v)} style={{ width: 44, height: 24, borderRadius: 12, background: withFallback ? t.accent : t.border, position: "relative", transition: "background 0.2s", cursor: "pointer" }}>
              <div style={{ position: "absolute", top: 3, left: withFallback ? 23 : 3, width: 18, height: 18, borderRadius: "50%", background: "#fff", transition: "left 0.2s", boxShadow: "0 1px 3px rgba(0,0,0,0.2)" }} />
            </div>
            <span style={{ fontSize: 13, color: t.text }}>Avec fallback</span>
          </label>
        </div>
      </div>
      <JsonEditor value={inputs} onChange={setInputs} t={t} label="Inputs de résolution" />
      <button onClick={resolve} disabled={loading || !context} style={{ padding: "11px 20px", borderRadius: 10, border: "none", background: t.accent, color: "#fff", cursor: "pointer", fontSize: 14, fontFamily: "inherit", fontWeight: 600, display: "flex", alignItems: "center", justifyContent: "center", gap: 8, opacity: (loading || !context) ? 0.6 : 1 }}>
        <Icon name="zap" size={16} /> {loading ? "Résolution…" : "Résoudre"}
      </button>
      {result && (
        <div style={{ background: t.successLight, border: `1px solid ${t.success}`, borderRadius: 10, padding: 16 }}>
          <div style={{ fontSize: 12, color: t.success, fontWeight: 600, marginBottom: 4, display: "flex", alignItems: "center", gap: 6 }}><Icon name="check" size={12} /> RÉSULTAT</div>
          <div style={{ fontSize: 20, fontWeight: 700, color: t.text, fontFamily: "'Syne', sans-serif" }}>{result.outputValue}</div>
          {result.ruleId && <div style={{ fontSize: 12, color: t.textMuted, marginTop: 4 }}>Règle #{result.ruleId} — priorité {result.priority}</div>}
        </div>
      )}
      {error && (
        <div style={{ background: t.dangerLight, border: `1px solid ${t.danger}`, borderRadius: 10, padding: 16, display: "flex", gap: 10, alignItems: "center" }}>
          <Icon name="alertCircle" size={16} />
          <span style={{ color: t.danger, fontSize: 14 }}>{error}</span>
        </div>
      )}
    </div>
  );
}

// ─── LoginScreen ──────────────────────────────────────────────────────────────
function LoginScreen({ onLogin, t }) {
  const [key, setKey] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!key.trim()) return;
    setLoading(true);
    setError("");
    try {
      const r = await fetch(`${API_BASE}/transco-rules`, {
        headers: { "X-API-Key": key.trim() },
      });
      if (r.status === 401 || r.status === 403) {
        setError("Clé API invalide ou inactive");
      } else if (r.ok) {
        sessionStorage.setItem("transco-api-key", key.trim());
        onLogin(key.trim());
      } else {
        setError("Erreur de connexion à l'API");
      }
    } catch {
      setError("Impossible de joindre l'API");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: "100vh", background: t.bg, display: "flex", alignItems: "center", justifyContent: "center", padding: 20 }}>
      <div style={{ background: t.surface, borderRadius: 20, padding: 40, width: "100%", maxWidth: 420, boxShadow: t.shadowHover, border: `1px solid ${t.border}`, animation: "modalIn 0.25s ease" }}>
        <div style={{ textAlign: "center", marginBottom: 32 }}>
          <div style={{ background: t.accent, borderRadius: 14, padding: 14, display: "inline-flex", color: "#fff", marginBottom: 16 }}>
            <Icon name="database" size={24} />
          </div>
          <div style={{ fontFamily: "'Syne', sans-serif", fontWeight: 800, fontSize: 22, color: t.text }}>Transco Admin</div>
          <div style={{ color: t.textMuted, fontSize: 14, marginTop: 6 }}>Entrez votre clé API pour continuer</div>
        </div>
        <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: 16 }}>
          <div>
            <label style={{ fontSize: 13, fontWeight: 600, color: t.textMuted, display: "block", marginBottom: 8 }}>
              <span style={{ display: "flex", alignItems: "center", gap: 6 }}><Icon name="key" size={13} /> Clé API</span>
            </label>
            <input
              type="password"
              value={key}
              onChange={e => { setKey(e.target.value); setError(""); }}
              placeholder="••••••••••••••••"
              autoFocus
              style={{
                width: "100%", background: t.surfaceAlt,
                border: `1px solid ${error ? t.danger : t.border}`,
                borderRadius: 10, padding: "12px 14px", color: t.text, fontSize: 14,
                fontFamily: "inherit", outline: "none", boxSizing: "border-box",
                transition: "border-color 0.15s",
              }}
            />
            {error && (
              <div style={{ marginTop: 8, fontSize: 13, color: t.danger, display: "flex", alignItems: "center", gap: 6 }}>
                <Icon name="alertCircle" size={13} /> {error}
              </div>
            )}
          </div>
          <button
            type="submit"
            disabled={loading || !key.trim()}
            style={{
              padding: "12px 20px", borderRadius: 10, border: "none",
              background: t.accent, color: "#fff", cursor: "pointer", fontSize: 14,
              fontFamily: "inherit", fontWeight: 600, display: "flex", alignItems: "center",
              justifyContent: "center", gap: 8,
              opacity: (loading || !key.trim()) ? 0.6 : 1,
              transition: "opacity 0.15s",
            }}
          >
            {loading ? <><Icon name="refresh" size={14} /> Vérification…</> : <><Icon name="check" size={14} /> Se connecter</>}
          </button>
        </form>
      </div>
    </div>
  );
}

// ─── App ──────────────────────────────────────────────────────────────────────
export default function App() {
  const [mode, setMode] = useState("light");
  const t = themes[mode];

  const [apiKey, setApiKey] = useState(() => sessionStorage.getItem("transco-api-key") || "");
  const api = useMemo(() => makeApi(apiKey), [apiKey]);

  const logout = useCallback(() => {
    sessionStorage.removeItem("transco-api-key");
    setApiKey("");
  }, []);

  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [contextFilter, setContextFilter] = useState("all");
  const [toasts, setToasts] = useState([]);
  const [modal, setModal] = useState(null);
  const [saving, setSaving] = useState(false);
  const [activeTab, setActiveTab] = useState("rules");
  const [showImport, setShowImport] = useState(false);

  const addToast = useCallback((message, type = "success") => {
    const id = Date.now();
    setToasts(t => [...t, { id, message, type }]);
    setTimeout(() => setToasts(t => t.filter(x => x.id !== id)), 3500);
  }, []);

  const removeToast = id => setToasts(t => t.filter(x => x.id !== id));

  const fetchRules = useCallback(async () => {
    setLoading(true);
    try {
      const data = await api.getAll();
      setRules(Array.isArray(data) ? data : []);
    } catch (err) {
      if (err?.status === 401 || err?.status === 403) {
        logout();
      } else {
        addToast("Impossible de charger les règles", "error");
        setRules([]);
      }
    } finally { setLoading(false); }
  }, [api, addToast, logout]);

  useEffect(() => { if (apiKey) fetchRules(); }, [apiKey, fetchRules]);

  // ─── Écran de connexion ───
  if (!apiKey) {
    return (
      <>
        <style>{`
          @import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&family=DM+Sans:wght@300;400;500;600&family=JetBrains+Mono:wght@400;500&display=swap');
          * { box-sizing: border-box; margin: 0; padding: 0; }
          body { font-family: 'DM Sans', sans-serif; }
          input:focus { border-color: ${t.accent} !important; }
          @keyframes modalIn { from { transform: translateY(-10px) scale(0.97); opacity: 0; } to { transform: translateY(0) scale(1); opacity: 1; } }
        `}</style>
        <LoginScreen onLogin={setApiKey} t={t} />
      </>
    );
  }

  const contexts = ["all", ...new Set(rules.map(r => r.context))];
  const filtered = rules.filter(r =>
    (contextFilter === "all" || r.context === contextFilter) &&
    (search === "" ||
      r.context.toLowerCase().includes(search.toLowerCase()) ||
      r.outputValue.toLowerCase().includes(search.toLowerCase()) ||
      JSON.stringify(r.inputs).toLowerCase().includes(search.toLowerCase()))
  );

  const handleCreate = async (data) => {
    setSaving(true);
    try { await api.create(data); addToast("Règle créée avec succès"); setModal(null); fetchRules(); }
    catch { addToast("Erreur lors de la création", "error"); }
    finally { setSaving(false); }
  };

  const handleUpdate = async (data) => {
    setSaving(true);
    try { await api.update(modal.data.id, data); addToast("Règle mise à jour"); setModal(null); fetchRules(); }
    catch { addToast("Erreur lors de la mise à jour", "error"); }
    finally { setSaving(false); }
  };

  const handleDelete = async () => {
    setSaving(true);
    try { await api.delete(modal.data.id); addToast("Règle supprimée"); setModal(null); fetchRules(); }
    catch { addToast("Erreur lors de la suppression", "error"); }
    finally { setSaving(false); }
  };

  const inputsDisplay = (inputs) => {
    if (!inputs || Object.keys(inputs).length === 0)
      return <span style={{ color: t.textMuted, fontStyle: "italic", fontSize: 12 }}>catch-all</span>;
    return (
      <div style={{ display: "flex", flexWrap: "wrap", gap: 4 }}>
        {Object.entries(inputs).map(([k, v]) => (
          <span key={k} style={{ background: t.accentLight, color: t.accent, borderRadius: 6, padding: "2px 8px", fontSize: 12, fontFamily: "'JetBrains Mono', monospace", fontWeight: 500 }}>
            {k}: {v}
          </span>
        ))}
      </div>
    );
  };

  const priorityBadge = (p) => {
    const color = p >= 20 ? t.danger : p >= 10 ? t.warning : t.success;
    const bg = p >= 20 ? t.dangerLight : p >= 10 ? t.warningLight : t.successLight;
    return <span style={{ background: bg, color, borderRadius: 6, padding: "2px 8px", fontSize: 12, fontWeight: 700 }}>{p}</span>;
  };

  return (
    <>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&family=DM+Sans:wght@300;400;500;600&family=JetBrains+Mono:wght@400;500&display=swap');
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: 'DM Sans', sans-serif; }
        input:focus, textarea:focus { border-color: ${t.accent} !important; }
        @keyframes slideIn { from { transform: translateX(20px); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
        @keyframes modalIn { from { transform: translateY(-10px) scale(0.97); opacity: 0; } to { transform: translateY(0) scale(1); opacity: 1; } }
        @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
        ::-webkit-scrollbar { width: 6px; height: 6px; }
        ::-webkit-scrollbar-track { background: transparent; }
        ::-webkit-scrollbar-thumb { background: ${t.border}; border-radius: 3px; }
        tr:hover td { background: ${t.surfaceAlt} !important; }
      `}</style>

      <div style={{ minHeight: "100vh", background: t.bg, color: t.text, transition: "background 0.3s, color 0.3s" }}>

        {/* Header */}
        <header style={{ background: t.surface, borderBottom: `1px solid ${t.border}`, padding: "0 32px", position: "sticky", top: 0, zIndex: 100, boxShadow: t.shadow }}>
          <div style={{ maxWidth: 1280, margin: "0 auto", display: "flex", alignItems: "center", height: 64, gap: 24 }}>
            <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
              <div style={{ background: t.accent, borderRadius: 10, padding: 8, display: "flex", color: "#fff" }}>
                <Icon name="database" size={18} />
              </div>
              <div>
                <div style={{ fontFamily: "'Syne', sans-serif", fontWeight: 800, fontSize: 17, color: t.text, lineHeight: 1 }}>Transco</div>
                <div style={{ fontSize: 11, color: t.textMuted, fontWeight: 500 }}>Administration</div>
              </div>
            </div>

            <nav style={{ display: "flex", gap: 2, marginLeft: 16, background: t.surfaceAlt, borderRadius: 10, padding: 4 }}>
              {[["rules", "database", "Règles"], ["resolve", "zap", "Résolution"]].map(([tab, icon, label]) => (
                <button key={tab} onClick={() => setActiveTab(tab)} style={{
                  padding: "6px 16px", borderRadius: 7, border: "none", cursor: "pointer",
                  fontSize: 13, fontWeight: 600, fontFamily: "inherit", display: "flex", alignItems: "center", gap: 6,
                  background: activeTab === tab ? t.surface : "none",
                  color: activeTab === tab ? t.text : t.textMuted,
                  boxShadow: activeTab === tab ? t.shadow : "none",
                  transition: "all 0.15s",
                }}>
                  <Icon name={icon} size={13} /> {label}
                </button>
              ))}
            </nav>

            <div style={{ flex: 1 }} />

            <button onClick={fetchRules} title="Rafraîchir" style={{ background: t.surfaceAlt, border: `1px solid ${t.border}`, borderRadius: 10, padding: 9, cursor: "pointer", color: t.textMuted, display: "flex", alignItems: "center" }}>
              <Icon name="refresh" size={16} />
            </button>
            <button onClick={() => setMode(m => m === "light" ? "dark" : "light")} style={{ background: t.surfaceAlt, border: `1px solid ${t.border}`, borderRadius: 10, padding: 9, cursor: "pointer", color: t.textMuted, display: "flex", alignItems: "center" }}>
              <Icon name={mode === "light" ? "moon" : "sun"} size={16} />
            </button>
            <button onClick={logout} title="Se déconnecter" style={{ background: t.dangerLight, border: `1px solid ${t.danger}30`, borderRadius: 10, padding: 9, cursor: "pointer", color: t.danger, display: "flex", alignItems: "center" }}>
              <Icon name="logOut" size={16} />
            </button>
          </div>
        </header>

        <main style={{ maxWidth: 1280, margin: "0 auto", padding: "32px" }}>
          {activeTab === "rules" ? (
            <>
              {/* Toolbar */}
              <div style={{ display: "flex", gap: 12, alignItems: "center", marginBottom: 24, flexWrap: "wrap" }}>
                <div style={{ position: "relative", flex: 1, minWidth: 200 }}>
                  <span style={{ position: "absolute", left: 12, top: "50%", transform: "translateY(-50%)", color: t.textMuted }}>
                    <Icon name="search" size={15} />
                  </span>
                  <input value={search} onChange={e => setSearch(e.target.value)}
                    placeholder="Rechercher contexte, valeur, critères…"
                    style={{ width: "100%", background: t.surface, border: `1px solid ${t.border}`, borderRadius: 10, padding: "10px 14px 10px 38px", color: t.text, fontSize: 14, fontFamily: "inherit", outline: "none" }}
                  />
                </div>

                <div style={{ position: "relative" }}>
                  <select value={contextFilter} onChange={e => setContextFilter(e.target.value)} style={{ appearance: "none", background: t.surface, border: `1px solid ${t.border}`, borderRadius: 10, padding: "10px 36px 10px 14px", color: t.text, fontSize: 14, fontFamily: "inherit", outline: "none", cursor: "pointer", minWidth: 160 }}>
                    {contexts.map(c => <option key={c} value={c}>{c === "all" ? "Tous les contextes" : c}</option>)}
                  </select>
                  <span style={{ position: "absolute", right: 10, top: "50%", transform: "translateY(-50%)", color: t.textMuted, pointerEvents: "none" }}><Icon name="chevronDown" size={14} /></span>
                </div>

                <div style={{ height: 32, width: 1, background: t.border }} />
                <div style={{ fontSize: 13, color: t.textMuted, whiteSpace: "nowrap" }}>
                  <b style={{ color: t.text }}>{filtered.length}</b> règle{filtered.length !== 1 ? "s" : ""}
                </div>

                {/* Bouton Import */}
                <button onClick={() => setShowImport(true)} style={{
                  background: t.surfaceAlt, color: t.text, border: `1px solid ${t.border}`, borderRadius: 10,
                  padding: "10px 18px", cursor: "pointer", fontSize: 14, fontFamily: "inherit",
                  fontWeight: 600, display: "flex", alignItems: "center", gap: 8,
                }}>
                  <Icon name="upload" size={15} /> Importer Excel
                </button>

                <button onClick={() => setModal({ type: "create" })} style={{
                  background: t.accent, color: "#fff", border: "none", borderRadius: 10,
                  padding: "10px 20px", cursor: "pointer", fontSize: 14, fontFamily: "inherit",
                  fontWeight: 600, display: "flex", alignItems: "center", gap: 8,
                  boxShadow: `0 2px 8px ${t.accent}40`,
                }}>
                  <Icon name="plus" size={15} /> Nouvelle règle
                </button>
              </div>

              {/* Table */}
              <div style={{ background: t.surface, borderRadius: 16, border: `1px solid ${t.border}`, overflow: "hidden", boxShadow: t.shadow }}>
                {loading ? (
                  <div style={{ padding: 60, textAlign: "center", color: t.textMuted }}>
                    <div style={{ fontSize: 28, marginBottom: 12 }}>⟳</div>Chargement des règles…
                  </div>
                ) : filtered.length === 0 ? (
                  <div style={{ padding: 60, textAlign: "center", color: t.textMuted }}>
                    <div style={{ fontSize: 40, marginBottom: 12 }}>∅</div>
                    Aucune règle{search ? ` pour "${search}"` : ""}
                  </div>
                ) : (
                  <div style={{ overflow: "auto" }}>
                    <table style={{ width: "100%", borderCollapse: "collapse" }}>
                      <thead>
                        <tr style={{ background: t.surfaceAlt }}>
                          {["ID", "Contexte", "Critères (inputs)", "Sortie", "Priorité", "Modifié le", "Actions"].map(h => (
                            <th key={h} style={{ padding: "12px 16px", textAlign: "left", fontSize: 12, fontWeight: 700, color: t.textMuted, whiteSpace: "nowrap", letterSpacing: "0.05em", borderBottom: `1px solid ${t.border}`, textTransform: "uppercase" }}>{h}</th>
                          ))}
                        </tr>
                      </thead>
                      <tbody>
                        {filtered.map((rule, i) => (
                          <tr key={rule.id} style={{ animation: `fadeIn 0.2s ease ${i * 0.02}s both` }}>
                            <td style={{ padding: "14px 16px", borderBottom: `1px solid ${t.border}`, fontSize: 12, color: t.textMuted, fontFamily: "'JetBrains Mono', monospace" }}>#{rule.id}</td>
                            <td style={{ padding: "14px 16px", borderBottom: `1px solid ${t.border}` }}>
                              <span style={{ fontWeight: 600, color: t.text, fontSize: 14 }}>{rule.context}</span>
                            </td>
                            <td style={{ padding: "14px 16px", borderBottom: `1px solid ${t.border}`, maxWidth: 280 }}>{inputsDisplay(rule.inputs)}</td>
                            <td style={{ padding: "14px 16px", borderBottom: `1px solid ${t.border}`, fontFamily: "'JetBrains Mono', monospace", fontSize: 13, color: t.text, fontWeight: 500 }}>{rule.outputValue}</td>
                            <td style={{ padding: "14px 16px", borderBottom: `1px solid ${t.border}` }}>{priorityBadge(rule.priority)}</td>
                            <td style={{ padding: "14px 16px", borderBottom: `1px solid ${t.border}`, fontSize: 12, color: t.textMuted, whiteSpace: "nowrap" }}>
                              {rule.updatedAt ? new Date(rule.updatedAt).toLocaleDateString("fr-FR", { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" }) : "—"}
                            </td>
                            <td style={{ padding: "14px 16px", borderBottom: `1px solid ${t.border}` }}>
                              <div style={{ display: "flex", gap: 6 }}>
                                <button onClick={() => setModal({ type: "edit", data: rule })} style={{ background: t.accentLight, border: "none", borderRadius: 8, padding: 8, cursor: "pointer", color: t.accent, display: "flex" }}><Icon name="edit" size={14} /></button>
                                <button onClick={() => setModal({ type: "delete", data: rule })} style={{ background: t.dangerLight, border: "none", borderRadius: 8, padding: 8, cursor: "pointer", color: t.danger, display: "flex" }}><Icon name="trash" size={14} /></button>
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </>
          ) : (
            <div style={{ maxWidth: 640, margin: "0 auto" }}>
              <div style={{ marginBottom: 24 }}>
                <h1 style={{ fontFamily: "'Syne', sans-serif", fontSize: 26, fontWeight: 800, color: t.text }}>Résolution de règle</h1>
                <p style={{ color: t.textMuted, marginTop: 6, fontSize: 14 }}>Testez la résolution en fournissant un contexte et des critères d'entrée.</p>
              </div>
              <div style={{ background: t.surface, borderRadius: 16, border: `1px solid ${t.border}`, padding: 28, boxShadow: t.shadow }}>
                <ResolvePanel t={t} api={api} />
              </div>
            </div>
          )}
        </main>
      </div>

      {/* Modals CRUD */}
      <Modal open={modal?.type === "create"} onClose={() => setModal(null)} title="Nouvelle règle" t={t}>
        <RuleForm onSubmit={handleCreate} onCancel={() => setModal(null)} loading={saving} t={t} />
      </Modal>
      <Modal open={modal?.type === "edit"} onClose={() => setModal(null)} title={`Modifier la règle #${modal?.data?.id}`} t={t}>
        {modal?.data && <RuleForm initial={modal.data} onSubmit={handleUpdate} onCancel={() => setModal(null)} loading={saving} t={t} />}
      </Modal>
      <Modal open={modal?.type === "delete"} onClose={() => setModal(null)} title="Supprimer la règle" t={t}>
        <div style={{ display: "flex", flexDirection: "column", gap: 20 }}>
          <div style={{ background: t.dangerLight, border: `1px solid ${t.danger}20`, borderRadius: 10, padding: 16 }}>
            <p style={{ color: t.text, fontSize: 14, marginBottom: 8 }}>Vous êtes sur le point de supprimer la règle <strong>#{modal?.data?.id}</strong> du contexte <strong>{modal?.data?.context}</strong>.</p>
            <p style={{ color: t.textMuted, fontSize: 13 }}>Cette action est irréversible.</p>
          </div>
          <div style={{ display: "flex", gap: 10, justifyContent: "flex-end" }}>
            <button onClick={() => setModal(null)} style={{ padding: "10px 20px", borderRadius: 10, border: `1px solid ${t.border}`, background: "none", color: t.text, cursor: "pointer", fontSize: 14, fontFamily: "inherit" }}>Annuler</button>
            <button onClick={handleDelete} disabled={saving} style={{ padding: "10px 20px", borderRadius: 10, border: "none", background: t.danger, color: "#fff", cursor: "pointer", fontSize: 14, fontFamily: "inherit", fontWeight: 600, display: "flex", alignItems: "center", gap: 8, opacity: saving ? 0.7 : 1 }}>
              {saving ? "Suppression…" : <><Icon name="trash" size={14} /> Supprimer</>}
            </button>
          </div>
        </div>
      </Modal>

      {/* Modal Import */}
      <ImportModal
        open={showImport}
        onClose={() => setShowImport(false)}
        onSuccess={() => { fetchRules(); addToast("Import terminé, liste mise à jour"); }}
        t={t}
        api={api}
      />

      <Toast toasts={toasts} removeToast={removeToast} t={t} />
    </>
  );
}
