import React, {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import axios from "axios";
import "../styles/ChannelPage.css";

const ChannelPage = () => {
  const [loggedUser, setLoggedUser] = useState("");
  const {channelName: rawChannelName} = useParams();
  const channelName = rawChannelName.replace(/_/g, " "); // Replace underscores with spaces for display
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState("");
  const [users, setUsers] = useState([]);
  const [channels, setChannels] = useState([]);
  const navigate = useNavigate(); // Use the navigate function to redirect the user to another page
  const [isAdmin, setIsAdmin] = useState(false);
  const [notifChannels, setNotifChannels] = useState(new Set());
  const [lastMessageID, setLastMessageID] = useState(null);
  const [justSentMessage, setJustSentMessage] = useState(false);

  // Getting current user and check if they are admin
  const getUserData = async () => {
    try {
      const response = await axios.get("http://localhost:8080/api/auth/check", {
        withCredentials: true,
      });
      setLoggedUser(response.data.username);

      // Check if current user is admin
      const adminResponse = await axios.get(`http://localhost:8080/api/admin/checkAdmin?username=${response.data.username}`);
      setIsAdmin(adminResponse.data); // Set isAdmin based on the response

      console.log("Current User:", response.data.username);
      console.log("Logged User:", loggedUser);
      console.log("Is Admin:", adminResponse.data);
    } catch (err) {
      console.error("Error fetching current user:", err);
    }
  };

  const currentChannel = channels.find((channel) => channel.name === rawChannelName);

  useEffect(() => {
    const interval = setInterval(() => {
      checkNewMessage();
    }, 15000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    // Get logged user data when the page is loaded
    getUserData();
    // Get all channel data from the backend when the channel changes
    // Poll for new messages every 5 seconds
    getChannelData();

    const interval = setInterval(getChannelData, 5000);
    return () => clearInterval(interval); // Cleanup on unmount
  }, [rawChannelName]);

  useEffect(() => {
    //Update channel type state when the channel changes
    const currentChannel = channels.find((channel) => channel.name === rawChannelName);

    if (currentChannel) {
      console.log("Setting channel type:", currentChannel.type);
      setChannelType(currentChannel.type);
      console.log("new channel change (type change)");
    } else {
      console.log("Current channel is undefined. Waiting for channels to load...");
    }
  }, [currentChannel && currentChannel.type, rawChannelName]); // Runs whenever `channels` or `rawChannelName` changes

  //so user doesnt trigger notif with his own message. Could  make getChannel return the message and then pass message to update.
  useEffect(() => {
    if (justSentMessage && messages.length > 0) {
      updateLastSeenMessage(messages[messages.length - 1].id);
      setJustSentMessage(false); // Reset the flag
    }
  }, [messages, justSentMessage]);

  const getChannelData = async (targetChannel = rawChannelName) => {
    try {
      const response = await axios.get(`http://localhost:8080/api/channel/${targetChannel}`, {withCredentials: true});
      setMessages(response.data.messages);
      setUsers(response.data.users);
      setChannels(response.data.channels);
      setLastMessageID(response.data.lastMessageID);
      console.log("last seen message (id) by the user was: " + response.data.lastMessageID);
      
    } catch (err) {
      console.error("Error fetching channel data:", err);
      if (err.response && err.response.status === 403) {
        alert("Redirecting to the General channel");
        navigate("/channel/General"); // Redirect to home if unauthorized
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

  //compare most recent message in every channel with last viewed message in every channel.
  const checkNewMessage = async () => {
    try {
      const response = await axios.get("http://localhost:8080/api/channel/getUnreadChannels", {withCredentials: true});
      const channelsNewMsg = response.data;
      setNotifChannels(new Set(channelsNewMsg));
    } catch (err) {
      console.error("Error checking for new messages to notify user:", err);
    }
  };

  const handleSendMessage = async () => {
    if (newMessage.trim() === "") {
      return;
    } // Don't send empty messages
    console.log("Sent message:", newMessage);
    try {
      await axios.post(`http://localhost:8080/api/channel/${rawChannelName}/sendMessage`, {
        content: newMessage,
      }, {withCredentials: true});
      setNewMessage(""); // Clear the input after sending

      await getChannelData(); // Fetch the latest messages
      setJustSentMessage(true);
    } catch (err) {
      console.error("Error sending message:", err);
    }
  };

  const handleDeleteMessage = async (messageId) => {
    const userConfirmed = window.confirm("Are you sure you want to delete this message?");

    if (!userConfirmed) {
      return;
    }

    try {
      await axios.delete(`http://localhost:8080/api/admin/deleteMessage/${messageId}`, {withCredentials: true});
      getChannelData(); // Fetch the latest messages
    } catch (err) {
      console.error("Error deleting message:", err);
    }
  };

  const [channelType, setChannelType] = useState("PC");

  const handleChannelTypeChange = (type) => {
    console.log("Changing channel type to:", type);
    setChannelType(type);
    getChannelData();
    console.log("handletypechange call");
  };

  const updateLastSeenMessage = async (msgID) => {
    console.log("clicked!, message read , updating table with latest message " + msgID);
    try {
      await axios.post(`http://localhost:8080/api/channel/${rawChannelName}/updateLastSeenMessage`, {
        lastSeenMessageID: msgID,
      }, {withCredentials: true});
    } catch (err) {
      console.error("Error updating the last seen message:", err);
    }
  };

  return (<div className="App">
    <div className="sidebar">
      <Channels
        channels={channels}
        channelName={channelName}
        getChannelData={getChannelData}
        isAdmin={isAdmin}
        notifChannels={notifChannels}
        loggedUser={loggedUser}
        channelType={channelType}
        handleChannelTypeChange={handleChannelTypeChange}
      />
      <UserPanel loggedUser={loggedUser} handleLogout={handleLogout}/>
    </div>

    <Channel messages={messages}
      newMessage={newMessage}
      setNewMessage={setNewMessage}
      handleSendMessage={handleSendMessage}
      handleDeleteMessage={handleDeleteMessage}
      isAdmin={isAdmin}
      channelName={currentChannel && currentChannel.type === "DM" ? currentChannel.name
        .replace(/_/g, " ")
        .split(" ")
        .filter((name) => name !== loggedUser)
        .join(" ") : currentChannel ? currentChannel.name.replace(/_/g, " ") : ""}
      lastMessageID={lastMessageID}
      setLastMessageID={setLastMessageID}
      updateLastSeenMessage={updateLastSeenMessage}
      setNotifChannels={setNotifChannels}
    />
    <Members
      channelName={channelName}
      users={users}
      isAdmin={isAdmin}
      loggedUser={loggedUser}
      setIsAdmin={setIsAdmin}
    />
  </div>);
};

function UserPanel({loggedUser, handleLogout}) {
  return (<div className="user-pannel">
    <h2>{loggedUser}</h2>
    <button className="logout-button" onClick={handleLogout}>
        Logout
    </button>
  </div>);
}

function Channels({
  channelName, channels, getChannelData, isAdmin, loggedUser, notifChannels, channelType, handleChannelTypeChange,
}) {
  console.log("Rendering channels:", channels);
  console.log("Current channel:", channelName);
  console.log("Channel type:", channelType);
  return (<div className="channels">
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
      isAdmin={isAdmin}
      notifChannels={notifChannels}
    />
  </div>);
}

function ChannelsLogo({channelType, handleChannelTypeChange}) {
  const [isHovered, setIsHovered] = useState(false);
  return (<div className="channels-logo">
    <button
      className="channel-type-button"
      onClick={() => handleChannelTypeChange(channelType === "PC" ? "DM" : "PC")}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      {(isHovered || channelType === "PC") && (<img
        className="channel-type-img"
        src={require("../styles/public-icon.png")}
        alt="Public Channels"
      ></img>)}
      {(isHovered || channelType === "DM") && (<img
        className="channel-type-img"
        src={require("../styles/private-icon.png")}
        alt="Private Channels"
      ></img>)}
    </button>
    <h3>CHANNELS</h3>
  </div>);
}

function ChannelList({
  channelName, channels, getChannelData, channelType, isAdmin, loggedUser, notifChannels,
}) {
  return (<ul className="channel-list">
    {channels
      .filter((channel) => channel.type === channelType)
      .map((channel, index) => (<ChannelButton
        key={index}
        channelName={channelName}
        getChannelData={getChannelData}
        channelKey={index}
        channel={channel}
        isAdmin={isAdmin}
        loggedUser={loggedUser}
        notifChannels={notifChannels.has(channel.name)}
      />))}
    {channelType === "PC" && (<>
      <NewChannelButton isAdmin={isAdmin} loggedUser={loggedUser}/>
      <JoinChannelButton loggedUser={loggedUser}/>
    </>)}
    {channelType === "DM" && <NewDMButton loggedUser={loggedUser}/>}
  </ul>);
}

function ChannelButton({
  channelName, getChannelData, channelKey, channel, isAdmin, loggedUser, notifChannels,
}) {
  const navigate = useNavigate();

  const handleChannelSwitch = async () => {
    try {
      await getChannelData(channel.name); // Wait until data is loaded
      navigate(`/channel/${channel.name}`); // Switch after loading
    } catch (err) {
      console.error("Failed to load channel data:", err);
    }
  };

  const handleDeleteChannel = async () => {
    // Call the backend API to delete the channel
    try {
      const response = await axios.delete(`http://localhost:8080/api/channel/delete-channel/${channel.name.replace(/ /g, "_")}`);
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

  return (<li
    className={`
        channel-button
        ${channelName === channel.name.replace(/_/g, " ") ? "channel-button-active" : "channel-button"}
      `}
    key={channelKey}
  >
    <button
      className={channelName === channel.name.replace(/_/g, " ") ? "button button-active" : "button"}
      style={{fontWeight: "bold", position: "relative"}}
      onClick={handleChannelSwitch} // Trigger with wait
    >
      {channel.type === "DM" ? channel.name
        .replace(/_/g, " ")
        .split(" ")
        .filter((name) => name !== loggedUser)
        .join(" ") : channel.name.replace(/_/g, " ")}

      {notifChannels && <span className="channel-button-notification"></span>}
    </button>
    {/* Button to delete channel, only visible to admins and not available for the General channel */}
    {isAdmin && channel.name !== "General" && (<span className="delete-channel-sign" onClick={confirmDelete}/>)}
  </li>);
}

function NewChannelButton({isAdmin, loggedUser}) {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [channelName, setChannelName] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  const handleCreateChannel = async () => {
    if (channelName.trim() === "") {
      return;
    }

    const formattedChannelName = channelName.replace(/ /g, "_");

    console.log("Creating channel:", formattedChannelName);

    try {
      await axios.post("http://localhost:8080/api/channel/create-channel", {
        formattedChannelName,
        loggedUser
      }, {withCredentials: true});

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

  return (<>
    {/* Button to create channel, only visible to admins */}
    {isAdmin && (<li className="channel-button" key={-1} style={{paddingLeft: "0rem"}}>
      <button className="new-button" onClick={() => setIsModalOpen(true)}>
            + Create New
      </button>
    </li>)}

    {/* Modal */}
    {isModalOpen && (<div className="modal-overlay">
      <div className="modal-content">
        <h2>Create a New Channel</h2>

        <input
          type="text"
          placeholder="Enter channel name"
          value={channelName}
          onChange={(e) => setChannelName(e.target.value)}
        />

        {errorMessage && <p style={{color: "red"}}>{errorMessage}</p>}

        <div className="modal-buttons">
          <button onClick={handleCreateChannel} className="create-btn">
                Create Channel
          </button>
          <button
            onClick={() => setIsModalOpen(false)}
            className="cancel-btn"
          >
                Cancel
          </button>
        </div>
      </div>
    </div>)}
  </>);
}

// For now you have to type in the exact name
// Reused the NewChannelButton Codes
function JoinChannelButton({loggedUser}) {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [channelName, setChannelName] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  const handleJoinChannel = async () => {
    console.log("Joining Channel:", channelName);
    const formattedChannelName = channelName.replace(/ /g, "_");
    try {
      await axios.post("http://localhost:8080/api/channel/join", {
        formattedChannelName,
        loggedUser
      }, {withCredentials: true});
      // Closing modal and resetting input fields
      setIsModalOpen(false);
      setChannelName("");
      setErrorMessage("");
    } catch (err) {
      console.error("Error joining channel:", err);
      setErrorMessage("Failed to join channel.");
    }
  };
  return (<>
    {/* Button to join channel, visible to everyone */}
    {<li className="channel-button" key={-1} style={{paddingLeft: "0rem"}}>
      <button className="new-button" onClick={() => setIsModalOpen(true)}>
          + Join a channel
      </button>
    </li>}

    {/* Modal */}
    {isModalOpen && (<div className="modal-overlay">
      <div className="modal-content">
        <h2>Join an existing Channel</h2>

        <input
          type="text"
          placeholder="Enter channel name"
          value={channelName}
          onChange={(e) => setChannelName(e.target.value)}
        />

        {errorMessage && <p style={{color: "red"}}>{errorMessage}</p>}

        <div className="modal-buttons">
          <button onClick={handleJoinChannel} className="create-btn">
                Join Channel
          </button>
          <button
            onClick={() => setIsModalOpen(false)}
            className="cancel-btn"
          >
                Cancel
          </button>
        </div>
      </div>
    </div>)}
  </>);
}

function NewDMButton({loggedUser}) {
  const navigate = useNavigate();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [genUsers, setGenUsers] = useState([]);

  const fetchGeneralData = async () => {
    try {
      const response = await axios.get("http://localhost:8080/api/channel/General", {withCredentials: true});
      setGenUsers(response.data.users);
      console.log("Updated user list:", response.data.users);
    } catch (err) {
      console.error("Error fetching channel data:", err);
    }
  };

  const createNewDM = async ({loggedUser, userUsername}) => {
    console.log("loggedUser", loggedUser);
    console.log("Creating DM with:", userUsername);
    if (loggedUser === userUsername) {
      console.log("Tried to create DM with self");
      alert("You cannot create a DM with yourself.");
      return;
    }
    const dmChannelName = loggedUser < userUsername ? `${loggedUser}_${userUsername}` : `${userUsername}_${loggedUser}`;
    try {
      await axios.post("http://localhost:8080/api/channel/create-dm-channel", {
        user1: loggedUser, user2: userUsername, channelName: dmChannelName,
      }, {withCredentials: true});
      navigate(`/channel/${dmChannelName}`);
      setIsModalOpen(false); // Close the modal after creating DM
    } catch (err) {
      console.error("Error creating DM channel:", err);
    }
  };
  return (<>
    {/* Button to create DM */}
    <li className="channel-button" key={-1} style={{paddingLeft: "0rem"}}>
      <button
        className="new-button"
        onClick={() => {
          fetchGeneralData();
          setIsModalOpen(true);
        }}
      >
          + Create DM
      </button>
    </li>

    {/* Modal */}
    {isModalOpen && (<div className="modal-overlay">
      <div className="modal-content">
        <h2>Select A User</h2>
        <ul className="member-list">
          {genUsers
            .filter((user) => user.username !== loggedUser)
            .map((user, index) => (<UserDMButton
              key={index}
              user={user}
              index={index}
              loggedUser={loggedUser}
              createNewDM={createNewDM}
            />))}
        </ul>
        <button onClick={() => setIsModalOpen(false)}>Close</button>
      </div>
    </div>)}
  </>);
}

function UserDMButton({user, index, loggedUser, createNewDM}) {
  return (<li key={index}>
    <button
      className="user-dm-button"
      onClick={() => createNewDM({loggedUser, userUsername: user.username})}
    >
      {user.username}
    </button>
  </li>);
}

function Channel({
  messages,
  newMessage,
  setNewMessage,
  handleSendMessage,
  handleDeleteMessage,
  isAdmin,
  channelName,
  lastMessageID,
  setLastMessageID,
  updateLastSeenMessage,
  setNotifChannels,
}) {
  return (<div className="channel">
    <ChannelLogo channelName={channelName}/>
    <Messages
      messages={messages}
      channelName={channelName}
      handleDeleteMessage={handleDeleteMessage}
      isAdmin={isAdmin}
      lastMessageID={lastMessageID}
      setLastMessageID={setLastMessageID}
      updateLastSeenMessage={updateLastSeenMessage}
      setNotifChannels={setNotifChannels}
    />
    <InputBox
      newMessage={newMessage}
      setNewMessage={setNewMessage}
      handleSendMessage={handleSendMessage}
    />
  </div>);
}

function ChannelLogo({channelName}) {
  return (<div className="channel-logo">
    <h1>{channelName}</h1>
  </div>);
}

function Messages({
  messages,
  channelName,
  handleDeleteMessage,
  isAdmin,
  lastMessageID,
  setLastMessageID,
  updateLastSeenMessage,
  setNotifChannels,
}) {
  const messagesEndRef = React.useRef(null);

  React.useEffect(() => {
    messagesEndRef.current?.scrollIntoView({behavior: "smooth"});
  }, [channelName]);

  const handleMessageClick = () => {
    if (messages.length === 0) {
      return;
    }
    // should be the latest message posted on the current channel
    const latestMsgID = messages[messages.length - 1].id;
    if (lastMessageID !== latestMsgID) {
      setLastMessageID(latestMsgID);
      console.log("last seen message(handleclick) " + latestMsgID);
      updateLastSeenMessage(latestMsgID);
    }
    // Remove notification when a message is clicked
    //pass setNotifChannel to Messages to work
    setNotifChannels((prevNotifChannel) => {
      const updatedNotifChannel = new Set(prevNotifChannel);
      updatedNotifChannel.delete(channelName);
      return updatedNotifChannel;
    });
  };
  return (<div className="message-container" onClick={handleMessageClick}>
    {messages.map((msg, index) => (<div key={msg.id}>
      <Message
        key={msg.id}
        id={msg.id}
        sender={msg.sender}
        content={msg.content}
        time={msg.date_time}
        handleDeleteMessage={handleDeleteMessage}
        isAdmin={isAdmin}
      />
      {msg.id === lastMessageID && !(index === messages.length - 1) && (
        <hr style={{borderColor: "indianred", borderWidth: "1px"}}/>)}
    </div>))}
    <div ref={messagesEndRef}/>
  </div>);
}

function Message(props) {
  const [isHovered, setIsHovered] = useState(false); //Use to check if it is being hovered
  return (<div
    className="message"
    onMouseEnter={() => setIsHovered(true)}
    onMouseLeave={() => setIsHovered(false)}
  >
    <div className="message-header">
      <strong>
        {props.sender.username}{" "}
        {props.sender.role === "ADMIN" ? "(Admin)" : ""}
      </strong>
      {// This button only appear when hovered and the current user is an admin
        isHovered && props.isAdmin && (<DeleteMessage
          id={props.id}
          handleDeleteMessage={props.handleDeleteMessage}
        />)}
    </div>
    {props.content}
  </div>);
}

function DeleteMessage({id, handleDeleteMessage}) {
  return (<div>
    <button
      className="message-delete-button"
      onClick={() => handleDeleteMessage(id)} // Call delete function
    >
        Delete
    </button>
  </div>);
}

function InputBox({newMessage, setNewMessage, handleSendMessage}) {
  return (<div className="input-box">
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
  </div>);
}

function Members({channelName, users, isAdmin, loggedUser, setIsAdmin}) {
  const [active, setActive] = React.useState(false);

  const changeUserRole = async (targetUsername, newRole) => {
    try {
      await axios.put("http://localhost:8080/api/admin/updateRole", null, {
        params: {
          currentUsername: loggedUser, targetUsername: targetUsername, newRole: newRole,
        }, withCredentials: true,
      });
    } catch (err) {
      console.error("Error changing user role:", err);
      alert("Failed to change user role.");
    }
  };

  return (<div className={active ? "members" : "members members-inactive"}>
    <MembersLogo active={active} setActive={setActive}/>
    <div className="members-wrapper">
      <MemberList
        active={active}
        activeChannel={channelName}
        users={users}
        loggedUser={loggedUser}
        isAdmin={isAdmin}
        changeUserRole={changeUserRole}
        setIsAdmin={setIsAdmin}
      />
      <UserSearch active={active} loggedUser={loggedUser} users={users}/>
    </div>
  </div>);
}

function MembersLogo({active, setActive}) {
  function toggleActive() {
    setActive(!active);
  }

  return (<div className="members-logo">
    <img
      className={active ? "members-logo-img" : "members-logo-img members-logo-img-inactive"}
      src={require("../styles/members-icon.png")}
      alt="Members Icon"
      onClick={toggleActive}
    />
    <p
      className={active ? "members-logo-txt" : "members-logo-txt members-logo-txt-inactive"}
    >
        MEMBERS
    </p>
  </div>);
}

function MemberList({
  active, users, loggedUser, isAdmin, changeUserRole, activeChannel, setIsAdmin,
}) {
  return (<ul className="member-list">
    {users.map((member, index) => (<MemberButton
      active={active}
      key={index}
      member={member}
      loggedUser={loggedUser}
      isAdmin={isAdmin}
      changeUserRole={changeUserRole}
      activeChannel={activeChannel}
      setIsAdmin={setIsAdmin}
    />))}
  </ul>);
}

function MemberButton({
  active, member, isAdmin, changeUserRole, activeChannel,
}) {
  const [adminsCount, setAdminsCount] = useState(0);
  const [isAdminRole, setIsAdminRole] = useState(member.role === "ADMIN");

  useEffect(() => {
    setIsAdminRole(member.role === "ADMIN");
  }, [member.role]); // Reacts to changes in `member.role`

  // Get the number of admins in the channel
  const fetchAdminsCount = async () => {
    try {
      const response = await fetch(`http://localhost:8080/api/channel/admins-count/${activeChannel}`);
      const data = await response.json();
      setAdminsCount(data.adminsCount); // Update the state with fetched value
      console.log(adminsCount);
    } catch (error) {
      console.error("Error fetching admins count:", error);
    }
  };

  // Call fetchAdminsCount to get the initial admin count
  useEffect(() => {
    fetchAdminsCount();
  }, [activeChannel]); // Re-fetch if the channel name changes

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
      await changeUserRole(member.username, newRole);

      // Get the updated admin count again
      await fetchAdminsCount();

      setIsAdminRole(!isAdminRole); // Toggle the role state immediately
    }
  };

  return (<li
    className={active ? "member-button" : "member-button member-button-inactive"}
  >
    <div className="member-info">
      <span className="username">{member.username}</span>
      {/* Show the toggle only if the current user is an admin */}
      {isAdmin && (<div className="role-toggle-container">
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
      </div>)}
    </div>
  </li>);
}

function UserSearch({active, loggedUser}) {
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState([]);

  const navigate = useNavigate();

  const NavChannel = async ({loggedUser, userUsername}) => {
    //if (!message.trim()) return;
    console.log("navigate to DM with ", userUsername);
    const dmChannelName = loggedUser < userUsername ? `${loggedUser}_${userUsername}` : `${userUsername}_${loggedUser}`;
    try {
      await axios.get(`http://localhost:8080/api/channel/${dmChannelName}`, {
        withCredentials: true,
      });
    } catch (err) {
      //if 404(DmChannel doesnt exist) create it
      if (err.response && err.response.status === 404) {
        try {
          await axios.post("http://localhost:8080/api/channel/create-dm-channel", {
            user1: loggedUser, user2: userUsername, channelName: dmChannelName,
          }, {withCredentials: true});
        } catch (err) {
          console.error("Error creating DM channel", err);
        }
      } else {
        console.error("Error checking the channel", err);
      }
    }
    navigate(`/channel/${dmChannelName}`);
  };
  const handleSearch = async () => {
    try {
      const response = await axios.get(`http://localhost:8080/api/channel/users/search?query=${searchQuery}`, {
        withCredentials: true,
      });
      setSearchResults(response.data);
    } catch (err) {
      console.error("No user found: ", err);
      setSearchResults([]);
    }
  };

  return (<div
    className={active ? "user-search" : "user-search user-search-inactive"}
  >
    <input
      type="text"
      placeholder="Find user..."
      value={searchQuery}
      onChange={(e) => setSearchQuery(e.target.value)}
    />
    <button onClick={handleSearch}>Search</button>
    <ul className="search-results">
      {searchResults.map((user) => (<div
        className="search-result"
        key={user.username}
        onClick={() => {
          if (loggedUser !== user.username) {
            NavChannel({loggedUser, userUsername: user.username});
          } else {
            console.log("tried talking to myself");
          }
        }}
      >
        {user.username}
      </div>))}
    </ul>
  </div>);
}

export default ChannelPage;
