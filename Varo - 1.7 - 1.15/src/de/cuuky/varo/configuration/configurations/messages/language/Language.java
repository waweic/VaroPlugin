package de.cuuky.varo.configuration.configurations.messages.language;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;

import de.cuuky.varo.configuration.configurations.messages.language.languages.DefaultLanguage;
import de.cuuky.varo.configuration.configurations.messages.language.languages.LoadableMessage;

public class Language {

	private String name;
	private LanguageManager manager;
	private boolean loaded;

	private File file;
	private YamlConfiguration configuration;

	private Class<? extends LoadableMessage> clazz;
	private LoadableMessage[] values;
	private HashMap<String, String> messages;

	public Language(String name, LanguageManager manager) {
		this(name, manager, null);
	}

	public Language(String name, LanguageManager manager, Class<? extends LoadableMessage> clazz) {
		this.name = name;
		this.clazz = clazz;
		this.manager = manager;
		this.messages = new HashMap<>();

		if(this.clazz != null) {
			try {
				this.values = (LoadableMessage[]) clazz.getMethod("values").invoke(null);
			} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}

			load();
		}
	}

	private void saveConfiguration() {
		try {
			this.configuration.save(file);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void load() {
		this.file = new File(manager.getLanguagePath(), this.name + ".yml");
		this.configuration = YamlConfiguration.loadConfiguration(this.file);
		this.configuration.options().copyDefaults(true);

		ArrayList<String> loadedMessages = new ArrayList<>();
		boolean save = file.exists();

		if(this.clazz != null) {
			for(LoadableMessage message : values) {
				if(!this.configuration.contains(message.getPath())) {
					save = true;
					this.configuration.addDefault(message.getPath(), message.getDefaultMessage());
				}

				if(message instanceof DefaultLanguage)
					((DefaultLanguage) message).setMessage(this.configuration.getString(message.getPath()));
				loadedMessages.add(message.getPath());
			}
		}

		for(String path : this.configuration.getKeys(true)) {
			if(this.configuration.isConfigurationSection(path))
				continue;

			if(clazz != null && clazz.isAssignableFrom(DefaultLanguage.class))
				if(!loadedMessages.contains(path)) {
					save = true;
					System.out.println("Removed lang path " + path);
					this.configuration.set(path, null);
					continue;
				}

			messages.put(path, this.configuration.getString(path));
		}

		if(save)
			saveConfiguration();

		this.loaded = true;
	}

	public String getMessage(String path) {
		if(!loaded)
			load();

		return messages.get(path);
	}

	public String getName() {
		return this.name;
	}

	public HashMap<String, String> getMessages() {
		return this.messages;
	}
}