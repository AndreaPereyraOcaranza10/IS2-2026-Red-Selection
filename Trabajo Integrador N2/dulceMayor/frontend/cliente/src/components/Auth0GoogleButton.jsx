import { useAuth0 } from "@auth0/auth0-react";
import { useEffect, useRef, useState } from "react";

export function Auth0GoogleButton({ configured, mode = "login", onCredential, onError }) {
  if (!configured) {
    return <div className="google-auth-unavailable" role="status">
      Configura VITE_AUTH0_DOMAIN y VITE_AUTH0_CLIENT_ID para habilitar Google.
    </div>;
  }
  return <ConfiguredAuth0GoogleButton mode={mode} onCredential={onCredential} onError={onError} />;
}

function ConfiguredAuth0GoogleButton({ mode, onCredential, onError }) {
  const { isAuthenticated, loginWithRedirect, getIdTokenClaims, user, error } = useAuth0();
  const [processing, setProcessing] = useState(false);
  const credentialHandler = useRef(onCredential);
  const errorHandler = useRef(onError);
  const exchangedSubject = useRef("");

  useEffect(() => { credentialHandler.current = onCredential; }, [onCredential]);
  useEffect(() => { errorHandler.current = onError; }, [onError]);

  useEffect(() => {
    if (error) errorHandler.current?.(error.message || "No se pudo completar el acceso con Auth0.");
  }, [error]);

  useEffect(() => {
    if (!isAuthenticated || !user?.sub || exchangedSubject.current === user.sub) return undefined;
    exchangedSubject.current = user.sub;
    setProcessing(true);
    (async () => {
      try {
        const claims = await getIdTokenClaims();
        if (!claims?.__raw) throw new Error("Auth0 no devolvio el token de identidad.");
        await credentialHandler.current?.(claims.__raw);
      } catch (authError) {
        errorHandler.current?.(authError instanceof Error ? authError.message : "No se pudo validar la cuenta.");
      } finally {
        setProcessing(false);
      }
    })();
    return undefined;
  }, [getIdTokenClaims, isAuthenticated, user?.sub]);

  const signIn = async () => {
    try {
      await loginWithRedirect({
        appState: { returnTo: window.location.pathname },
        authorizationParams: { connection: "google-oauth2" }
      });
    } catch (authError) {
      errorHandler.current?.(authError instanceof Error ? authError.message : "No se pudo iniciar el acceso con Google.");
    }
  };

  return <button type="button" className="auth0-google-button" onClick={signIn} disabled={processing} aria-busy={processing}>
    <span className="auth0-google-mark" aria-hidden="true">G</span>
    {processing ? "Validando cuenta..." : mode === "register" ? "Registrarse con Google" : "Continuar con Google"}
  </button>;
}
