import { useAuth0 } from "@auth0/auth0-react";

const LoginButton = () => {
  const { loginWithRedirect } = useAuth0();

  return (
    <button
      className="inline-block px-4 py-3 text-sm font-semibold text-center text-gray-500 transition duration-100 rounded-lg outline-none ring-indigo-300 hover:text-indigo-500 focus-visible:ring active:text-indigo-600 md:text-base"
      onClick={() =>
        loginWithRedirect({
          appState: {
            returnTo: window.location.pathname,
          },
        })
      }
    >
      Log In
    </button>
  );
};

export default LoginButton;