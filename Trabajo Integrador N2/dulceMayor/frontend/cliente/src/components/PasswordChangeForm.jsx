import { useState } from "react";
import { KeyRound, LoaderCircle, ShieldCheck } from "lucide-react";
import { api } from "../api.js";

export function PasswordChangeForm({ firstLogin = false, onSuccess }) {
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmation, setConfirmation] = useState("");
  const [notice, setNotice] = useState(null);
  const [saving, setSaving] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
    setNotice(null);
    if (newPassword !== confirmation) {
      setNotice({ type: "error", text: "Las claves nuevas no coinciden." });
      return;
    }
    if (newPassword.length < 8 || !/[A-Z]/.test(newPassword) || !/\d/.test(newPassword) || !/[^A-Za-z0-9]/.test(newPassword) || newPassword.toLowerCase() === "dulcemayor") {
      setNotice({ type: "error", text: "Usa al menos 8 caracteres, una mayuscula, un numero y un simbolo. No uses la clave inicial." });
      return;
    }
    setSaving(true);
    try {
      const updated = firstLogin
        ? await api.changeInitialPassword(newPassword)
        : await api.changePassword(currentPassword, newPassword);
      setNotice({ type: "success", text: "Clave actualizada." });
      onSuccess?.(updated);
    } catch (error) {
      setNotice({ type: "error", text: error instanceof Error ? error.message : "No se pudo cambiar la clave." });
    } finally {
      setSaving(false);
    }
  };

  return (
    <form className="profile-section password-change-section" onSubmit={submit}>
      <header><span><KeyRound /></span><div>
        <h2>{firstLogin ? "Elige una clave nueva" : "Seguridad de la cuenta"}</h2>
        <p>{firstLogin ? "Cambia la clave inicial para continuar." : "Actualiza la clave que usas para ingresar."}</p>
      </div></header>
      <div className="profile-fields">
        {!firstLogin && <label>Clave actual<input type="password" autoComplete="current-password" value={currentPassword} onChange={(event) => setCurrentPassword(event.target.value)} required /></label>}
        <label>Clave nueva<input type="password" autoComplete="new-password" value={newPassword} onChange={(event) => setNewPassword(event.target.value)} required /></label>
        <label>Confirma la clave nueva<input type="password" autoComplete="new-password" value={confirmation} onChange={(event) => setConfirmation(event.target.value)} required /></label>
      </div>
      {notice && <div className={`profile-notice ${notice.type}`} role={notice.type === "error" ? "alert" : "status"}>{notice.text}</div>}
      <footer className="profile-form-footer">
        <span><ShieldCheck size={15} /> Las claves se almacenan de forma segura.</span>
        <button className="profile-save" type="submit" disabled={saving || (!firstLogin && !currentPassword)}>
          {saving ? <LoaderCircle className="spin" size={16} /> : <KeyRound size={16} />}
          {saving ? "Guardando..." : "Cambiar clave"}
        </button>
      </footer>
    </form>
  );
}
