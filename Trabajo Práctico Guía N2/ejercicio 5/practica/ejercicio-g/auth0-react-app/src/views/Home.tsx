import { useAuth0 } from "@auth0/auth0-react";
import axios from "axios";

const Home = () => {
  const { user, isAuthenticated } = useAuth0();

  const callApi = async () => {
    try {
      const response = await axios.get(
        `${import.meta.env.VITE_API_SERVER_URL}/api/public`
      );

      console.log(response);

      const responseData = response.data;

      alert(responseData.message);
    } catch (error) {
      alert(error);
      console.error(error);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center mx-96">
      <h1 className="mb-4 text-2xl font-bold">Esta es la pagina de inicio.</h1>
      <div className="px-6 mb-4 text-justify">
        Este proyecto utiliza el sdk de Auth0 para administrar login, logout y
        registro de usuario, ademas de aplicar validaciones de permisos tanto
        para las rutas establecidad en con react-router-dom como en las
        peticiones que se realizaran a una API Rest hecha con Spring Boot.
      </div>
      {isAuthenticated ? (
        <div className="font-semibold text-green-500">
          Has iniciado sesion como {user?.name}{" "}
        </div>
      ) : (
        <div className="font-semibold text-red-500">
          Actualmente no has iniciado sesion
        </div>
      )}
      <div className="mt-4">
        <button
          onClick={callApi}
          className="px-4 py-2 font-bold text-white bg-blue-500 rounded hover:bg-blue-700"
        >
          Call Public API
        </button>
      </div>
    </div>
  );
};

export default Home;