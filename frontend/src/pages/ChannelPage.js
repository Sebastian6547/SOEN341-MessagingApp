import React, { useState, useEffect, useRef } from "react";
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
    const [loggedInUser, setLoggedInUser] = useState("");
    const [lastMessageTimeStamp, setLastMessageTimeStamp] = useState({});
    const [notifChannel, setNotifChannels] = useState(new Set());

    const getLoggedInUser = async () => {
        try {
            const response = await axios.get("http://localhost:8080/api/auth/check", {
                withCredentials: true,
            });
            setLoggedInUser(response.data.username);
        } catch (error) {
            console.error("Error fetching logged-in user:", error);
        }
    };

  useEffect(() => {
    // Get all channel data from the backend when the channel changes
    getChannelData();
    getLoggedInUser();
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
      //console.log(response.data, "channelName: " + channelName);
        const latestTime = response.data.messages.length > 0
            ? response.data.messages[response.data.messages.length - 1].timestamp
            : null;
      //take current loaded channel mark most recent message as seen
        console.log("Last message on this channel was sent at ", latestTime);
      lastSeenMessage(rawChannelName,latestTime);
      //Update
        console.log("fetching all channels for new messages");
      checkNewMessage();
    } catch (err) {
      console.error("Error fetching channel data:", err);
      if (err.response && err.response.status === 403) {
        alert("You are not a member of this channel.");
        navigate("/"); // Redirect to home if unauthorized
      }
    }
  };

  //return more recent message in channel
  const getLatestMessages= async(channelName) =>{
          try {
              const response = await axios.get(`http://localhost:8080/api/channel/${channelName}/latest`,
                  {withCredentials: true}
              );

              return response.data;
          }catch(err){
              console.error("Error getting latest messages",err);
              return null;
          }
  }
//compare most recent message in every channel with last viewed message in every channel.
  const checkNewMessage = async() =>{
      //create a copy so we render once
      let updatedNotifChannels = new Set(notifChannel);
      for(const channel of channels){

          console.log(" checking channel", channel.name);
          const newMessage = await getLatestMessages(channel.name);
          console.log(" newest message sent at ", newMessage.timestamp);
          console.log(" last seen message sent at ", lastMessageTimeStamp[channel.name]);
            //put in the list of channels that require a notification
          if(newMessage.timestamp > lastMessageTimeStamp[channel.name]){
              console.log("giving channel ", channel.name, " with a notification");
              updatedNotifChannels.add(channel.name);
          }
          else{
              //console.log("removing notification from ", channel.name, " message was seen");
              updatedNotifChannels.delete(channel.name);
          }
          setNotifChannels(updatedNotifChannels);
      }

    }
    const lastSeenMessage = (channelName, timeStamp) => {
        setLastMessageTimeStamp((prev)=>({
            ...prev, [channelName]:timeStamp
        }));
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
        notifChannel = {notifChannel}
      />
      <Channel
        messages={messages}
        newMessage={newMessage}
        setNewMessage={setNewMessage}
        handleSendMessage={handleSendMessage}
        channelName={channelName}
      />
      <Members channelName={channelName} users={users} loggedInUser={loggedInUser}/>

    </div>
  );
};

