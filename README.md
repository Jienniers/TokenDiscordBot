
# Discord Token Bot

A Discord Bot for creating ticket channels inside discord severs.
## Requirements
you must created a `.env` file with the following variables.
```
TOKEN="Your Discord Bot Token"
JDBC_URL="Your JDBC URL"
SQL_Username = "Your SQL Username"
SQL_Password = "Your SQL Password"

```

And you must have SQL installed and running. You can download mysql Community from [here](@https://dev.mysql.com/downloads/).

# Config
you can edit all the configurations in ``bot.config``.\
\
```bot.TicketChannelID``` this is your Channel id where the ticket embed and button would be sent.\
\
``bot.UserRoleID`` is the User ID of the user who's allowed to view the tickets and access them when they are first created and, is usually admin.\
\
``bot.PingRoleID`` is the role ID who would be pinged after the ticket has been created.

``sql.Table`` is the SQL table which would be used create the SQL table by the following command:
```
Create table discordticketdata(
	GuildID long,
    TicketChannelID long,
    TicketCreatorID long,
    TimeCreated datetime,
    TicketState Varchar(50)
)
```
### SQL Data Information

| Column | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `GuildID` | `long` | Is the ID of the server in which the ticket was created. |
| `TicketChannelID` | `long` | Is the ID of the ticket channel |
| `TicketCreatorID` | `long` | Is the ID of the user who created the ticket |
| `TimeCreated` | `datetime` | Is the Time when the ticket was created. |
| `TicketState` | `varchar(50)` | Indicates the state of the ticket. For example when its ``1`` it means that the ticket is open and when its ``0`` it means that the ticket is closed. |


# Commands

**Note: These commands should only be runned inside the ticket channels, Which were created by this bot.**

| Column | Parameter     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `/add` | `user` | Adds the parameter user to view and access the current ticket channel. |
| `/close` |  | Closes the current ticket channel and deletes it. |
| `/remove` | `user` | Removes the the parameter user from the ticket channel, which makes it unable for him to view or access the current ticket channel. |
| `/rename` | `string` | Renames the current ticket channel to the paramter string name. |


## Support

If you encounter any issues or have suggestions for improvement, please submit an issue on the GitHub repository.