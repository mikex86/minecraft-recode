package me.gommeantilegit.minecraft.server.console;

import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.server.console.command.Command;
import me.gommeantilegit.minecraft.server.console.command.manager.CommandManager;
import org.jetbrains.annotations.NotNull;

import java.util.Scanner;

@SideOnly(side = Side.SERVER)
public class ConsoleReader extends Thread {

    /**
     * Server minecraft instance
     */
    @NotNull
    private final ServerMinecraft minecraft;

    /**
     * Console scanner
     */
    @NotNull
    private final Scanner scanner;

    /**
     * Command Manager
     */
    @NotNull
    private final CommandManager commandManager;

    public ConsoleReader(@NotNull ServerMinecraft minecraft) {
        super("Console-Reader-Thread");
        this.minecraft = minecraft;
        this.scanner = new Scanner(System.in);
        this.commandManager = new CommandManager(minecraft);
    }

    @Override
    public void run() {
        try {
            while (this.minecraft.isRunning()) {
                if (this.scanner.hasNextLine()) {
                    String line = this.scanner.nextLine();
                    minecraft.logger.info("Console input: \"" + line + "\"", false);
                    String[] args = line.split(" ");
                    if (args.length > 0) {
                        boolean commandFound = false;
                        for (Command command : this.commandManager.getCommands()) {
                            if (command.getName().equals(args[0])) {
                                String msg = command.onCommand(args);
                                if (msg.startsWith("ERROR: "))
                                    minecraft.logger.err(msg.substring(7));
                                else
                                    minecraft.logger.info(msg);
                                commandFound = true;
                                break;
                            }
                        }
                        if (!commandFound)
                            minecraft.logger.err("Unknown command entered!");
                    } else {
                        minecraft.logger.err("Invalid command entered!");
                    }
                }
                Thread.sleep(1000);

            }
        } catch (InterruptedException e) {
            minecraft.logger.info("Console reader interrupted");
        }
    }
}
