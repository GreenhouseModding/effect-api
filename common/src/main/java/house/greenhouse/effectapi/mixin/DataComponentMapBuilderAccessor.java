package house.greenhouse.effectapi.mixin;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(DataComponentMap.Builder.class)
public interface DataComponentMapBuilderAccessor {
    @Invoker("buildFromMapTrusted")
    static DataComponentMap effect_api$invokeBuildFromMapTrusted(Map<DataComponentType<?>, Object> map) {
        throw new RuntimeException();
    }
}
