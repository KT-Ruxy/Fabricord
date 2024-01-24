package com.inf_ruxy.projects.mc.plugin.several.fabricord

import com.inf_ruxy.projects.mc.plugin.several.fabricord.Fabricord.MOD_ID
import com.inf_ruxy.projects.mc.plugin.several.fabricord.Fabricord.logger
import com.inf_ruxy.projects.mc.plugin.several.fabricord.discord.DiscordBotManager
import com.inf_ruxy.projects.mc.plugin.several.fabricord.discord.DiscordEmbed
import com.inf_ruxy.projects.mc.plugin.several.fabricord.discord.console.DiscordConsoleCommandListener
import com.inf_ruxy.projects.mc.plugin.several.fabricord.discord.console.DiscordLogAppender
import com.inf_ruxy.projects.mc.plugin.several.fabricord.discord.events.DiscordMessageListener
import com.inf_ruxy.projects.mc.plugin.several.fabricord.discord.events.MCMessageListener
import com.inf_ruxy.projects.mc.plugin.several.fabricord.util.ConfigLoader
import com.inf_ruxy.projects.mc.plugin.several.fabricord.util.ConfigManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.LogManager
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import java.io.File

object FabricordApi {

    private lateinit var configManager: ConfigManager
    lateinit var discordBotManager: DiscordBotManager
    lateinit var discordEmbed: DiscordEmbed
    lateinit var config: ConfigLoader
    lateinit var dml: DiscordMessageListener
    lateinit var mcml: MCMessageListener
    lateinit var discordConsoleCommandListener: DiscordConsoleCommandListener

    val dataFolder: File = File(FabricLoader.getInstance().gameDir.toFile(), MOD_ID)

    fun serverStarted(server: MinecraftServer) {
        configManager = ConfigManager()
        config = ConfigLoader()
        discordBotManager = DiscordBotManager()
        discordConsoleCommandListener = DiscordConsoleCommandListener(server)
        discordEmbed = DiscordEmbed()
        dml = DiscordMessageListener(server)
        mcml = MCMessageListener()

        configManager.checkDataFolder()
        configManager.checkConfigFile()
        config.loadConfig()
    }

    fun serverStarting(server: MinecraftServer) {
        if (config.isBotTokenAndLogChannelIDNull()) {
            logger.error("Bot token or log channel ID is null. Please check your config file.")
            return
        } else {
            discordBotManager.startBot()
            discordBotManager.registerEventListeners()
            if (config.doBridgeConsole()) {
                val discordLogAppender = discordBotManager.jda?.let { DiscordLogAppender.createAppender(it) }
                discordLogAppender?.let { addAppenderToLogger(it) }
            }
        }
    }

    private fun addAppenderToLogger(appender: DiscordLogAppender) {
        val ctx: LoggerContext = LogManager.getContext(false) as LoggerContext
        val config: Configuration = ctx.configuration
        appender.start()
        config.addAppender(appender)
        val loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME)
        loggerConfig.addAppender(appender, null, null)
        ctx.updateLoggers()
    }

    fun serverStopping(server: MinecraftServer) {
        if (config.isBotTokenAndLogChannelIDNull()) {
            discordBotManager.stopBot()
        }
    }


}