package us.ianjohnson.tasklist.cmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TestCommand implements CommandExecutor {
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

		File file = new File("plugins/TaskList/tasks.yml");
		Player player = (Player) sender;
		FileConfiguration tasksYML = YamlConfiguration.loadConfiguration(file);
		List<String> tasks = new ArrayList<>(Objects.requireNonNull(tasksYML.getConfigurationSection(player.getWorld().getName() + ".tasks")).getKeys(false));
		player.sendMessage(tasks.toString());
		return true;
	}
}
