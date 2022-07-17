package us.ianjohnson.tasklist.cmds;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.ianjohnson.tasklist.TaskList;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TaskCommand implements CommandExecutor, TabCompleter {

	private static final File tasksFile = new File(TaskList.getInstance().getDataFolder(), "tasks.yml");
	private static final FileConfiguration tasksYML = YamlConfiguration.loadConfiguration(tasksFile);

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender.hasPermission("tasklist.user") || sender.isOp())) {
			return true;
		}

		try {

			switch (args[0].toLowerCase()) {
				case "add":
					add(sender, args);
					break;
				case "remove":
					remove(sender, args);
					break;
				case "list":
					list(sender, args);
					break;
				case "complete":
					complete(sender, args);
					break;
				case "reset":
					reset(sender, args);
					break;
				default:
					TaskList.getPhrase("invalid_command");
					break;
			}
			if ("title".equalsIgnoreCase(args[1])) {
				update(sender, args);
			}
		} catch (Exception ignored) {
			TaskList.getPhrase("invalid_command");
			return true;
		}
		return true;
	}

	private void update(CommandSender sender, String[] args) throws IOException {
		Player player = (Player) sender;
		StringBuilder sb = new StringBuilder();
		int taskNumber = Integer.parseInt(args[2]);
		if (!tasksYML.contains(player.getWorld().getName().toLowerCase() + "." + taskNumber)) {
			player.sendMessage(TaskList.getPluginPrefix() + "§cTask " + taskNumber + " does not exist.");
			return;
		}

		for (int i = 3; i < args.length; i++) {
			sb.append(args[i]);
			sb.append(" ");
		}

		String task_name = sb.toString();
		task_name = task_name.substring(0, task_name.length() - 1);

		tasksYML.set(player.getWorld().getName().toLowerCase() + "." + taskNumber + ".name", task_name);
		tasksYML.save(tasksFile);
		player.sendMessage(TaskList.getPluginPrefix() + "§aTask " + taskNumber + " updated.");
	}

	private void add(CommandSender sender, String[] args) throws IOException {
		Player player = (Player) sender;
		StringBuilder sb = new StringBuilder();
		int taskNumber;
		boolean task_status = false;

		for (int i = 1; i < args.length; i++) {
			sb.append(args[i]);
			sb.append(" ");
		}

		String task_name = sb.toString();
		task_name = task_name.substring(0, task_name.length() - 1);

		if (!tasksYML.contains(player.getWorld().getName().toLowerCase())) {
			taskNumber = 1;
		} else {
			taskNumber = Objects.requireNonNull(tasksYML.getConfigurationSection(player.getWorld().getName().toLowerCase())).getKeys(false).size() + 1;
		}

		tasksYML.set(player.getWorld().getName().toLowerCase() + "." + taskNumber + ".name", task_name);
		tasksYML.set(player.getWorld().getName().toLowerCase() + "." + taskNumber + ".status", task_status);
		tasksYML.save(tasksFile);
		player.sendMessage(TaskList.getPluginPrefix() + "§aTask " + taskNumber + " added.");
	}

	private void remove(CommandSender sender, String[] args) throws IOException {
		/*
		 *
		 * //TODO: REORDER THE TASKS AFTER REMOVING A TASK
		 *
		 * */
		Player player = (Player) sender;
		int taskNumber = Integer.parseInt(args[1]);
		if (!tasksYML.contains(player.getWorld().getName().toLowerCase() + "." + taskNumber)) {
			player.sendMessage(TaskList.getPluginPrefix() + "§cTask " + taskNumber + " does not exist.");
			return;
		}
		tasksYML.set(player.getWorld().getName().toLowerCase() + "." + taskNumber, null);
		tasksYML.save(tasksFile);
		player.sendMessage(TaskList.getPluginPrefix() + "§cTask " + taskNumber + " has been removed.");
	}

	private void list(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		StringBuilder sb = new StringBuilder();
		String[] taskNumbers = Objects.requireNonNull(tasksYML.getConfigurationSection(player.getWorld().getName().toLowerCase())).getKeys(false).toArray(new String[0]);
		if (taskNumbers.length == 0) {
			sb.append("§cNo tasks found.");
		}
		for (String taskNumber : taskNumbers) {
			if (tasksYML.getBoolean(player.getWorld().getName().toLowerCase() + "." + taskNumber + ".status")) {
				sb.append("§a");
				sb.append(taskNumber);
				sb.append(". ");
				sb.append(tasksYML.getString(player.getWorld().getName().toLowerCase() + "." + taskNumber + ".name"));
				sb.append("\n");
			} else {
				sb.append("§c");
				sb.append(taskNumber);
				sb.append(". ");
				sb.append(tasksYML.getString(player.getWorld().getName().toLowerCase() + "." + taskNumber + ".name"));
				sb.append("\n");
			}
		}
		player.sendMessage(TaskList.getPluginPrefix() + "§7Tasks for " + player.getWorld().getName() + ":\n§a{} §7= Completed | §c{} §7= Incomplete §r\n\n" + sb.toString());

	}

	private void complete(CommandSender sender, String[] args) throws IOException {
		Player player = (Player) sender;
		int taskNumber = Integer.parseInt(args[1]);
		if (tasksYML.getBoolean(player.getWorld().getName().toLowerCase() + "." + taskNumber + ".status")) {
			player.sendMessage(TaskList.getPluginPrefix() + "§cTask " + taskNumber + " is already completed.");
			return;
		}
		tasksYML.set(player.getWorld().getName().toLowerCase() + "." + taskNumber + ".status", true);
		tasksYML.save(tasksFile);
		player.sendMessage(TaskList.getPluginPrefix() + "§aTask " + taskNumber + " marked as complete.");
		Song s = NBSDecoder.parse(new File(TaskList.getInstance().getDataFolder(), "complete.nbs"));
		SongPlayer sp = new RadioSongPlayer(s);
		sp.addPlayer(player.getUniqueId());
		sp.setPlaying(true);
	}

	private void reset(CommandSender sender, String[] args) throws IOException {
		Player player = (Player) sender;
		int taskNumber = Integer.parseInt(args[1]);

		if (!tasksYML.contains(player.getWorld().getName().toLowerCase() + "." + taskNumber)) {
			player.sendMessage(TaskList.getPluginPrefix() + "§cThis task doesn't exist");
			return;
		}
		if (!tasksYML.getBoolean(player.getWorld().getName().toLowerCase() + "." + taskNumber + ".status")) {
			player.sendMessage(TaskList.getPluginPrefix() + "§cTask " + taskNumber + " is already incomplete.");
			return;
		}
		tasksYML.set(player.getWorld().getName().toLowerCase() + "." + taskNumber + ".status", false);
		tasksYML.save(tasksFile);
		player.sendMessage(TaskList.getPluginPrefix() + "§cTask " + taskNumber + " marked as incomplete.");
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		Player player = (Player) sender;
		String[] taskNumbers = Objects.requireNonNull(tasksYML.getConfigurationSection(player.getWorld().getName().toLowerCase())).getKeys(false).toArray(new String[0]);
		String[] subCommands = {"add", "remove", "list", "help", "update", "complete", "reset"};

		try {
			if (args[0].equals("update") && args[1].equals("title")) {
				return Arrays.asList(taskNumbers);
			}
			if (args[0].equals("update")) {
				return Collections.singletonList("title");
			}
			if (args[0].equals("remove") || args[0].equals("complete") || args[0].equals("reset")) {
				return Arrays.asList(taskNumbers);
			}
		} catch (Exception e) {
			return Arrays.asList(subCommands);
		}
		return Arrays.asList(subCommands);
	}
}