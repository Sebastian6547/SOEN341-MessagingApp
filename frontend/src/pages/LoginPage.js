import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import "../styles/LoginPage.css";

const LoginPage = () => {
  const [username, setUsername] = useState(""); // Create a state variable to store the username
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate(); // Use the navigate function to redirect the user to another page

  const handleLogin = async (e) => {
    // Handles the login form submission. This is called when user clicks the login button
    e.preventDefault(); //prevent refresh
    setError(""); //reset error before we make the request

    try {
      const response = await axios.post(
        "http://localhost:8080/api/auth/login",
        {
          // Make a POST request to the login endpoint from the backend
          username,
          password,
        },
        { withCredentials: true }
      ); // for cookies

      if (response.status === 200) {
        navigate("/channel/General"); // Redirect to the General channel
      }
    } catch (err) {
      setError("Invalid username or password");
    }
  };

  const handleSignup = () => {
    // Redirect to the signup page when the signup link is clicked
    navigate("/signup");
  };

  return (
    <>
      <div className="login-container">
        <h2>Login</h2>
        <form onSubmit={handleLogin}>
          <input
            type="text"
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          <button type="submit">Login</button>
        </form>
        {/* Redirect to the signup page when the signup button is clicked */}
        {error && <p className="error">{error}</p>}{" "}
        {/* Display the error message only if there is an error */}
      </div>
      <div className="signup-container">
        <p>Don't have an account yet?</p>
        <p>
          <br />
        </p>
        <button onClick={handleSignup} className="signup-button">
          Sign Up
        </button>
      </div>
    </>
  );
};

export default LoginPage;
