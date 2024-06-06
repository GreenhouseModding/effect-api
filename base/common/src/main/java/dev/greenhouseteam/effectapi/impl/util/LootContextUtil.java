package dev.greenhouseteam.effectapi.impl.util;

import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.effectapi.mixin.LootContextAccessor;
import dev.greenhouseteam.effectapi.mixin.LootParamsAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

import java.util.Map;

public class LootContextUtil {
    public static LootParams.Builder copyIntoParamBuilder(LootContext context) {
        LootParams.Builder params = new LootParams.Builder(EffectAPI.getHelper().getServer().getLevel(Level.OVERWORLD));
        for (Map.Entry<LootContextParam<?>, Object> entry : ((LootParamsAccessor)((LootContextAccessor)context).effectapi$getParams()).effectapi$getParams().entrySet()) {
            params.withParameter((LootContextParam<Object>)entry.getKey(), entry.getValue());
        }
        return params;
    }
}