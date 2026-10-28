import { withAuthenticationRequired } from "@auth0/auth0-react";

type Props = {
  component: React.ComponentType<object>;
};

export const AuthenticationGuard = ({ component }: Props) => {
  const Component = withAuthenticationRequired(component, {
    onRedirecting: () => (
      <div className="flex flex-col items-center justify-center mx-96">
        <div className="mb-4 text-2xl font-bold">Redireccionando...</div>
      </div>
    ),
  });

  return <Component />;
};