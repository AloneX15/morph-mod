package com.takumistudios.morphmod.client.mixin;

import com.takumistudios.morphmod.client.character.ClientCharacters;
import java.util.*;
import net.minecraft.server.packs.repository.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds the validated session pack without altering the user's selected packs. */
@Mixin(PackRepository.class)
public abstract class CharacterPackMixin {
    @Inject(method = "discoverAvailable", at = @At("RETURN"), cancellable = true)
    private void morphmod$characters(CallbackInfoReturnable<Map<String, Pack>> cir) {
        Pack pack = ClientCharacters.pack();
        if (pack != null) { Map<String, Pack> packs = new TreeMap<>(cir.getReturnValue()); packs.put(pack.getId(), pack); cir.setReturnValue(packs); }
    }
}
