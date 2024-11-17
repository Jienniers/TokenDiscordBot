package org.tokenbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.tokenbot.Main.dotenv;

public class Events extends ListenerAdapter {
    List<Long> ticketChannels = new ArrayList<>();

    private Role pingRole;
    private Role userRole;

    private String JDBC_URL = dotenv.get("JDBC_URL");
    private String USERNAME = dotenv.get("SQL_Username");
    private String PASSWORD = dotenv.get("SQL_Password");


    private Properties config(){
        Properties prop = new Properties();
        String fileName = "bot.config";
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
        } catch (IOException ex) {
        }
        return prop;
    }


    @Override
    public void onReady(ReadyEvent e){
        if (e instanceof ReadyEvent){
            TextChannel channel = e.getJDA().getTextChannelById(config().getProperty("bot.TicketChannelID"));
            if (channel != null) {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("Create a new Ticket!")
                        .setDescription("Chat with the staff by opening a new ticket!");
                pingRole = e.getJDA().getRoleById(config().getProperty("bot.PingRoleID"));
                userRole = e.getJDA().getRoleById(config().getProperty("bot.UserRoleID"));
                if (pingRole == null || userRole == null ){
                    System.out.println("Roles are not found! This will produce future errors.");
                }
                channel.sendMessageEmbeds(embedBuilder.build()).setActionRow(Button.primary("OpenTicket", "Open Ticket!")).queue();

            } else {
                System.err.println("TextChannel not found. Make sure the provided channel ID is correct.");
            }
            System.out.println("Successfully Online!");
        }
    }

    @Override
    public void onGuildReady(GuildReadyEvent e){
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("add", "Ads the user to the ticket").addOption(OptionType.USER, "adduser", "adding user to the role"));
        commandData.add(Commands.slash("remove", "Removes the user to the ticket").addOption(OptionType.USER, "removeuser", "removing user to the role"));
        commandData.add(Commands.slash("rename", "Renames the ticket channel").addOption(OptionType.STRING, "newname", "renaming the ticket channel"));
        commandData.add(Commands.slash("close", "Closes the ticket"));

        e.getGuild().updateCommands().addCommands(commandData).complete();

    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        switch (event.getComponentId()) {
            case "OpenTicket":
                TextChannel newChannel = event.getGuild().createTextChannel(event.getUser().getName()).complete();
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("Thanks for opening a channel")
                        .setDescription("Your Ticket will be managed by someone in a few mins!");
                newChannel.sendMessageEmbeds(embedBuilder.build()).setActionRow(Button.danger("CloseTicket", "Close")).queue();
                newChannel.sendMessage("<@&" + pingRole.getId() + ">").queue();
                ticketChannels.add(Long.valueOf(newChannel.getId()));
                LocalDateTime currentTime = LocalDateTime.now();
                storeDatainDatabase(event, newChannel, currentTime);
                String messageContent = newChannel.getAsMention() +" Ticket Has been successfully created!";
                Message sentMessage = event.getChannel().sendMessage(messageContent).complete();
                ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                executorService.schedule(() -> {
                    sentMessage.delete().queue();
                    executorService.shutdown();
                }, 5, TimeUnit.SECONDS);

                if (userRole != null && newChannel != null) {
                    newChannel.upsertPermissionOverride(newChannel.getGuild().getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
                    newChannel.getManager().putPermissionOverride(userRole,  EnumSet.of(Permission.VIEW_CHANNEL), null).queue();
                }
                break;
            case "CloseTicket":
                ticketChannels.remove(event.getChannel().getId());
                String insertQuery = "Update DiscordTicketData SET TicketState = (?) Where TicketChannelID = (?);";
                try (
                        Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
                        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)
                ) {
                    preparedStatement.setBoolean(1, false);
                    preparedStatement.setString(2, event.getChannel().getId());
                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Data inserted successfully!");
                    } else {
                        System.out.println("Failed to insert data.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                event.getChannel().delete().queue();
                break;
        }
    }

    private void storeDatainDatabase(ButtonInteractionEvent event, TextChannel newchannel, LocalDateTime currenttime){
        String insertQuery = "INSERT INTO DiscordTicketData (GuildID, TicketChannelID, TicketCreatorID, TimeCreated, TicketState) VALUES (?, ?, ?, ?, ?)";
        try (
                Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
                PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)
        ) {
            preparedStatement.setLong(1, Long.parseLong(event.getGuild().getId()));
            preparedStatement.setLong(2, Long.parseLong(newchannel.getId()));
            preparedStatement.setLong(3, Long.parseLong(event.getUser().getId()));
            preparedStatement.setObject(4, currenttime);
            preparedStatement.setBoolean(5, true);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data inserted successfully!");
            } else {
                System.out.println("Failed to insert data.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        long channelId = Long.parseLong(event.getChannel().getId());
        if (ticketChannels.contains(channelId)) {
            System.out.println("Channel ID is in the list.");
            switch (event.getName()){
                case "add":
                    User addUser = event.getOption("adduser").getAsUser();
                    event.getGuild().addRoleToMember(addUser, userRole).queue();
                    event.reply("<@"+addUser.getId()+"> Has Been Added to the Role!").queue();
                    break;
                case "remove":
                    User removeUser = event.getOption("removeuser").getAsUser();
                    event.getGuild().removeRoleFromMember(removeUser, userRole).queue();
                    event.reply("<@"+removeUser.getId()+"> Has Been Removed from the Role!").queue();
                    break;
                case "rename":
                    Channel channel = event.getChannel();
                    String newName = event.getOption("newname").getAsString();
                    if (channel instanceof TextChannel) {
                        TextChannel textChannel = (TextChannel) channel;
                        textChannel.getManager().setName(newName).queue();
                        event.reply("Channel name has been successfully changed to "+newName).queue();
                    }
                    break;
                case "close":
                    ticketChannels.remove(event.getChannel().getId());
                    String insertQuery = "Update DiscordTicketData SET TicketState = (?) Where TicketChannelID = (?);";
                    try (
                            Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
                            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)
                    ) {
                        preparedStatement.setBoolean(1, false);
                        preparedStatement.setString(2, event.getChannel().getId());
                        int rowsAffected = preparedStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("Data inserted successfully!");
                        } else {
                            System.out.println("Failed to insert data.");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    event.getChannel().delete().queue();
                    break;
            }
        } else {
            String messageContent = "This is not a ticket channel!";
            Message sentMessage = event.getChannel().sendMessage(messageContent).complete();
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.schedule(() -> {
                sentMessage.delete().queue();
                executorService.shutdown();
            }, 5, TimeUnit.SECONDS);
            System.out.println("Channel ID is not in the list.");
        }
    }
}
