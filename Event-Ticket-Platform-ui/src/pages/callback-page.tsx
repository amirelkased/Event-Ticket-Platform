import {useEffect} from "react";
import {useAuth} from "react-oidc-context";
import {useNavigate} from "react-router";

const CallbackPage: React.FC = () => {
    const auth = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        // Wait for auth to finish loading
        if (auth.isLoading) {
            return;
        }

        // If authenticated, navigate to the appropriate page
        if (auth.isAuthenticated) {
            const redirectPath = localStorage.getItem("redirectPath");
            if (redirectPath) {
                localStorage.removeItem("redirectPath");
                navigate(redirectPath, {replace: true});
            } else {
                // Navigate to dashboard by default if no redirect path is stored
                navigate("/dashboard", {replace: true});
            }
        } else if (auth.error) {
            // If there's an error, redirect to login
            console.error("Authentication error:", auth.error);
            navigate("/login", {replace: true});
        }
    }, [auth.isLoading, auth.isAuthenticated, auth.error, navigate]);

    if (auth.isLoading) {
        return <p>Processing login...</p>;
    }

    if (auth.error) {
        return <p>Authentication failed. Redirecting...</p>;
    }

    return <p>Completing login...</p>;
};

export default CallbackPage;
