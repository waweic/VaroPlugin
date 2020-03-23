package de.cuuky.varo.spawns.sort;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.cuuky.varo.Main;
import de.cuuky.varo.configuration.configurations.config.ConfigSetting;
import de.cuuky.varo.configuration.configurations.messages.ConfigMessages;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.spawns.Spawn;

public class PlayerSort {
	
	public enum SortResult {
		NO_SPAWN,
		NO_SPAWN_WITH_TEAM,
		SORTED_WELL;
	}
	
	private int scheduler;
	private HashMap<Player, Location> toTeleport;
	public PlayerSort() {
		toTeleport = new HashMap<>();
	}
	
	public SortResult sortPlayers() {
		ArrayList<VaroPlayer> players = VaroPlayer.getOnlinePlayer();
		ArrayList<VaroPlayer> playersForIterator = VaroPlayer.getOnlinePlayer();
		ArrayList<Spawn> spawns = Spawn.getSpawnsClone();
		ArrayList<Spawn> spawnsForIterator = Spawn.getSpawns();

		for(VaroPlayer vp : playersForIterator) {
			if(!vp.getStats().isSpectator()) 
				continue;

			toTeleport.put(vp.getPlayer(), vp.getPlayer().getWorld().getSpawnLocation());
			vp.sendMessage(Main.getPrefix() + ConfigMessages.SORT_SPECTATOR_TELEPORT.getValue());
			players.remove(vp);
		}

		for(Spawn spawn : spawnsForIterator) {
			if(spawn.getPlayer() == null) {
				continue;
			} else if(!spawn.getPlayer().isOnline()) {
				continue;
			} else {
				spawn.getPlayer().cleanUpPlayer();
				toTeleport.put(spawn.getPlayer().getPlayer(), spawn.getLocation());
				spawn.getPlayer().sendMessage(Main.getPrefix() + ConfigMessages.SORT_OWN_HOLE.getValue());
				players.remove(spawn.getPlayer());
				spawns.remove(spawn);
			}
		}

		SortResult result = SortResult.SORTED_WELL;

		while(spawns.size() > 0) {
			if(players.size() <= 0) 
				break;

			VaroPlayer player = players.get(0);
			Spawn spawn = spawns.get(0);
			player.cleanUpPlayer();
			toTeleport.put(player.getPlayer(), spawn.getLocation());
			spawn.setPlayer(player);
			player.sendMessage(Main.getPrefix() + ConfigMessages.SORT_NUMBER_HOLE.getValue().replace("%number%", String.valueOf(spawn.getNumber())));
			players.remove(0);
			spawns.remove(0);

			if(player.getTeam() == null) 
				continue;

			int playerTeamRegistered = 1;
			for(VaroPlayer teamPlayer : player.getTeam().getMember()) {
				if(spawns.size() <= 0) {
					break;
				}

				if(ConfigSetting.TEAM_PLACE_SPAWN.getValueAsInt() > 0) {
					if(playerTeamRegistered < ConfigSetting.TEAM_PLACE_SPAWN.getValueAsInt()) {
						if(players.contains(teamPlayer)) {
							teamPlayer.cleanUpPlayer();
							toTeleport.put(teamPlayer.getPlayer(), spawns.get(0).getLocation());
							spawns.get(0).setPlayer(teamPlayer);
							teamPlayer.sendMessage(Main.getPrefix() + ConfigMessages.SORT_NUMBER_HOLE.getValue().replace("%number%", String.valueOf(spawns.get(0).getNumber())));
							players.remove(teamPlayer);
						}

						spawns.remove(0);
						playerTeamRegistered++;
					} else {
						result = SortResult.NO_SPAWN_WITH_TEAM;
						players.remove(teamPlayer);
						teamPlayer.sendMessage(Main.getPrefix() + ConfigMessages.SORT_NO_HOLE_FOUND_TEAM.getValue());
					}
				} else if(players.contains(teamPlayer)) {
					teamPlayer.cleanUpPlayer();
					toTeleport.put(teamPlayer.getPlayer(), spawns.get(0).getLocation());
					spawns.get(0).setPlayer(teamPlayer);
					teamPlayer.sendMessage(Main.getPrefix() + ConfigMessages.SORT_NUMBER_HOLE.getValue().replace("%number%", String.valueOf(spawns.get(0).getNumber())));
					players.remove(teamPlayer);
					spawns.remove(0);
				}
			}
		}

		for(VaroPlayer vp : players) {
			vp.sendMessage(Main.getPrefix() + ConfigMessages.SORT_NO_HOLE_FOUND.getValue());
			if(result == SortResult.SORTED_WELL) {
				result = SortResult.NO_SPAWN;
			}
		}
		
		scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), new Runnable() {
			
			int index = 0;
			
			@Override
			public void run() {
				if(index == toTeleport.size()) {
					Bukkit.getScheduler().cancelTask(scheduler);
					return;
				}
				
				Player player = (Player) toTeleport.keySet().toArray()[index];
				player.teleport(toTeleport.get(player));
				index++;
			}
		}, 0, 1);
		
		return result;
	}
}