package com.trikon.hardkor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import com.google.gson.Gson;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Hardkor {
    private MinecraftServer server;
    private static Hardkor instance;
    private static final String[] LOW_HEALTH_MESSAGES = new String[]{"is close to death!", "might die in a sec...", "is not really good at the game and is about to die", "has very low health"};
    private static final String[] DEATH_MESSAGES = new String[]{"has died...", "is dead..."};

    private List<String> lowHealthPlayers;

    public static Hardkor getInstance() {

        return instance;
    }

    public Hardkor(MinecraftServer server) {
        this.server = server;
        this.instance = this;
        this.lowHealthPlayers = new ArrayList();
        server.addTickable(new Runner());
    }

    public MinecraftServer getServer() {
        return server;
    }

    public void checkIfWhitelisted(Player player) throws IOException {
        Gson gson = new Gson();
        URL url = new URL("https://student2.cs.appstate.edu/osbornedk1/hardkor/users.json");
        //Retrieving the contents of the specified page
        Scanner sc = new Scanner(url.openStream());
        //Instantiating the StringBuffer class to hold the result
        StringBuffer sb = new StringBuffer();
        while (sc.hasNext()) {
            sb.append(sc.next());
            //System.out.println(sc.next());
        }
        //Retrieving the String from the String Buffer object
        String result = sb.toString();
        sc.close();
        List<String> users = gson.fromJson(result, ArrayList.class);
        boolean whitelisted = false;
        for (String s : users) {
            if (s.equals(player.getDisplayName().getString())) {
                whitelisted = true;
            }
        }

        if (!whitelisted) {
            TextComponent output = new TextComponent("");
            TextComponent action = new TextComponent("Kicked from Server\n\n");
            action.setStyle(Style.EMPTY.withColor(TextColor.parseColor("red")));
            TextComponent msg = new TextComponent("You are not registered!");
            msg.setStyle(Style.EMPTY.withColor(TextColor.parseColor("yellow")));
            output.append(action);
            output.append(msg);
            server.getPlayerList().getPlayerByName(player.getDisplayName().getString()).connection.disconnect(output);
        } else {
            server.getPlayerList().broadcastMessage(new TextComponent("Welcome!"), ChatType.GAME_INFO, player.getUUID());
        }


    }

    public void addLowHealthPlayer(String username) {
        lowHealthPlayers.add(username);
    }

    public void removeLowHealthPlayer(String username) {

        lowHealthPlayers.remove(username);


    }


    public boolean hasLowHealth(String username) {
        for (String s : lowHealthPlayers) {
            if (s.equals(username))
                return true;
        }
        return false;
    }

    public void trackPlayersHealth() {
        List<ServerPlayer> players = Hardkor.getInstance().getServer().getPlayerList().getPlayers();
        for (ServerPlayer ply : players) {
            float percent = ply.getHealth() / ply.getMaxHealth();
            if (percent <= .35f && !hasLowHealth(ply.getDisplayName().getString()) && StatTrack.getInstance().isPlayerAlive(ply.getDisplayName().getString())) {
                TextComponent output = new TextComponent("");
                TextComponent action = new TextComponent(ply.getDisplayName().getString());
                action.setStyle(Style.EMPTY.withColor(TextColor.parseColor("aqua")));
                Random rnd = new Random();
                TextComponent msg = new TextComponent(" " + LOW_HEALTH_MESSAGES[rnd.nextInt(0, LOW_HEALTH_MESSAGES.length - 1)]);
                msg.setStyle(Style.EMPTY.withColor(TextColor.parseColor("yellow")));
                output.append(action);
                output.append(msg);
                //server.getPlayerList().broadcastMessage(output, ChatType.GAME_INFO, ply.getUUID());
                server.getPlayerList().broadcastMessage(output, ChatType.SYSTEM, ply.getUUID());
                addLowHealthPlayer(ply.getDisplayName().getString());

            } else if (percent > .35f) {
                removeLowHealthPlayer(ply.getDisplayName().getString());
            }
            if (ply.getHealth() == 0f && StatTrack.getInstance().isPlayerAlive(ply.getDisplayName().getString())) {

                TextComponent output = new TextComponent("");
                TextComponent action = new TextComponent(ply.getDisplayName().getString());
                action.setStyle(Style.EMPTY.withColor(TextColor.parseColor("aqua")));
                Random rnd = new Random();
                TextComponent msg = new TextComponent(" " + DEATH_MESSAGES[rnd.nextInt(0, DEATH_MESSAGES.length - 1)]);
                msg.setStyle(Style.EMPTY.withColor(TextColor.parseColor("red")));
                output.append(action);
                output.append(msg);

                TextComponent output2 = new TextComponent("");
                TextComponent action2 = new TextComponent("You Died!\n\n");
                action2.setStyle(Style.EMPTY.withColor(TextColor.parseColor("red")));
                TextComponent msg2 = new TextComponent("You can join back if you like, \n but you will only be able to spectate.");
                msg2.setStyle(Style.EMPTY.withColor(TextColor.parseColor("yellow")));
                output2.append(action2);
                output2.append(msg2);
                removeLowHealthPlayer(ply.getDisplayName().getString());
                StatTrack.getInstance().updateAliveStatus(ply.getDisplayName().getString(), false);
                ply.respawn();
                server.getPlayerList().getPlayerByName(ply.getDisplayName().getString()).connection.disconnect(output2);
                return;

            }
        }
    }

}
