# 🤖 Discord Token Bot

A feature-rich Discord bot designed to streamline support through automated ticket creation and management within Discord servers.

> ⚠️ **Project Status: Not Actively Maintained**
>
> This project is no longer actively maintained. It remains public for learning, reference, or modification by others. Some parts of the code may be outdated or include minor bugs.

---

## 📦 Requirements

Before running the bot, ensure you have the following setup:

### 🔐 `.env` File

Create a `.env` file in the root directory with the following variables:

```env
TOKEN="Your Discord Bot Token"
JDBC_URL="Your JDBC URL"
SQL_Username="Your SQL Username"
SQL_Password="Your SQL Password"
```

> 💡 Make sure your SQL server is up and running. You can download MySQL Community Edition [here](https://dev.mysql.com/downloads/).

---

## ⚙️ Configuration

Edit the `bot.config` file to customize your bot:

* `bot.TicketChannelID` → Channel ID where the embed and ticket creation button will appear.
* `bot.UserRoleID` → Role ID allowed to view and manage tickets (usually an admin).
* `bot.PingRoleID` → Role ID to be pinged when a new ticket is created.
* `sql.Table` → Name of the SQL table where ticket data will be stored.

### 🗃️ SQL Table Schema

Use the following SQL command to create the required table:

```sql
CREATE TABLE discordticketdata (
  GuildID BIGINT,
  TicketChannelID BIGINT,
  TicketCreatorID BIGINT,
  TimeCreated DATETIME,
  TicketState VARCHAR(50)
);
```

#### 📊 Data Columns

| Column            | Type          | Description                                              |
| ----------------- | ------------- | -------------------------------------------------------- |
| `GuildID`         | `BIGINT`      | Discord server ID where the ticket was created           |
| `TicketChannelID` | `BIGINT`      | ID of the generated ticket channel                       |
| `TicketCreatorID` | `BIGINT`      | ID of the user who created the ticket                    |
| `TimeCreated`     | `DATETIME`    | Timestamp of when the ticket was created                 |
| `TicketState`     | `VARCHAR(50)` | State of the ticket (`1` for open, `0` for closed, etc.) |

---

## 💬 Bot Commands

**Note:** These commands should only be used within channels created by this bot.

| Command   | Parameters | Description                                                       |
| --------- | ---------- | ----------------------------------------------------------------- |
| `/add`    | `user`     | Grants the specified user access to the current ticket channel    |
| `/remove` | `user`     | Revokes the specified user's access to the current ticket channel |
| `/close`  | *(none)*   | Closes and deletes the current ticket channel                     |
| `/rename` | `string`   | Renames the current ticket channel to the provided string         |

---

## 🛠️ Developer Info

* **Author**: [Jienniers](https://github.com/Jienniers)
* **Repository**: [GitHub](https://github.com/Jienniers/TokenDiscordBot)

## 📜 License

This project is licensed under the [MIT License](LICENSE).

---

## 🙋‍♀️ Feedback & Contributions

Have suggestions or found a bug? Feel free to open an issue or submit a pull request!

