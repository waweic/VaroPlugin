package de.cuuky.varo.item;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import de.cuuky.varo.Main;
import de.cuuky.varo.utils.JavaUtils;
import de.cuuky.varo.version.BukkitVersion;
import de.cuuky.varo.version.VersionUtils;
import de.cuuky.varo.version.types.Materials;

public class ItemBuilder {

	private static Class<?> itemFlagClass;
	private static String[] attributes;
	private static Object[] itemFlags;

	static {
		try {
			if(VersionUtils.getVersion().isHigherThan(BukkitVersion.ONE_7)) {
				itemFlagClass = Class.forName("org.bukkit.inventory.ItemFlag");

				attributes = new String[] { "HIDE_ATTRIBUTES", "HIDE_DESTROYS", "HIDE_ENCHANTS", "HIDE_PLACED_ON", "HIDE_POTION_EFFECTS", "HIDE_UNBREAKABLE" };
				itemFlags = new Object[attributes.length];

				for(int i = 0; i < attributes.length; i++) {
					try {
						itemFlags[i] = itemFlagClass.getDeclaredField(attributes[i]).get(null);
					} catch(IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
						e.printStackTrace();
					}
				}
			}
		} catch(ClassNotFoundException e) {}
	}

	private int amount;
	private String displayName;
	private ArrayList<String> lore;
	private String playerName;
	private ItemStack stack;

	public ItemBuilder() {
		amount = 1;
	}

	public ItemBuilder amount(int amount) {
		this.amount = amount;
		return this;
	}

	public ItemStack build() {
		ItemMeta stackMeta = stack.getItemMeta();
		if(displayName != null && stack.getType() != Material.AIR)
			stackMeta.setDisplayName(displayName);

		if(lore != null)
			stackMeta.setLore(lore);
		stack.setItemMeta(stackMeta);
		this.deleteDamageAnnotation();
		stack.setAmount(amount);

		return stack;
	}

	@SuppressWarnings("deprecation")
	public ItemStack buildSkull() {
		stack = Materials.PLAYER_HEAD.parseItem();
		SkullMeta skullMeta = (SkullMeta) stack.getItemMeta();

		skullMeta.setDisplayName(displayName != null ? Main.getColorCode() + displayName : Main.getColorCode() + playerName);
		skullMeta.setOwner(playerName != null ? playerName : displayName);

		if(lore != null)
			skullMeta.setLore(lore);

		stack.setItemMeta(skullMeta);
		stack.setAmount(amount);

		return stack;
	}

	public void deleteDamageAnnotation() {
		ItemMeta meta = stack.getItemMeta();
		if(!VersionUtils.getVersion().isHigherThan(BukkitVersion.ONE_7)) {
			for(Enchantment key : meta.getEnchants().keySet()) {
				meta.removeEnchant(key);
			}
			// TODO Hide other attributes?
		} else {
			// Reflections for errorless display of the menu for 1.7
			try {
				Method m = meta.getClass().getDeclaredMethod("addItemFlag", itemFlagClass);
				for(Object obj : itemFlags) {
					m.invoke(meta, obj);
				}
			} catch(Exception e) {}
		}

		stack.setItemMeta(meta);
	}

	public ItemBuilder displayname(String displayname) {
		this.displayName = displayname;
		return this;
	}

	public ItemBuilder itemstack(ItemStack stack) {
		this.stack = stack;
		return this;
	}

	public ItemBuilder lore(ArrayList<String> lore) {
		this.lore = lore;
		return this;
	}

	public ItemBuilder lore(String lore) {
		this.lore = JavaUtils.collectionToArray(new String[] { lore });
		return this;
	}

	public ItemBuilder lore(String[] lore) {
		this.lore = JavaUtils.collectionToArray(lore);
		return this;
	}

	public ItemBuilder player(Player player) {
		this.playerName = player.getName();
		return this;
	}

	public ItemBuilder playername(String playername) {
		this.playerName = playername;
		return this;
	}
}