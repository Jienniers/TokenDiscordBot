package org.tokenbot;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.lang.*;
public class Main {
    static Dotenv dotenv = Dotenv.load();
    static String token = dotenv.get("TOKEN");

    public static void main(String[] args) {
        JDABuilder jdaBuilder = JDABuilder.createDefault(token);
        jdaBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES);
        jdaBuilder.addEventListeners(new Events());
        jdaBuilder.build();
    }
}