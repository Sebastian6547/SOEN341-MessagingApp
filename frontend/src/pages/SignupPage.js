import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import "../styles/SignupPage.css";

const SignupPage = () => {
  const navigate = useNavigate(); // Use the navigate function to redirect the user to another page
  const [username, setUsername] = useState(""); // Create a state variable to store the username
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const role = "MEMBER";

  const handleSignup = async (e) => {
    // Handles the login form submission. This is called when user clicks the login button
    e.preventDefault(); //prevent refresh
    setError(""); //reset error before we make the request
    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }
    try {
      const response = await axios.post(
        "http://localhost:8080/api/auth/register",
        {
          // Make a POST request to the login endpoint from the backend
          username,
          password,
          role,
        },
        { withCredentials: true }
      ); // for cookies

      if (response.status === 200) {
        alert("Signup successful!");
        navigate("/"); // Redirect to the General channel
      }
    } catch (err) {
      setError("Invalid username or password");
    }
  };
  return (
    <>
      <form className="signup-form" onSubmit={handleSignup}>
        <h2>Sign Up</h2>
        <input
          type="text"
          placeholder="Enter your username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Enter your password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Confirm your password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          required
        />
        {error && <p className="error">{error}</p>}
        <button type="submit">Sign Up</button>
        <p>
          <br />
        </p>
        <button type="button" onClick={() => navigate("/")}>
          Back to Login
        </button>
      </form>
    </>
  );
};

export default SignupPage;
