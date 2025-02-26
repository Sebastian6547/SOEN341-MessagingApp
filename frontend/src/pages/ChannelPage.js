import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
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

  useEffect(() => {
    // Get all channel data from the backend when the channel changes
    getChannelData();

    // Poll for new messages every 5 seconds
    const interval = setInterval(getChannelData, 5000);
    return () => clearInterval(interval); // Cleanup on unmount
  }, [rawChannelName]);

  const getChannelData = async () => {
    try {
      const response = await axios.get(
        `http://localhost:8080/api/channel/${rawChannelName}`,
        { withCredentials: true }
      );
      setMessages(response.data.messages);
      setUsers(response.data.users);
      setChannels(response.data.channels);
      console.log(response.data, "channelName: " + channelName);
      console.log("Updated channel list:", response.data.channels);
    } catch (err) {
      console.error("Error fetching channel data:", err);
      if (err.response && err.response.status === 403) {
        alert("You are not a member of this channel.");
        navigate("/"); // Redirect to home if unauthorized
      }
    }
  };

  const handleSendMessage = async () => {
    if (newMessage.trim() === "") return; // Don't send empty messages
    console.log("Sent message:", newMessage);
    try {
      await axios.post(
        `http://localhost:8080/api/channel/${rawChannelName}/sendMessage`,
        {
          content: newMessage,
        },
        { withCredentials: true }
      );
      setNewMessage(""); // Clear the input after sending
      getChannelData(); // Fetch the latest messages
    } catch (err) {
      console.error("Error sending message:", err);
    }
  };

  return (
    <div className="App">
      <Channels
        channels={channels}
        channelName={channelName}
        getChannelData={getChannelData}
      />
      <Channel
        messages={messages}
        newMessage={newMessage}
        setNewMessage={setNewMessage}
        handleSendMessage={handleSendMessage}
        channelName={channelName}
      />
      <Members channelName={channelName} users={users} />
    </div>
  );
};

function Channels({ channelName, channels, getChannelData }) {
  return (
    <div className="channels">
      <ChannelsLogo />
      <ChannelList
        getChannelData={getChannelData}
        channelName={channelName}
        channels={channels}
      />
    </div>
  );
}

function ChannelsLogo() {
  return (
    <div className="channels-logo">
      <h2>CHANNELS</h2>
    </div>
  );
}

function ChannelList({ channelName, channels, getChannelData }) {
  return (
    <ul className="channel-list">
      {channels.map((channel, index) => (
        <ChannelButton
          channelName={channelName}
          getChannelData={getChannelData}
          channelKey={index}
          channel={channel}
        />
      ))}
      <NewChannelButton />
    </ul>
  );
}

function ChannelButton({ channelName, channelKey, channel }) {
    const navigate = useNavigate();
    const [currentUser, setCurrentUser] = useState("");
    const [isAdmin, setIsAdmin] = useState(false);

    // Getting current user and check if they are admin
    useEffect(() => {
        const fetchCurrentUser = async () => {
            try {
                const response = await axios.get("http://localhost:8080/api/auth/check", { withCredentials: true });
                setCurrentUser(response.data.username);

                // Check if current user is admin
                const adminResponse = await axios.get(
                    `http://localhost:8080/api/admin/checkAdmin?username=${response.data.username}`
                );
                setIsAdmin(adminResponse.data); // Set isAdmin based on the response

                console.log("Current User:", response.data.username);
                console.log("Is Admin:", adminResponse.data);
            } catch (err) {
                console.error("Error fetching current user:", err);
            }
        };

        fetchCurrentUser();
    }, []);

    const handleDeleteChannel = async () => {
        // Call the backend API to delete the channel
        try {
            const response = await axios.delete(
                `http://localhost:8080/api/channel/delete-channel/${channel.name.replace(/ /g, "_")}`
            );
            console.log("Channel deleted successfully:", response.data);
            // If the deleted channel is the current one, navigate to the "general" channel
            if (window.location.pathname === `/channel/${channel.name.replace(/ /g, "_")}`) {
                navigate("/channel/General");
            }
        } catch (error) {
            console.error("Error deleting channel:", error);
        }
    };

    const confirmDelete = () => {
        // Show confirmation dialog
        const isConfirmed = window.confirm(`Are you sure you want to delete the channel ${channel.name.replace(/_/g, " ")}?`);

        if (isConfirmed) {
            handleDeleteChannel();
        }
    };

    return (
        <li
            className={channelName === channel.name.replace(/_/g, " ")
                ? "channel-button channel-button-active"
                : "channel-button"
            }
            key={channelKey}
        >
            <button
                className={channelName === channel.name.replace(/_/g, " ")
                    ? "button button-active"
                    : "button"
                }
                onClick={() => {
                    console.log(`Navigating to channel: ${channel.name} (channelName: ${channelName})`);
                    navigate(`/channel/${channel.name}`);
                }}
            >
                {channel.name.replace(/_/g, " ")}
            </button>
            {/* Button to delete channel, only visible to admins and not available for the General channel */}
            {isAdmin && channel.name !== "General" && (
                <span
                    className="delete-channel-sign"
                    onClick={confirmDelete}
                />
            )}
        </li>
    );
}



