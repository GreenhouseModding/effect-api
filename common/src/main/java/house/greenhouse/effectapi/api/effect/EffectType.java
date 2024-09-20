package house.greenhouse.effectapi.api.effect;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.registry.EffectAPILootContextParamSets;
import house.greenhouse.effectapi.impl.EffectAPIEffectTypeInternals;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.Collection;
import java.util.function.BiFunction;

public class EffectType<E> implements DataComponentType<E> {
    private final Codec<E> codec;
    private final BiFunction<Entity, ResourceLocation, LootContext> contextCreator;
    private final Collection<LootContextParam<?>> requiredParams;

    protected EffectType(Codec<E> codec, BiFunction<Entity, ResourceLocation, LootContext> contextCreator, Collection<LootContextParam<?>> requiredParams) {
        this.codec = codec;
        this.contextCreator = contextCreator;
        this.requiredParams = requiredParams;
    }

    public static <E> Builder<E> builder() {
        return new Builder<>();
    }

    @Override
    public Codec<E> codec() {
        return codec;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, E> streamCodec() {
        return ByteBufCodecs.fromCodecWithRegistries(codec);
    }

    public LootContext createContext(Entity provider, ResourceLocation source) {
        return contextCreator.apply(provider, source);
    }

    public Collection<LootContextParam<?>> requiredParams() {
        return this.requiredParams;
    }

    public static class Builder<E> {
        protected Codec<E> codec;
        protected BiFunction<Entity, ResourceLocation, LootContext> contextCreator = EffectAPIEffectTypeInternals::buildDefaultContext;
        protected Collection<LootContextParam<?>> requiredParams = EffectAPILootContextParamSets.ENTITY.getRequired();

        protected Builder() {

        }

        public Builder<E> codec(Codec<E> codec) {
            this.codec = codec;
            return this;
        }

        public Builder<E> lootContext(BiFunction<Entity, ResourceLocation, LootContext> contextCreator, LootContextParamSet paramSet) {
            this.contextCreator = contextCreator;
            this.requiredParams = paramSet.getRequired();
            return this;
        }

        public Builder<E> lootContext(BiFunction<Entity, ResourceLocation, LootContext> contextCreator, Collection<LootContextParam<?>> requiredParams) {
            this.contextCreator = contextCreator;
            this.requiredParams = requiredParams;
            return this;
        }

        public EffectType<E> build() {
            if (codec == null)
                throw new RuntimeException("EffectType requires a codec.");
            if (contextCreator == null)
                throw new RuntimeException("EffectType requires a loot context creator.");
            if (requiredParams == null)
                throw new RuntimeException("EffectType requires required parameters");
            return new EffectType<>(codec, contextCreator, requiredParams);
        }
    }
}
