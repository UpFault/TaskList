package us.ianjohnson.tasklist.utils;

import com.google.common.io.ByteStreams;
import org.bukkit.plugin.Plugin;
import us.ianjohnson.tasklist.TaskList;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class Utilities {

	/**
	 * Description: Copies a file from the jar to the server's data folder
	 *
	 * @param plugin   The plugin instance
	 * @param resource The file to copy
	 */
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

	public static boolean checkDependencies() {
		File nbapi = new File("plugins/NoteBlockAPI-2.0-SNAPSHOT.jar");
		if (!nbapi.exists()) {
			return false;
		}
		return true;
	}

	public static void checkForUpdates(TaskList taskList) {
		String currentVersion = taskList.getDescription().getVersion();
		String latestVersion = null;
		try {
			latestVersion = taskList.getDescription().getVersion();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (latestVersion != null && !latestVersion.equals(currentVersion)) {
			taskList.getLogger().info("A new version of TaskList is available!");
			taskList.getLogger().info("Current version: " + currentVersion);
			taskList.getLogger().info("Latest version: " + latestVersion);
		}
	}

/*	public static String rainbowify(String string) {
		String[] colors = {"§a", "§b", "§c", "§d", "§e", "§f", "§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9"};
		String[] words = string.split(" ");
		StringBuilder sb = new StringBuilder();
		for (String word : words) {
			for (int i = 0; i < word.length(); i++) {
				sb.append(colors[i % colors.length]);
				sb.append(word.charAt(i));
			}
			sb.append(" ");
		}
		return sb.toString();
	}*/

}

