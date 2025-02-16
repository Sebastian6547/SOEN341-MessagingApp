import React, { useState, useEffect } from "react";
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
      //console.log(response.data);
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
    console.log(newMessage);
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

  const [activeChannel, setActiveChannel] = React.useState("General");
  return (
    <div className="App">
      <Channels
        activeChannel={activeChannel}
        setActiveChannel={setActiveChannel}
        channels={channels}
      />
      <Channel
        activeChannel={activeChannel}
        messages={messages}
        newMessage={newMessage}
        setNewMessage={setNewMessage}
      />
      <Members activeChannel={activeChannel} users={users} />
    </div>
  );
};

function Channels({ activeChannel, setActiveChannel, channels }) {
  return (
    <div className="channels">
      <ChannelsLogo />
      <ChannelList
        activeChannel={activeChannel}
        setActiveChannel={setActiveChannel}
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

function ChannelList({ activeChannel, setActiveChannel, channels }) {
  return (
    <ul className="channel-list">
      {channels.map((channel, index) => (
        <ChannelButton
          activeChannel={activeChannel}
          setActiveChannel={setActiveChannel}
          key={index}
          channel={channel}
        />
      ))}
      <NewChannelButton />
    </ul>
  );
}

function ChannelButton(props) {
  return (
    <li
      className={
        props.activeChannel === props.channel.name
          ? "channel-button channel-button-active"
          : "channel-button"
      }
      key={props.key}
    >
      <Link
        to={`/channel/${props.channel.name}`}
        className={
          props.activeChannel === props.channel
            ? "button button-active"
            : "button"
        }
        style={{ fontWeight: "bold" }}
        onClick={() => props.setActiveChannel(props.channel.name)}
      >
        {props.channel.name.replace(/_/g, " ")}{" "}
        {/* Replace underscores with spaces */}
      </Link>
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

function Channel({ activeChannel, messages, newMessage, setNewMessage }) {
  return (
    <div className="channel">
      <ChannelLogo activeChannel={activeChannel} />
      <Messages activeChannel={activeChannel} messages={messages} />
      <InputBox newMessage={newMessage} setNewMessage={setNewMessage} />
    </div>
  );
}

function ChannelLogo({ activeChannel }) {
  return (
    <div className="channel-logo">
      <h1>{activeChannel}</h1>
    </div>
  );
}

function Messages({ activeChannel, messages }) {
  const messagesEndRef = React.useRef(null);

  React.useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [activeChannel]);

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
        {props.sender.username} - {props.time}
      </strong>
      {props.content}
    </div>
  );
}

function InputBox({ newMessage, setNewMessage }) {
  return (
    <div className="input-box">
      <input
        className="message-input"
        type="text"
        placeholder="Type a message..."
        value={newMessage}
        onChange={(e) => setNewMessage(e.target.value)}
      />
      <button className="send-button">Send</button>
    </div>
  );
}

function Members({ activeChannel, users }) {
  const [active, setActive] = React.useState(false);
  return (
    <div className={active ? "members" : "members members-inactive"}>
      <MembersLogo active={active} setActive={setActive} />
      <MemberList active={active} activeChannel={activeChannel} users={users} />
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

function MemberList({ active, activeChannel, users }) {
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
          backgroundColor: "white",
        }}
      >
        {props.member.username}
      </button>
    </li>
  );
}

export default ChannelPage;
