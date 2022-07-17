package us.ianjohnson.tasklist;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import us.ianjohnson.tasklist.cmds.TaskCommand;
import us.ianjohnson.tasklist.cmds.TestCommand;
import us.ianjohnson.tasklist.utils.Utilities;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class TaskList extends JavaPlugin {

	@Getter
	private static TaskList instance;
	@Getter
	private static final String consolePrefix = "[TaskList] ";
	@Getter
	private static final String pluginPrefix = "§7[§eTaskList§7] §r";
	private static final Map<String, String> phrases = new HashMap<>();

	@Override
	public void onEnable() {
		instance = this;
		registerCommands();
		loadFiles();
		//check if plugin is up to date from github releases
		Utilities.checkForUpdates(this);
		if (!Utilities.checkDependencies()) {
			getLogger().severe("NoteBlockAPI-2.0-SNAPSHOT not found, disabling plugin.");
			getLogger().severe("Please download it from https://ci.haprosgames.com/job/NoteBlockAPI-2.0/");
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}

	@Override
	public void onDisable() {
		instance = null;
	}

	private void registerCommands() {
		Objects.requireNonNull(getCommand("tasklist")).setExecutor(new TaskCommand());
		Objects.requireNonNull(getCommand("test")).setExecutor(new TestCommand());
	}

	public void loadFiles() {

		File taskFile = new File(getDataFolder(), "tasks.yml");
		FileConfiguration taskFileConfig = new YamlConfiguration();
		File langFile = new File(getDataFolder(), "language.yml");
		FileConfiguration langFileConfig = new YamlConfiguration();
		File configFile = new File(getDataFolder(), "config.yml");
		File completeFile = new File(getDataFolder(), "complete.nbs");

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

		if (!completeFile.exists()) {
			Utilities.loadResource(this, "complete.nbs");
		}

		Bukkit.getLogger().info(consolePrefix + "Added audio files.");
		Bukkit.getLogger().info(consolePrefix + "Loaded " + phrases.size() + " phrases.");
		Bukkit.getLogger().info(consolePrefix + "Settings reloaded from config.yml");
		Bukkit.getLogger().info(consolePrefix + "Phrases reloaded from tasks.yml");
	}

	public static String getPhrase(String key) {
		return phrases.get(key);
	}
}
