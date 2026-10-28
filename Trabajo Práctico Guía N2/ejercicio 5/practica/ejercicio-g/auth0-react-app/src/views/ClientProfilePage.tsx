import { useAuth0 } from "@auth0/auth0-react";

const ClientProfilePage = () => {
  const { user, isAuthenticated, isLoading } = useAuth0();

  if (isLoading) {
    return (
      <div className="text-xl font-bold text-center">Cargando datos...</div>
    );
  }

  return isAuthenticated ? (
    <div className="flex-col text-center">
      <img className="inline mb-2" src={user?.picture} alt={user?.name} />
      <h2 className="font-bold">{user?.nickname}</h2>
      <p>{user?.email}</p>
    </div>
  ) : (
    <div className="text-xl font-bold text-center">
      Presiona Log In para ver información de tu perfil.
    </div>
  );
};

export default ClientProfilePage;