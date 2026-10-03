package com.takumistudios.morphmod;

import com.takumistudios.morphmod.command.MorphCommand;
import com.takumistudios.morphmod.config.MorphConfig;
import com.takumistudios.morphmod.data.MorphAttachments;
import com.takumistudios.morphmod.event.MorphEvents;
import com.takumistudios.morphmod.morph.MorphRegistry;
import com.takumistudios.morphmod.network.MorphNetworking;
import com.takumistudios.morphmod.util.FeatureGuard;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
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
		// Startup only: disk operations run on Morph-IO before gameplay callbacks.
		config = MorphConfig.loadAsync(FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json")).join();
		MorphAttachments.init();
		MorphRegistry.init();
		com.takumistudios.morphmod.character.CharacterEntity.init();
		MorphNetworking.init();
		MorphEvents.init();
		com.takumistudios.morphmod.character.CharacterPermissions.init();
		com.takumistudios.morphmod.network.CharacterNetworking.init();
		FeatureGuard commands = new FeatureGuard("command registration");
		CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> commands.run(() -> MorphCommand.register(dispatcher, context)));
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> { MorphConfig.shutdown(); com.takumistudios.morphmod.character.CharacterCatalog.shutdown(); });
		String version = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata().getVersion().getFriendlyString();
		LOGGER.info("Morph {} cargado. Creado por TakumiStudios.", version);
	}

	public static MorphConfig config() {
		return config;
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
