CREATE TABLE users (
                       username VARCHAR(255) PRIMARY KEY,
                       password VARCHAR(255) NOT NULL ,
                       role VARCHAR(50) CHECK ( role IN ('ADMIN', 'MEMBER') ) NOT NULL
);

CREATE TABLE channels(
                        name VARCHAR(255) PRIMARY KEY,
                        type VARCHAR(50) CHECK ( type IN ('DM', 'PC') ) NOT NULL
);

CREATE TABLE messages (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(255) NOT NULL,
                        text VARCHAR(255) NOT NULL,
                        date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                        channel_name VARCHAR(255) NOT NULL,
                        FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
                        FOREIGN KEY (channel_name) REFERENCES channels(name) ON DELETE CASCADE
);

CREATE TABLE channel_users (
                        username VARCHAR(255) NOT NULL,
                        channel_name VARCHAR(255) NOT NULL,
                        PRIMARY KEY (username, channel_name),
                        FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
                        FOREIGN KEY (channel_name) REFERENCES channels(name) ON DELETE CASCADE
);