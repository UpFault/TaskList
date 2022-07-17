package us.ianjohnson.tasklist.utils;

import com.google.common.io.ByteStreams;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.ianjohnson.tasklist.TaskList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class Utilities {

	public static File loadResource(Plugin plugin, String resource) {
		File folder = plugin.getDataFolder();
		if (!folder.exists())
			folder.mkdir();
		File resourceFile = new File(folder, resource);
		try {
			if (!resourceFile.exists()) {
				resourceFile.createNewFile();
				try (InputStream in = plugin.getResource(resource);
					 OutputStream out = Files.newOutputStream(resourceFile.toPath())) {
					assert in != null;
					ByteStreams.copy(in, out);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resourceFile;
	}

	public static void checkForUpdates(TaskList taskList) {
		String currentVersion = taskList.getDescription().getVersion();
		String latestVersion = "https://www.github.com/UpFault/TaskList/releases/latest";
		try {
			latestVersion = taskList.getDescription().getVersion();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!latestVersion.equals(currentVersion)) {
			taskList.getLogger().info("A new version of TaskList is available!");
			taskList.getLogger().info("Current version: " + currentVersion);
			taskList.getLogger().info("Latest version: " + latestVersion);
		}
	}

	public static void reorderList(Player player) throws IOException {
		File tasksFile = new File(TaskList.getInstance().getDataFolder(), "tasks.yml");
		FileConfiguration tasksYML = YamlConfiguration.loadConfiguration(tasksFile);
		
	}
}

