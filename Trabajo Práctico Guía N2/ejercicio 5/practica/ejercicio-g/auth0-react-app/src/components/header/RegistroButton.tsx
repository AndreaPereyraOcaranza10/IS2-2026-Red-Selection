import { useAuth0 } from "@auth0/auth0-react";

const RegistroButton = () => {
  const { loginWithRedirect } = useAuth0();

  return (
    <button
      className="inline-block px-8 py-3 text-sm font-semibold text-center text-white transition duration-100 bg-indigo-500 rounded-lg outline-none ring-indigo-300 hover:bg-indigo-600 focus-visible:ring active:bg-indigo-700 md:text-base"
      onClick={() =>
        loginWithRedirect({
          appState: {
            returnTo: "/cliente/perfil",
          },
          authorizationParams: {
            screen_hint: "signup",
          },
        })
      }
    >
      Sign up
    </button>
  );
};

export default RegistroButton;