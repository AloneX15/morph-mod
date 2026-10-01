package io.github.jmarc.morph;

import io.github.jmarc.morph.command.MorphCommand;
import io.github.jmarc.morph.config.MorphConfig;
import io.github.jmarc.morph.data.MorphAttachments;
import io.github.jmarc.morph.event.MorphEvents;
import io.github.jmarc.morph.morph.MorphRegistry;
import io.github.jmarc.morph.network.MorphNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MorphMod implements ModInitializer {
	public static final String MOD_ID = "morphmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static MorphConfig config = new MorphConfig();

	@Override
	public void onInitialize() {
		config = MorphConfig.load(FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json"));
		MorphAttachments.init();
		MorphRegistry.init();
		MorphNetworking.init();
		MorphEvents.init();
		CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> MorphCommand.register(dispatcher, context));
		LOGGER.info("Morph mod ready");
	}

	public static MorphConfig config() {
		return config;
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
