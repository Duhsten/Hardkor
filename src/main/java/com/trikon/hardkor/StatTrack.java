package com.trikon.hardkor;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;

public class StatTrack {
	private static StatTrack instance;
	private List<WebStats> stats;

	public static StatTrack getInstance() {
		if (instance == null) {
			return instance = new StatTrack();
		} else
			return instance;
	}

	public static int lastXP = 0;

	public WebStats updateExp(int exp, WebStats self) {

		if (self.exp != exp) {
			self.exp = exp;
			StatTrack.sendUpdatedExp(self.mcuser, exp);
		}
		return self;

	}

	public WebStats updateMobKills(int kills, WebStats self) {

		if (self.mob_kills != kills) {
			self.mob_kills = kills;
			StatTrack.sendUpdatedMobKills(self.mcuser, kills);
		}
		return self;

	}
	
	public WebStats updatePlayerKills(int kills, WebStats self) {

		if (self.player_kills != kills) {
			self.player_kills = kills;
			StatTrack.sendUpdatedPlayerKills(self.mcuser, kills);
		}
		return self;

	}
	public boolean isPlayerAlive(String username) {
		updatePlayers();
		for(WebStats s : stats) {
			if(s.mcuser.equals(username)) {
				if(s.alive == 1) {
					return true;
				}
				else {
					return false;
				}
			}
		}
		return false;
	}
	public void updateAliveStatus(String username, boolean s) {
		try {
			int r = 0;
			if (s) {
				r = 1;
			}
			sendUpdate("https://student2.cs.appstate.edu/osbornedk1/hardkor/upleaderboard.php?mcusername=" + username
					+ "&alive=" + r);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void sendUpdatedExp(String mcuser, int exp) {

		try {
			sendUpdate("https://student2.cs.appstate.edu/osbornedk1/hardkor/upleaderboard.php?mcusername=" + mcuser
					+ "&exp=" + exp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void sendUpdatedMobKills(String mcuser, int kills) {

		try {
			sendUpdate("https://student2.cs.appstate.edu/osbornedk1/hardkor/upleaderboard.php?mcusername=" + mcuser
					+ "&mobkills=" + kills);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void sendUpdatedPlayerKills(String mcuser, int kills) {

		try {
			sendUpdate("https://student2.cs.appstate.edu/osbornedk1/hardkor/upleaderboard.php?mcusername=" + mcuser
					+ "&playerkills=" + kills);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updatePlayers() {
		if (stats == null) {
			Gson gson = new Gson();
			try {
				stats = gson.fromJson(
						sendUpdate("https://student2.cs.appstate.edu/osbornedk1/hardkor/leaderboard.json"),
						new TypeToken<List<WebStats>>() {
						}.getType());
			} catch (JsonSyntaxException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		List<ServerPlayer> players = Hardkor.getInstance().getServer().getPlayerList().getPlayers();
		for (ServerPlayer ply : players) {
			for (WebStats stat : stats) {
				if (stat.mcuser.equals(ply.getDisplayName().getString())) {
					int mobKills = ply.getStats().getValue(Stats.CUSTOM, Stats.MOB_KILLS);
					int playerKills = ply.getStats().getValue(Stats.CUSTOM, Stats.PLAYER_KILLS);
					updateMobKills(mobKills, stat);
					updatePlayerKills(playerKills, stat);
					updateExp(ply.totalExperience, stat);
				}
			}

		}
	}

	public static String sendUpdate(String requestURL) throws IOException {
		try (Scanner scanner = new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8.toString())) {
			scanner.useDelimiter("\\A");
			return scanner.hasNext() ? scanner.next() : "";
		}
	}
}