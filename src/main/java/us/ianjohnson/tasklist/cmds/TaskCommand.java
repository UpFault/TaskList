package us.ianjohnson.tasklist.cmds;

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
import us.ianjohnson.tasklist.utils.Utilities;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TaskCommand implements CommandExecutor, TabCompleter {

	private static final File tasksFile = new File(TaskList.getInstance().getDataFolder(), "tasks.yml");
	private static final FileConfiguration tasksYML = YamlConfiguration.loadConfiguration(tasksFile);

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		Player player = (Player) sender;

		if (TaskList.getInstance().getConfig().getStringList("disabled-worlds").contains(player.getWorld().getName()) && !args[0].equals("reload")) {
			sender.sendMessage(TaskList.getPluginPrefix() + "You cannot use this command in this world.");
			return true;
		}
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
					list(sender);
					break;
				case "complete":
					complete(sender, args);
					break;
				case "reset":
					reset(sender, args);
					break;
				case "reload":
					reload(sender);
					break;
				default:
					player.sendMessage(TaskList.getPluginPrefix() + TaskList.getPhrase("invalid_command"));
					break;
			}
			if ("title".equalsIgnoreCase(args[1])) {
				update(sender, args);
			}
		} catch (Exception ignored) {
			return true;
		}
		return true;
	}

	private void reload(CommandSender sender) {
		if (!sender.isOp()) {
			return;
		}
		TaskList.getInstance().reloadConfig();
		TaskList.getInstance().saveConfig();
		sender.sendMessage(TaskList.getPluginPrefix() + "Reloaded config and all its values.");
	}

	private void update(CommandSender sender, String[] args) throws IOException {
		String taskNumber = args[2];
		Player player = (Player) sender;
		StringBuilder sb = new StringBuilder();

		if (!tasksYML.contains(player.getWorld().getName().toLowerCase() + "." + taskNumber)) {
			player.sendMessage(TaskList.getPluginPrefix() + "Task " + taskNumber + " does not exist.");
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
		player.sendMessage(TaskList.getPluginPrefix() + "Task " + taskNumber + " updated.");
	}

	private void add(CommandSender sender, String[] args) throws IOException {
		String taskNumber;
		boolean task_status = false;
		Player player = (Player) sender;
		StringBuilder sb = new StringBuilder();

		for (int i = 1; i < args.length; i++) {
			sb.append(args[i]);
			sb.append(" ");
		}

		String task_name = sb.toString();
		task_name = task_name.substring(0, task_name.length() - 1);

		taskNumber = Utilities.createId(5, "Task", "");

		tasksYML.set(player.getWorld().getName().toLowerCase() + "." + taskNumber + ".name", task_name);
		tasksYML.set(player.getWorld().getName().toLowerCase() + "." + taskNumber + ".status", task_status);
		tasksYML.save(tasksFile);
		player.sendMessage(TaskList.getPluginPrefix() + "Task " + taskNumber + " added.");
	}

	private void remove(CommandSender sender, String[] args) throws IOException {
		String taskNumber = args[1];
		Player player = (Player) sender;

		if (args[1].equalsIgnoreCase("all")) {
			tasksYML.set(player.getWorld().getName().toLowerCase(), null);
			tasksYML.save(tasksFile);
			player.sendMessage(TaskList.getPluginPrefix() + "All tasks have been removed.");
			return;
		}

		if (!tasksYML.contains(player.getWorld().getName().toLowerCase() + "." + taskNumber)) {
			player.sendMessage(TaskList.getPluginPrefix() + "Task " + taskNumber + " does not exist.");
			return;
		}
		tasksYML.set(player.getWorld().getName().toLowerCase() + "." + taskNumber, null);
		tasksYML.save(tasksFile);
		player.sendMessage(TaskList.getPluginPrefix() + "Task " + taskNumber + " has been removed.");
	}

	private void list(CommandSender sender) {
		Player player = (Player) sender;
		StringBuilder sb = new StringBuilder();

		if (Objects.requireNonNull(tasksYML.getConfigurationSection(player.getWorld().getName().toLowerCase())).getKeys(false).toArray(new String[0]).length == 0) {
			sb.append("??cNo tasks found.");
		}
		for (String taskNumber : Objects.requireNonNull(tasksYML.getConfigurationSection(player.getWorld().getName().toLowerCase())).getKeys(false).toArray(new String[0])) {
			if (tasksYML.getBoolean(player.getWorld().getName().toLowerCase() + "." + taskNumber + ".status")) {
				sb.append("??a");
				sb.append(taskNumber);
				sb.append(". ");
				sb.append(tasksYML.getString(player.getWorld().getName().toLowerCase() + "." + taskNumber + ".name"));
				sb.append("\n");
			} else {
				sb.append("??c");
				sb.append(taskNumber);
				sb.append(". ");
				sb.append(tasksYML.getString(player.getWorld().getName().toLowerCase() + "." + taskNumber + ".name"));
				sb.append("\n");
			}
		}
		player.sendMessage(TaskList.getPluginPrefix() + "Tasks for " + player.getWorld().getName() + ":\n??a{} ??7= Completed | ??c{} ??7= Incomplete ??r\n\n" + sb);

	}

	@SuppressWarnings("all")
	private void complete(CommandSender sender, String[] args) throws IOException {
		String taskNumber = args[1];
		Player player = (Player) sender;

		if (args[1].equalsIgnoreCase("all")) {
			for (String taskNumber1 : Objects.requireNonNull(tasksYML.getConfigurationSection(player.getWorld().getName().toLowerCase())).getKeys(false).toArray(new String[0])) {
				tasksYML.set(player.getWorld().getName().toLowerCase() + "." + taskNumber1 + ".status", true);
				tasksYML.save(tasksFile);
			}
			player.sendMessage(TaskList.getPluginPrefix() + "All tasks have been completed.");
			return;
		}

		if (tasksYML.getBoolean(player.getWorld().getName().toLowerCase() + "." + taskNumber + ".status")) {
			player.sendMessage(TaskList.getPluginPrefix() + "Task " + taskNumber + " is already completed.");
			return;
		}
		tasksYML.set(player.getWorld().getName().toLowerCase() + "." + taskNumber + ".status", true);
		tasksYML.save(tasksFile);
		player.sendMessage(TaskList.getPluginPrefix() + "Task " + taskNumber + " marked as complete.");
	}

	private void reset(CommandSender sender, String[] args) throws IOException {
		String taskNumber = args[1];
		Player player = (Player) sender;

		if (args[1].equalsIgnoreCase("all")) {
			for (String taskNumber1 : Objects.requireNonNull(tasksYML.getConfigurationSection(player.getWorld().getName().toLowerCase())).getKeys(false).toArray(new String[0])) {
				tasksYML.set(player.getWorld().getName().toLowerCase() + "." + taskNumber1 + ".status", false);
				tasksYML.save(tasksFile);
			}
			player.sendMessage(TaskList.getPluginPrefix() + "All tasks have been reset.");
			return;
		}

		if (!tasksYML.contains(player.getWorld().getName().toLowerCase() + "." + taskNumber)) {
			player.sendMessage(TaskList.getPluginPrefix() + "This task doesn't exist");
			return;
		}
		if (!tasksYML.getBoolean(player.getWorld().getName().toLowerCase() + "." + taskNumber + ".status")) {
			player.sendMessage(TaskList.getPluginPrefix() + "Task " + taskNumber + " is already incomplete.");
			return;
		}
		tasksYML.set(player.getWorld().getName().toLowerCase() + "." + taskNumber + ".status", false);
		tasksYML.save(tasksFile);
		player.sendMessage(TaskList.getPluginPrefix() + "Task " + taskNumber + " marked as incomplete.");
	}

	@Override
	@SuppressWarnings("all")
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		Player player = (Player) sender;
		String[] subCommands = {"add", "remove", "list", "update", "complete", "reset", "reload"};
		if (args.length == 0) {
			try {
				return Arrays.asList(subCommands);
			} catch (Exception ignored) {
				return Arrays.asList(Objects.requireNonNull(tasksYML.getConfigurationSection(player.getWorld().getName().toLowerCase())).getKeys(false).toArray(new String[0]));
			}
		}
		try {
			if (args[0].equals("update") && args[1].equals("title")) {
				return Arrays.asList(Objects.requireNonNull(tasksYML.getConfigurationSection(player.getWorld().getName().toLowerCase())).getKeys(false).toArray(new String[0]));
			}
			if (args[0].equals("update")) {
				return Collections.singletonList("title");
			}
			List<String> list = new ArrayList<>();
			list.add("all");
			list.addAll(Objects.requireNonNull(tasksYML.getConfigurationSection(player.getWorld().getName().toLowerCase())).getKeys(false));

			if (args[0].equals("remove") || args[0].equals("complete") || args[0].equals("reset")) {
				return list;
			}
		} catch (Exception e) {
			return Arrays.asList(subCommands);
		}
		return Arrays.asList(subCommands);
	}
}
