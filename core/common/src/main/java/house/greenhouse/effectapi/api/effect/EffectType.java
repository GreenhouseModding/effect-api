package house.greenhouse.effectapi.api.effect;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.Collection;
import java.util.function.BiFunction;

public class EffectType<E, T> implements DataComponentType<E> {
    private final Codec<E> codec;
    private final BiFunction<T, ResourceLocation, LootContext> contextCreator;
    private final Collection<LootContextParam<?>> requiredParams;

    protected EffectType(Codec<E> codec, BiFunction<T, ResourceLocation, LootContext> contextCreator, Collection<LootContextParam<?>> requiredParams) {
        this.codec = codec;
        this.contextCreator = contextCreator;
        this.requiredParams = requiredParams;
    }

    @Override
    public Codec<E> codec() {
        return codec;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, E> streamCodec() {
        return ByteBufCodecs.fromCodecWithRegistries(codec);
    }

    public LootContext createContext(T provider, ResourceLocation source) {
        return contextCreator.apply(provider, source);
    }

    public Collection<LootContextParam<?>> requiredParams() {
        return this.requiredParams;
    }

    public static class Builder<E, T> {
        protected Codec<E> codec;
        protected BiFunction<T, ResourceLocation, LootContext> contextCreator;
        protected Collection<LootContextParam<?>> requiredParams;

        protected Builder() {

        }

        public Builder<E, T> codec(Codec<E> codec) {
            this.codec = codec;
            return this;
        }

        public Builder<E, T> lootContext(BiFunction<T, ResourceLocation, LootContext> contextCreator, LootContextParamSet paramSet) {
            this.contextCreator = contextCreator;
            this.requiredParams = paramSet.getRequired();
            return this;
        }

        public Builder<E, T> lootContext(BiFunction<T, ResourceLocation, LootContext> contextCreator, Collection<LootContextParam<?>> requiredParams) {
            this.contextCreator = contextCreator;
            this.requiredParams = requiredParams;
            return this;
        }

        public EffectType<E, T> build() {
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
