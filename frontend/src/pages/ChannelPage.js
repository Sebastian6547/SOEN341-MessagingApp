import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";
import "../styles/ChannelPage.css";

const ChannelPage = () => {
  const [loggedUser, setLoggedUser] = useState("");
  const { channelName: rawChannelName } = useParams();
  const channelName = rawChannelName.replace(/_/g, " "); // Replace underscores with spaces for display
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState("");
  const [users, setUsers] = useState([]);
  const [channels, setChannels] = useState([]);
  const navigate = useNavigate(); // Use the navigate function to redirect the user to another page
  const [isAdmin, setIsAdmin] = useState(false); // Used to identify if the current user is an admin

  useEffect(() => {
    // Get logged user data when the page is loaded
    getUserData();
    // Get all channel data from the backend when the channel changes
    getChannelData();
    // Check if the current user is admin to display the delete message on hovered
    getAdminData();
    // Poll for new messages every 5 seconds
    const interval = setInterval(getChannelData, 5000);
    return () => clearInterval(interval); // Cleanup on unmount
  }, [rawChannelName]);

  //Check for admin when the page is loaded
  const getUserData = async () => {
    try {
      const response = await axios.get("http://localhost:8080/api/auth/check", {
        withCredentials: true,
      });
      const user = response.data;
      setLoggedUser(user.username);
      console.log("Logged User:", loggedUser);
    } catch (error) {
      console.error("Error fetching user status:", error);
    }
  };

  const getAdminData = async () => {
    try {
      const response = await axios.get(
        "http://localhost:8080/api/admin/checkAdmin",
        { withCredentials: true }
      );
      const isAdmin = response.data;
      console.log("Is Admin:", isAdmin);
      setIsAdmin(isAdmin);
    } catch (error) {
      console.error("Error fetching admin status:", error);
    }
  };

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
    } catch (err) {
      console.error("Error fetching channel data:", err);
      if (err.response && err.response.status === 403) {
        alert("You are not a member of this channel.");
        navigate("/"); // Redirect to home if unauthorized
      }
    }
  };

  const handleLogout = async () => {
    try {
      await axios.post("http://localhost:8080/api/auth/logout", null, {
        withCredentials: true,
      });
      navigate("/"); // Redirect to home after logout
    } catch (err) {
      console.error("Error logging out:", err);
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

  const handleDeleteMessage = async (messageId) => {
    const userConfirmed = window.confirm(
      "Are you sure you want to delete this message?"
    );

    if (!userConfirmed) {
      return;
    }

    try {
      await axios.delete(
        `http://localhost:8080/api/admin/deleteMessage/${messageId}`,
        { withCredentials: true }
      );
      getChannelData(); // Fetch the latest messages
    } catch (err) {
      console.error("Error deleting message:", err);
    }
  };

  const currentChannel = channels.find(
    (channel) => channel.name === rawChannelName
  );

  return (
    <div className="App">
      <div className="sidebar">
        <Channels
          channels={channels}
          channelName={channelName}
          getChannelData={getChannelData}
          loggedUser={loggedUser}
        />
        <UserPanel loggedUser={loggedUser} handleLogout={handleLogout} />
      </div>

      <Channel
        messages={messages}
        newMessage={newMessage}
        setNewMessage={setNewMessage}
        handleSendMessage={handleSendMessage}
        handleDeleteMessage={handleDeleteMessage}
        isAdmin={isAdmin}
        channelName={
          currentChannel && currentChannel.type === "DM"
            ? currentChannel.name
                .replace(/_/g, " ")
                .split(" ")
                .filter((name) => name !== loggedUser)
                .join(" ")
            : currentChannel
            ? currentChannel.name.replace(/_/g, " ")
            : ""
        }
      />
      <Members channelName={channelName} users={users} />
    </div>
  );
};

function UserPanel({ loggedUser, handleLogout }) {
  return (
    <div className="user-pannel">
      <h2>{loggedUser}</h2>
      <button className="logout-button" onClick={handleLogout}>
        Logout
      </button>
    </div>
  );
}

function Channels({ channelName, channels, getChannelData, loggedUser }) {
  const [channelType, setChannelType] = useState("PC");

  const handleChannelTypeChange = (type) => {
    console.log("Changing channel type to:", type);
    setChannelType(type);
    getChannelData();
  };
  return (
    <div className="channels">
      <ChannelsLogo
        channelType={channelType}
        handleChannelTypeChange={handleChannelTypeChange}
      />
      <ChannelList
        getChannelData={getChannelData}
        channelName={channelName}
        channels={channels}
        channelType={channelType}
        loggedUser={loggedUser}
      />
    </div>
  );
}

function ChannelsLogo({ channelType, handleChannelTypeChange }) {
  const [isHovered, setIsHovered] = useState(false);
  return (
    <div className="channels-logo">
      <button
        className="channel-type-button"
        onClick={() =>
          handleChannelTypeChange(channelType === "PC" ? "DM" : "PC")
        }
        onMouseEnter={() => setIsHovered(true)}
        onMouseLeave={() => setIsHovered(false)}
      >
        {(isHovered || channelType === "PC") && (
          <img
            className="channel-type-img"
            src={require("../styles/public-icon.png")}
            alt="Public Channels"
          ></img>
        )}
        {(isHovered || channelType === "DM") && (
          <img
            className="channel-type-img"
            src={require("../styles/private-icon.png")}
            alt="Private Channels"
          ></img>
        )}
      </button>
      <h3>CHANNELS</h3>
    </div>
  );
}

function ChannelList({
  channelName,
  channels,
  getChannelData,
  channelType,
  loggedUser,
}) {
  return (
    <ul className="channel-list">
      {channels
        .filter((channel) => channel.type === channelType)
        .map((channel, index) => (
          <ChannelButton
            channelName={channelName}
            getChannelData={getChannelData}
            channelKey={index}
            channel={channel}
            loggedUser={loggedUser}
          />
        ))}
      <NewChannelButton />
    </ul>
  );
}

function ChannelButton({
  channelName,
  getChannelData,
  channelKey,
  channel,
  loggedUser,
}) {
  const navigate = useNavigate();
  return (
    <li
      className={
        channelName === channel.name.replace(/_/g, " ")
          ? "channel-button channel-button-active"
          : "channel-button"
      }
      key={channelKey}
    >
      <button
        className={
          channelName === channel.name.replace(/_/g, " ")
            ? "button button-active"
            : "button"
        }
        style={{ fontWeight: "bold" }}
        onClick={() => {
          console.log(
            `Navigating to channel: ${channel.name} (channelName: ${channelName}) (Channel type: ${channel.type})`
          );
          navigate(`/channel/${channel.name}`);
        }}
      >
        {channel.type === "DM"
          ? channel.name
              .replace(/_/g, " ")
              .split(" ")
              .filter((name) => name !== loggedUser)
              .join(" ")
          : channel.name.replace(/_/g, " ")}
      </button>
    </li>
  );
}

function NewChannelButton() {
  return (
    <li className="channel-button" key={-1} style={{ paddingLeft: "0rem" }}>
      <button className="new-button">+ create new</button>
    </li>
  );
}

function Channel({
  messages,
  newMessage,
  setNewMessage,
  handleSendMessage,
  handleDeleteMessage,
  isAdmin,
  channelName,
}) {
  return (
    <div className="channel">
      <ChannelLogo channelName={channelName} />
      <Messages
        messages={messages}
        channelName={channelName}
        handleDeleteMessage={handleDeleteMessage}
        isAdmin={isAdmin}
      />
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

function Messages({ messages, channelName, handleDeleteMessage, isAdmin }) {
  const messagesEndRef = React.useRef(null);

  React.useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [channelName]);

  return (
    <div className="message-container">
      {messages.map((msg) => (
        <Message
          key={msg.id}
          id={msg.id}
          sender={msg.sender}
          content={msg.content}
          time={msg.date_time}
          handleDeleteMessage={handleDeleteMessage}
          isAdmin={isAdmin}
        />
      ))}
      <div ref={messagesEndRef} />
    </div>
  );
}

function Message(props) {
  const [isHovered, setIsHovered] = useState(false); //Use to check if it is being hovered
  return (
    <div
      className="message"
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <div className="message-header">
        <strong>
          {props.sender.username} - {props.time}
        </strong>
        {
          // This button only appear when hovered and the current user is an admin
          isHovered && props.isAdmin && (
            <DeleteMessage
              id={props.id}
              handleDeleteMessage={props.handleDeleteMessage}
            />
          )
        }
      </div>
      {props.content}
    </div>
  );
}

function DeleteMessage({ id, handleDeleteMessage }) {
  return (
    <div>
      <button
        className="message-delete-button"
        onClick={() => handleDeleteMessage(id)} // Call delete function
      >
        Delete
      </button>
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
  return (
    <div className={active ? "members" : "members members-inactive"}>
      <MembersLogo active={active} setActive={setActive} />
      <MemberList active={active} activeChannel={channelName} users={users} />
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
          active
            ? "members-logo-img"
            : "members-logo-img members-logo-img-inactive"
        }
        src={require("../styles/members-icon.png")}
        alt="Members Icon"
        onClick={() => toggleActive()}
      />
      <p
        className={
          active
            ? "members-logo-txt"
            : "members-logo-txt members-logo-txt-inactive"
        }
      >
        MEMBERS
      </p>
    </div>
  );
}

function MemberList({ active, users }) {
  return (
    <ul className="member-list">
      {users.map((member, index) => (
        <MemberButton active={active} key={index} member={member} />
      ))}
    </ul>
  );
}

function MemberButton(props) {
  return (
    <li
      className={
        props.active ? "member-button" : "member-button member-button-inactive"
      }
      key={props.key}
    >
      <button
        className="button"
        style={{
          borderRadius: "50px",
          paddingLeft: "0.5rem",
        }}
      >
        {props.member.username}
      </button>
    </li>
  );
}

export default ChannelPage;
