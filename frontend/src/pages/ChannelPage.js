import React, { useState,useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import axios from "axios";
import "../styles/ChannelPage.css";

const ChannelPage = () => {
    const { channelName: rawChannelName } = useParams();
    const channelName = rawChannelName.replace(/_/g, " "); // Replace underscores with spaces for display
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState("");
    const [users, setUsers] = useState([]);
    const [channels, setChannels] = useState([]);
    const navigate = useNavigate(); // Use the navigate function to redirect the user to another page

    useEffect(() => { // Get all channel data from the backend when the channel changes
        getChannelData();

        // Poll for new messages every 5 seconds
        const interval = setInterval(getChannelData, 5000);
        return () => clearInterval(interval); // Cleanup on unmount
    }, [rawChannelName]);

    const getChannelData = async () => {
        try {
            const response = await axios.get(`http://localhost:8080/api/channel/${rawChannelName}`, { withCredentials: true });
            setMessages(response.data.messages);
            setUsers(response.data.users);
            setChannels(response.data.channels);
            //console.log(response.data);
        } catch (err) {
            console.error("Error fetching channel data:", err);
            if (err.response && err.response.status === 403) {
                alert("You are not a member of this channel.");
                navigate("/"); // Redirect to home if unauthorized
            }
        }
    }

    const handleSendMessage = async () => {
        if (newMessage.trim() === "") return; // Don't send empty messages
        console.log(newMessage);
        try {
            await axios.post(`http://localhost:8080/api/channel/${rawChannelName}/sendMessage`, {
                content: newMessage
            }, { withCredentials: true });
            setNewMessage(""); // Clear the input after sending
            getChannelData(); // Fetch the latest messages
        } catch (err) {
            console.error("Error sending message:", err);
        }
    }



    

    return (
        <div className="channel-page">
        <div className="sidebar">
            <h3>Channels</h3>
                <ul>
                    {channels.map((ch, index) => (
                        <li key={index}>
                            <Link to={`/channel/${ch.name}`} className="channel-button">
                                {ch.name.replace(/_/g, " ")} {/* Replace underscores with spaces */}
                            </Link>
                        </li>
                    ))}
                </ul>
        </div>

        <div className="chat-container">
            <h2>{channelName}</h2>
            <div className="messages-container">
                {messages.map(msg => (
                    <div key={msg.id} className="message">
                        <strong>{msg.sender.username}: </strong> {msg.content}
                    </div>
                ))}
            </div>
            <div className="input-container">
                <input
                    type="text"
                    placeholder="Type a message..."
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                />
                <button onClick={handleSendMessage}>Send</button>
            </div>
        </div>

        <div className="sidebar">
            <h3>Users</h3>
            <ul className="users-list">
                {users.map((user, index) => (
                    <li class="user-item" key={index}>{user.username}</li>
                ))}
            </ul>
        </div>
    </div>
);
};

export default ChannelPage;