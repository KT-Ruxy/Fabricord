package com.inf_ruxy.projects.mc.plugin.several.fabricord.discord.console

import com.inf_ruxy.projects.mc.plugin.several.fabricord.FabricordApi.config
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.ParseResults
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource

class DiscordConsoleCommandListener(
    private val minecraftServer: MinecraftServer
) : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return

        if (event.channel.id != config.consoleLogChannelID) return

        val command = event.message.contentDisplay.trim()

        val commandDispatcher: CommandDispatcher<ServerCommandSource> = minecraftServer.commandManager.dispatcher
        val commandSource: ServerCommandSource = minecraftServer.commandSource

        val parseResults: ParseResults<ServerCommandSource> = commandDispatcher.parse(command, commandSource)

        if (parseResults.context.nodes.isNotEmpty()) {
            commandDispatcher.execute(parseResults)
        }
    }
}