function NewChannelButton() {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [channelName, setChannelName] = useState("");
    const [channelType, setChannelType] = useState("PC"); // Default to "PC"
    const [errorMessage, setErrorMessage] = useState("");

    const [currentUser, setCurrentUser] = useState("");
    const [isAdmin, setIsAdmin] = useState(false);

    // Getting current user and check if they are admin
    useEffect(() => {
        const fetchCurrentUser = async () => {
            try {
                const response = await axios.get("http://localhost:8080/api/auth/check", { withCredentials: true });
                setCurrentUser(response.data.username);

                // Check if current user is admin
                const adminResponse = await axios.get(
                    `http://localhost:8080/api/admin/checkAdmin?username=${response.data.username}`
                );
                setIsAdmin(adminResponse.data);

                console.log("Current User:", response.data.username);
                console.log("Is Admin:", adminResponse.data);
            } catch (err) {
                console.error("Error fetching current user:", err);
            }
        };

        fetchCurrentUser();
    }, []);

    const handleCreateChannel = async () => {
        if (channelName.trim() === "") return;

        console.log("Creating channel:", channelName, channelType);

        try {
            await axios.post(
                "http://localhost:8080/api/channel/create-channel",
                { channelName, channelType, currentUser },
                { withCredentials: true }
            );

            console.log("Channel Created Successfully");

            // Closing modal and resetting input fields
            setIsModalOpen(false);
            setChannelName("");
            setErrorMessage("");
        } catch (err) {
            console.error("Error creating channel:", err);
            setErrorMessage("Failed to create channel. Try again.");
        }
    };

    return (
        <>
            {/* Button to create channel, only visible to admins */}
            {isAdmin && (
                <li className="channel-button" key={-1} style={{ paddingLeft: "0rem" }}>
                    <button className="new-button" onClick={() => setIsModalOpen(true)}>
                        + Create New
                    </button>
                </li>
            )}

            {/* Modal */}
            {isModalOpen && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <h2>Create a New Channel</h2>

                        <input
                            type="text"
                            placeholder="Enter channel name"
                            value={channelName}
                            onChange={(e) => setChannelName(e.target.value)}
                        />

                        <select value={channelType} onChange={(e) => setChannelType(e.target.value)}>
                            <option value="PC">PC</option>
                            <option value="DM">DM</option>
                        </select>

                        {errorMessage && <p style={{ color: "red" }}>{errorMessage}</p>}

                        <div className="modal-buttons">
                            <button onClick={handleCreateChannel} className="create-btn">
                                Create Channel
                            </button>
                            <button onClick={() => setIsModalOpen(false)} className="cancel-btn">
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
}

function Channel({
  messages,
  newMessage,
  setNewMessage,
  handleSendMessage,
  channelName,
}) {
  return (
    <div className="channel">
      <ChannelLogo channelName={channelName} />
      <Messages messages={messages} channelName={channelName} />
      <InputBox
        newMessage={newMessage}
        setNewMessage={setNewMessage}
        handleSendMessage={handleSendMessage}
      />
    </div>
  );
}

function ChannelLogo({ channelName }) {
  return (
    <div className="channel-logo">
      <h1>{channelName}</h1>
    </div>
  );
}

function Messages({ messages, channelName }) {
  const messagesEndRef = React.useRef(null);

  React.useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [channelName]);

  return (
    <div className="message-container">
      {messages.map((msg) => (
        <Message
          key={msg.id}
          sender={msg.sender}
          content={msg.content}
          time={msg.date_time}
        />
      ))}
      <div ref={messagesEndRef} />
    </div>
  );
}

function Message(props) {
  return (
    <div className="message">
      <strong>
          {props.sender.username} {props.sender.role === "ADMIN" ? "(Admin)" : ""} - {props.time}
      </strong>
      {props.content}
    </div>
  );
}

function InputBox({ newMessage, setNewMessage, handleSendMessage }) {
  return (
    <div className="input-box">
      <input
        className="message-input"
        type="text"
        placeholder="Type a message..."
        value={newMessage}
        onChange={(e) => setNewMessage(e.target.value)}
        onSubmit={() => handleSendMessage()}
      />
      <button className="send-button" onClick={() => handleSendMessage()}>
        Send
      </button>
    </div>
  );
}

function Members({ channelName, users }) {
  const [active, setActive] = React.useState(false);
    const [currentUser, setCurrentUser] = useState(""); // Track the current logged-in user's username
    const [isAdmin, setIsAdmin] = useState(false); // Track if the current user is an admin

    // Getting current user and check if they are admin
    useEffect(() => {
        const fetchCurrentUser = async () => {
            try {
                const response = await axios.get("http://localhost:8080/api/auth/check", { withCredentials: true });
                setCurrentUser(response.data.username);

                // Check if current user is admin
                const adminResponse = await axios.get(
                    `http://localhost:8080/api/admin/checkAdmin?username=${response.data.username}`
                );
                setIsAdmin(adminResponse.data);

                console.log("Current User:", response.data.username);
                console.log("Is Admin:", adminResponse.data);
            } catch (err) {
                console.error("Error fetching current user:", err);
            }
        };

        fetchCurrentUser();
    }, []);

    const changeUserRole = async (targetUsername, newRole) => {
        try {
            const response = await axios.put(
                `http://localhost:8080/api/admin/updateRole`,
                null,
                {
                    params: {
                        currentUsername: currentUser,
                        targetUsername: targetUsername,
                        newRole: newRole,
                    },
                    withCredentials: true,
                }
            );
        } catch (err) {
            console.error("Error changing user role:", err);
            alert("Failed to change user role.");
        }
    };


    return (
    <div className={active ? "members" : "members members-inactive"}>
      <MembersLogo active={active} setActive={setActive} />
      <MemberList active={active} activeChannel={channelName} users={users} currentUser={currentUser} isAdmin={isAdmin} changeUserRole={changeUserRole}/>
    </div>
  );
}

function MembersLogo({ active, setActive }) {
    function toggleActive() {
        setActive(!active);
    }
    return (
        <div className="members-logo">
            <img
                className={
                    active ? "members-logo-img" : "members-logo-img members-logo-img-inactive"
                }
                src={require("../styles/members-icon.png")}
                alt="Members Icon"
                onClick={toggleActive}
            />
            <p
                className={
                    active ? "members-logo-txt" : "members-logo-txt members-logo-txt-inactive"
                }
            >
                MEMBERS
            </p>
        </div>
    );
}

function MemberList({ active, users, currentUser, isAdmin, changeUserRole, activeChannel }) {
  return (
    <ul className="member-list">
      {users.map((member, index) => (
        <MemberButton active={active} key={index} member={member} currentUser={currentUser} isAdmin={isAdmin} changeUserRole={changeUserRole} activeChannel={activeChannel}/>
      ))}
    </ul>
  );
}

function MemberButton({ member, currentUser, isAdmin, changeUserRole, activeChannel }) {
    const [isAdminRole, setIsAdminRole] = useState(member.role === "ADMIN");
    const [adminsCount, setAdminsCount] = useState(0);

    // Get the number of admins in the channel
    const fetchAdminsCount = async () => {
        try {
            const response = await fetch(`http://localhost:8080/api/channel/admins-count/${activeChannel}`);
            const data = await response.json();
            setAdminsCount(data.adminsCount); // Update the state with fetched value
        } catch (error) {
            console.error("Error fetching admins count:", error);
        }
    };

    // Call fetchAdminsCount to get the initial admin count
    useEffect(() => {
        fetchAdminsCount();
    }, [activeChannel]);  // Re-fetch if the channel name changes

    // Handling the role toggle
    const handleToggleChange = async () => {
        const newRole = isAdminRole ? "MEMBER" : "ADMIN";

        // Fetch the latest admins count to make sure it is updated before making changes
        await fetchAdminsCount();

        // Check if we are trying to remove the last admin (because there should always be at least one admin in a channel)
        if (newRole === "MEMBER" && adminsCount <= 1) {
            alert("You cannot remove the last admin.");
            return;
        }

        // Do the role change if it's valid
        if (window.confirm(`Are you sure you want to make ${member.username} ${newRole === "ADMIN" ? "an Admin" : "a Member"}?`)) {
            changeUserRole(member.username, newRole);

            // Get the updated admin count again
            await fetchAdminsCount();

            setIsAdminRole(!isAdminRole); // Toggle the role state immediately
        }
    };

    return (
        <li className="member-button">
            <div className="member-info">

                <span className="username">{member.username}</span>

                {/* Show the toggle only if the current user is an admin */}
                {isAdmin && (
                    <div className="role-toggle-container">
                        {/* For the toggle -> left is "Member" and right is "Admin" */}
                        <label className="role-toggle">
                            <span className="role-label">Member</span>
                            <input
                                type="checkbox"
                                checked={isAdminRole}
                                onChange={handleToggleChange}
                            />
                            <span className="role-label">Admin</span>
                        </label>
                    </div>
                )}
            </div>
        </li>
    );
}

export default ChannelPage;
