import "./App.css";
import React, {useEffect, useState} from "react";
import {BrowserRouter as Router, Route, Routes, useNavigate} from "react-router-dom";
import axios from "axios";
import LoginPage from "./pages/LoginPage";
import ChannelPage from "./pages/ChannelPage.js";

function App() {

    const [username, setUsername] = useState(null);

    return (<Router>
            <AppRoutes setUsername={setUsername}/>
        </Router>);
}

// Seperated AppRoutes component to make the navigation easier
const AppRoutes = ({setUsername}) => {
    const navigate = useNavigate();

    useEffect(() => {
        // Check session when the app starts
        axios.get("http://localhost:8080/api/auth/check", {withCredentials: true})
            .then(response => {
                console.log("Session found:", response.data);
                setUsername(response.data.username);
                navigate("/channel/General"); // Redirect if session exists
            })
            .catch(() => {
                setUsername(null); // No session found, user must log in
            });
    }, []);

    return (<Routes>
            <Route path="/" element={<LoginPage/>}/>
            <Route path="/channel/:channelName" element={<ChannelPage/>}/>
            {/* Add more routes here */}
        </Routes>);
};

export default App;
