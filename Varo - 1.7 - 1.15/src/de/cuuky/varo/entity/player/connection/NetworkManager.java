package de.cuuky.varo.entity.player.connection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.entity.Player;

import de.cuuky.varo.version.BukkitVersion;
import de.cuuky.varo.version.VersionUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class NetworkManager {

	// CHAT TITLE
	private static Class<?> chatMessageTypeClass;

	private static Object genericSpeedType;
	private static Class<?> ioBase;

	// IOBASE
	private static Class<?> ioBaseChat;

	private static Class<?> titleClass;
	private static Constructor<?> titleConstructor;
	private static Class<?> packetChatClass;

	// TAB
	private static Class<?> tablistClass;
	private static Method ioBaseChatMethod;

	// ACTIONBAR
	private static Object title, subtitle;
	private static Constructor<?> chatByteMethod, chatEnumMethod;

	// FAKEHEALTH
	private static Object healthPacket;

	static {
		try {
			if(VersionUtils.getVersion().isHigherThan(BukkitVersion.ONE_7)) {
				ioBaseChat = VersionUtils.getChatSerializer();

				ioBase = Class.forName(VersionUtils.getNmsClass() + ".IChatBaseComponent");
				titleClass = Class.forName(VersionUtils.getNmsClass() + ".PacketPlayOutTitle");

				Class<?> enumTitleClass = null;
				try {
					enumTitleClass = titleClass.getDeclaredClasses()[0];
				} catch(Exception e) {
					enumTitleClass = Class.forName(VersionUtils.getNmsClass() + ".EnumTitleAction");
				}

				try {
					title = enumTitleClass.getDeclaredField("TITLE").get(null);
					subtitle = enumTitleClass.getDeclaredField("SUBTITLE").get(null);
				} catch(Exception e) {
					e.printStackTrace();
				}

				packetChatClass = Class.forName(VersionUtils.getNmsClass() + ".PacketPlayOutChat");
				try {
					chatMessageTypeClass = Class.forName(VersionUtils.getNmsClass() + ".ChatMessageType");
					chatEnumMethod = packetChatClass.getConstructor(ioBase, chatMessageTypeClass);
				} catch(Exception e) {
					chatByteMethod = packetChatClass.getConstructor(ioBase, byte.class);
				}

				titleConstructor = titleClass.getConstructor(enumTitleClass, ioBase, int.class, int.class, int.class);

				tablistClass = Class.forName(VersionUtils.getNmsClass() + ".PacketPlayOutPlayerListHeaderFooter");
				ioBaseChatMethod = ioBaseChat.getDeclaredMethod("a", String.class);

				healthPacket = Class.forName(VersionUtils.getNmsClass() + ".PacketPlayOutUpdateHealth").newInstance();
				for(Field f : healthPacket.getClass().getDeclaredFields()) {
					f.setAccessible(true);
					f.set(healthPacket, 0);
				}
			}
		} catch(ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private Player player;
	private Channel channel;
	private Object connection, playerHandle, tablist, networkManager;
	private Method sendPacketMethod;

	private Field footerField, headerField;
	private Field pingField;

	private String locale;

	public NetworkManager(Player player) {
		this.player = player;

		if(!VersionUtils.getVersion().isHigherThan(BukkitVersion.ONE_7))
			return;

		try {
			this.playerHandle = player.getClass().getMethod("getHandle").invoke(player);
			this.pingField = playerHandle.getClass().getField("ping");
			this.connection = playerHandle.getClass().getField("playerConnection").get(playerHandle);
			this.sendPacketMethod = connection.getClass().getMethod("sendPacket", Class.forName(VersionUtils.getNmsClass() + ".Packet"));
			this.networkManager = this.connection.getClass().getField("networkManager").get(this.connection);
			this.channel = (Channel) this.networkManager.getClass().getField("channel").get(this.networkManager);

			Field localeField = playerHandle.getClass().getField("locale");
			localeField.setAccessible(true);
			this.locale = (String) localeField.get(playerHandle);

			hookChannel();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void hookChannel() {
		this.channel.pipeline().addBefore("packet_handler", "Varo", new ChannelDuplexHandler() {
			@Override
			public void channelRead(ChannelHandlerContext arg0, Object arg1) throws Exception {
				Class<?> clazz = arg1.getClass();
				if(clazz.getName().endsWith("PacketPlayInSettings"))
					locale = (String) clazz.getDeclaredMethod("a").invoke(arg1);

				super.channelRead(arg0, arg1);
			}

			@Override
			public void write(ChannelHandlerContext arg0, Object arg1, ChannelPromise arg2) throws Exception {
//				Class<?> clazz = arg1.getClass();

				// Tryed to make a anti damage indicator 
//				// System.out.println(clazz.getName());
//				if(clazz.getName().contains("Metadata")) {
//					Field a = clazz.getDeclaredField("a");
//					a.setAccessible(true);
//
//					// System.out.println(a.getInt(arg1));
//					Field b = clazz.getDeclaredField("b");
//					b.setAccessible(true);
//
//					ArrayList<WatchableObject> ao = (ArrayList<WatchableObject>) b.get(arg1);
//					for(WatchableObject wo : ao) {
//						Field wa = wo.getClass().getDeclaredField("a");
//						wa.setAccessible(true);
//						if(wa.getInt(wo) == 3) {
//							Field wc = wo.getClass().getDeclaredField("c");
//							wc.setAccessible(true);
//							wc.set(wo, 0.0d);
//						}
//					}
//				}

				super.write(arg0, arg1, arg2);
			}
		});
	}

	public void close() {
		this.channel.pipeline().remove("Varo");
	}

	public Object getConnection() {
		return connection;
	}

	public int getPing() {
		if(!VersionUtils.getVersion().isHigherThan(BukkitVersion.ONE_7))
			return -1;

		try {
			return pingField.getInt(playerHandle);
		} catch(Exception e) {
			e.printStackTrace();
		}

		return -1;
	}

	public Player getPlayer() {
		return player;
	}

	public void sendFakeHealthUpdate() {
		sendPacket(healthPacket);
	}

	public void respawnPlayer() {
		try {
			Object respawnEnum = Class.forName(VersionUtils.getNmsClass() + ".EnumClientCommand").getEnumConstants()[0];
			Constructor<?>[] constructors = Class.forName(VersionUtils.getNmsClass() + ".PacketPlayInClientCommand").getConstructors();
			for(Constructor<?> constructor : constructors) {
				Class<?>[] args = constructor.getParameterTypes();
				if(args.length == 1 && args[0] == respawnEnum.getClass()) {
					Object packet = Class.forName(VersionUtils.getNmsClass() + ".PacketPlayInClientCommand").getConstructor(args).newInstance(respawnEnum);
					sendPacket(packet);
					break;
				}
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}

	public void sendActionbar(String message) {
		if(!VersionUtils.getVersion().isHigherThan(BukkitVersion.ONE_7))
			return;

		try {
			Object barchat = ioBaseChatMethod.invoke(ioBaseChat, "{\"text\": \"" + message + "\"}");

			Object packet = null;
			if(chatEnumMethod == null)
				packet = chatByteMethod.newInstance(barchat, (byte) 2);
			else
				packet = chatEnumMethod.newInstance(barchat, chatMessageTypeClass.getDeclaredField("GAME_INFO").get(null));

			sendPacket(packet);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void sendLinkedMessage(String message, String link) {
		try {
			Constructor<?> constructor = packetChatClass.getConstructor(ioBase);
			Object text = ioBaseChatMethod.invoke(ioBaseChat, "{\"text\": \"" + message + "\", \"color\": \"white\", \"clickEvent\": {\"action\": \"open_url\" , \"value\": \"" + link + "\"}}");
			Object packetFinal = constructor.newInstance(text);
			Field field = packetFinal.getClass().getDeclaredField("a");
			field.setAccessible(true);
			field.set(packetFinal, text);

			sendPacket(packetFinal);
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}

	public void sendPacket(Object packet) {
		try {
			sendPacketMethod.invoke(connection, packet);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void sendTablist(String header, String footer) {
		if(!VersionUtils.getVersion().isHigherThan(BukkitVersion.ONE_7))
			return;

		try {
			Object tabheader = ioBaseChatMethod.invoke(ioBaseChat, "{\"text\": \"" + header + "\"}");
			Object tabfooter = ioBaseChatMethod.invoke(ioBaseChat, "{\"text\": \"" + footer + "\"}");

			if(tablist == null) {
				tablist = tablistClass.newInstance();

				headerField = getField(tablist.getClass(), "a", "header");
				headerField.setAccessible(true);

				footerField = getField(tablist.getClass(), "b", "footer");
				footerField.setAccessible(true);
			}

			headerField.set(tablist, tabheader);
			footerField.set(tablist, tabfooter);

			sendPacket(tablist);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void sendTitle(String header, String footer) {
		if(!VersionUtils.getVersion().isHigherThan(BukkitVersion.ONE_7))
			return;

		try {
			Object titleHeader = ioBaseChatMethod.invoke(ioBaseChat, "{\"text\": \"" + header + "\"}");
			Object titleFooter = ioBaseChatMethod.invoke(ioBaseChat, "{\"text\": \"" + footer + "\"}");

			Object headerPacket = titleConstructor.newInstance(title, titleHeader, 0, 2, 0);
			Object footerPacket = titleConstructor.newInstance(subtitle, titleFooter, 0, 2, 0);

			sendPacket(headerPacket);
			sendPacket(footerPacket);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void setAttributeSpeed(double value) {
		if(!VersionUtils.getVersion().isHigherThan(BukkitVersion.ONE_8))
			return;

		try {
			if(genericSpeedType == null) {
				Class<?> attribute = Class.forName("org.bukkit.attribute.Attribute");
				genericSpeedType = attribute.getField("GENERIC_ATTACK_SPEED").get(attribute);
			}

			Object attributeInstance = player.getClass().getMethod("getAttribute", genericSpeedType.getClass()).invoke(player, genericSpeedType);

			attributeInstance.getClass().getMethod("setBaseValue", double.class).invoke(attributeInstance, value);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static Field getField(Class<?> clazz, String... strings) {
		for(String s : strings) {
			try {
				return clazz.getDeclaredField(s);
			} catch(NoSuchFieldException e) {
				continue;
			}
		}

		return null;
	}

	public String getLocale() {
		return this.locale;
	}
}