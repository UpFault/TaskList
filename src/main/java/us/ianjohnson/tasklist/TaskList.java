package us.ianjohnson.tasklist;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;
import us.ianjohnson.tasklist.cmds.TaskCommand;
import us.ianjohnson.tasklist.utils.Utilities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class TaskList extends JavaPlugin implements Listener {

	@Getter
	private static TaskList instance;
	@Getter
	private static final String consolePrefix = "[TaskList] ";
	@Getter
	private static final String pluginPrefix = "§7[§eTaskList§7] §r";
	private static final Map<String, String> phrases = new HashMap<>();
	private final File tasksFile = new File(this.getDataFolder(), "tasks.yml");

	@Override
	public void onEnable() {
		instance = this;
		Utilities.checkForUpdates();
		registerCommands();
		loadFiles();
	}

	@Override
	public void onDisable() {
		instance = null;
	}

	private void registerCommands() {
		Objects.requireNonNull(getCommand("tasklist")).setExecutor(new TaskCommand());
	}

	public void loadFiles() {

		File taskFile = new File(getDataFolder(), "tasks.yml");
		FileConfiguration taskFileConfig = new YamlConfiguration();
		File langFile = new File(getDataFolder(), "language.yml");
		FileConfiguration langFileConfig = new YamlConfiguration();
		File configFile = new File(getDataFolder(), "config.yml");

		if (!taskFile.exists()) {
			Utilities.loadResource(this, "tasks.yml");
		}
		try {
			taskFileConfig.load(taskFile);
		} catch (Exception e3) {
			e3.printStackTrace();
		}
		for (String langString : taskFileConfig.getKeys(false)) {
			phrases.put(langString, taskFileConfig.getString(langString));
		}
		if (!configFile.exists()) {
			Utilities.loadResource(this, "config.yml");
		}
		if (!langFile.exists()) {
			Utilities.loadResource(this, "language.yml");
		}
		try {
			langFileConfig.load(langFile);
		} catch (Exception e3) {
			e3.printStackTrace();
		}
		for (String langString : langFileConfig.getKeys(false)) {
			phrases.put(langString, langFileConfig.getString(langString));
		}
		FileConfiguration tasksYML = YamlConfiguration.loadConfiguration(tasksFile);
		for (String world : Bukkit.getWorlds().stream().map(WorldInfo::getName).toArray(String[]::new)) {
			if (!tasksYML.contains(world)) {
				if (world.contains("_nether") || world.contains("_the_end")) {
					continue;
				}
				tasksYML.set(world, new HashMap<>());
			}
		}
		try {
			tasksYML.save(tasksFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Bukkit.getLogger().info(consolePrefix + "Loaded Audio Files, Phrases, Worlds, and Config Values");
	}

	public static String getPhrase(String key) {
		return phrases.get(key);
	}
}