function Channels({ channelName, channels, getChannelData, notifChannel }) {
  return (
    <div className="channels">
      <ChannelsLogo />
      <ChannelList
        getChannelData={getChannelData}
        channelName={channelName}
        channels={channels}
        notifChannel = {notifChannel}
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

function ChannelList({ channelName, channels, getChannelData ,notifChannel}) {
  return (
    <ul className="channel-list">
      {channels.map((channel, index) => (
        <ChannelButton
          channelName={channelName}
          getChannelData={getChannelData}
          channelKey={index}
          channel={channel}
          notifChannel  = {notifChannel.has(channel.name)}
        />
      ))}
      <NewChannelButton />
    </ul>
  );
}

function ChannelButton({ channelName, getChannelData, channelKey, channel, notifChannel }) {
  const navigate = useNavigate();
  //console.log(notifChannel);
  return (
    <li
      className={`
        channel-button
        ${channelName === channel.name.replace(/_/g, " ") ? "channel-button-active" : ""}
        ${notifChannel ? "channel-button-notified" : ""} 
      `}
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
            `Navigating to channel: ${channel.name} (from channelName: ${channelName})`
          );
          navigate(`/channel/${channel.name}`);
        }}
      >
        {channel.name.replace(/_/g, " ")}
        {/* Replace underscores with spaces */}
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
        {props.sender.username} - {props.time}
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

function Members({ channelName, users, loggedInUser }) {
  const [active, setActive] = React.useState(false);
  return (
    <div className={active ? "members" : "members members-inactive"}>
      <MembersLogo active={active} setActive={setActive} />
      <MemberList active={active} activeChannel={channelName} users={users} loggedInUser={loggedInUser}/>
       {active && <UserSearch loggedInUser={loggedInUser} />}
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

function MemberList({ active, users, loggedInUser }) {
  return (
    <ul className="member-list">
      {users.map((member, index) => (
        <MemberButton active={active} key={index} member={member} loggedInUser={loggedInUser}/>
      ))}
    </ul>
  );
}

function MemberButton({active,member,loggedInUser}) {
    const [DmOpen, setDmOpen] = useState(false);
    const [message, setMessage] = useState("");
    const messageWindow = useRef(null);
    const navigate = useNavigate();

    const handleClick = () => {
        setDmOpen(true);
    };
    const handleOutsideClick = (event) => {
        if(messageWindow.current && !messageWindow.current.contains(event.target)){
            setDmOpen(false);
        }
    };
    useEffect(()=> {
        if(DmOpen){
            document.addEventListener("mousedown", handleOutsideClick);
        }  else{
            document.removeEventListener("mousedown", handleOutsideClick);
        }
        return()=> document.removeEventListener("mousedown", handleOutsideClick);
    }, [DmOpen]);

    const handleSendMessage = async () => {
        if(!message.trim()) return;
        const dmChannelName = loggedInUser < member.username
            ? `${loggedInUser}_${member.username}`
            : `${member.username}_${loggedInUser}`;
        try{
            await axios.get(`http://localhost:8080/api/channel/${dmChannelName}`,
                {withCredentials: true
                });
        } catch(err){
            //if 404(DmChannel doesnt exist) create it
            if(err.response && err.response.status === 404) {
                try{
                    await axios.post(`http://localhost:8080/api/channel/create-dm-channel`,
                        {user1: loggedInUser, user2: member.username, channelName: dmChannelName},
                        {withCredentials: true}
                    );
                } catch(err){
                    console.error("Error creating DM channel" , err);
                    return;
                }
            }
            else{
                console.error("Error checking the channel" ,  err);
                return;
                }

            }

            try {
                await axios.post(
                    `http://localhost:8080/api/channel/${dmChannelName}/sendMessage`, {
                    content: message
                },{withCredentials:true});
                setMessage(""); // Clear the input after sending
                setDmOpen(false);
                navigate(`/channel/${dmChannelName}`);
            }
            catch(err) {
                console.error("Error sending the message" , err);
        }

    };
// const handleOLDCLICK = async () => {
    //     if (!loggedInUser || !member.username){
    //         console.error("logged in user error", loggedInUser, " and member" , member.username);
    //         return;
    //     }
    //     console.error("clicked on user", member.username);
    //     const dmChannelName = loggedInUser < member.username
    //         ? `${loggedInUser}_${member.username}`
    //         : `${member.username}_${loggedInUser}`;
    //     try {
    //         const response = await axios.get(`http://localhost:8080/api/channel/${dmChannelName}`, {
    //             withCredentials: true,
    //         });
    //
    //         if (response.status === 200) {
    //             console.log("DM channel exists, navigating...");
    //             navigate(`/channel/${dmChannelName}`);
    //         }
    //     } catch (err) {
    //         console.log("tried to connect to DM");
    //         if (err.response && err.response.status === 404) {
    //             console.log("DM channel does not exist, creating one...");
    //             try {
    //                 await axios.post(
    //                     `http://localhost:8080/api/channel/create-dm-channel`,
    //                     { user1: loggedInUser, user2: member.username , channelName: dmChannelName},
    //                     { withCredentials: true }
    //                 );
    //
    //                 console.log("DM channel created, navigating...");
    //                 navigate(`/channel/${dmChannelName}`);
    //             } catch (createErr) {
    //                 console.error("Error creating DM channel:", createErr);
    //             }
    //         } else {
    //             console.error("Error failed to catch", err);
    //         }
    //     }
    // };

    // No profile pictures are planned, but we can think about adding like a set of default pp to choose from
    return (
        <li className={active ? "member-button" : "member-button member-button-inactive"}>
            <button className="button" onClick={handleClick}>
                {member.username}
            </button>
            {DmOpen && (
                <div className="messageWindow" ref={messageWindow}>
                    <div className="messageWindow-header">

                        <img src={require("../styles/members-icon.png")} alt={member.username} className="user-icon" />
                        <span>{member.username}</span>
                    </div>
                    <input
                        type="text"
                        placeholder="Send a message..."
                        value={message}
                        onChange={(e) => setMessage(e.target.value)}
                    />
                    <button onClick={handleSendMessage}>Send</button>
                </div>
            )}
        </li>

        // <li
        //   className={
        //     active ? "member-button" : "member-button member-button-inactive"
        //   }
        //
        // >
        //   <button
        //     className="button"
        //     style={{
        //       borderRadius: "50px",
        //       paddingLeft: "0.5rem",
        //     }}
        //     onClick={handleClick}
        //   >
        //     {member.username}
        //   </button>
        // </li>
    );
}
function UserSearch(active, loggedInUser){
    const [searchQuery, setSearchQuery] = useState("");
    const [searchResults, setSearchResults] = useState([]);

    const handleSearch = async () =>{

        try{
            const response = await axios.get(`http://localhost:8080/api/channel/users/search?query=${searchQuery}`,
                {withCredentials: true,
                });
            setSearchResults(response.data);
        }catch(err){
            console.error("No user found: ", err);
            setSearchResults([]);
        }
    };

    if(!active){
        console.log("Search is hidden")
        return null;
    }

    return (
        <div className={active ? "user-search" : "user-search user-search-inactive"}>
            <input
                type="text"
                placeholder="Find user..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
            />
            <button onClick={handleSearch}>Search</button>
            <ul>
                {searchResults.map((user) => (
                    <MemberButton active={active} key={user.username} member={user} loggedInUser={loggedInUser.loggedInUser}/>
                ))}
            </ul>
        </div>
    );
}




export default ChannelPage;
