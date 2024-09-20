package house.greenhouse.effectapi.api.effect;

import house.greenhouse.effectapi.api.resource.Resource;
import net.minecraft.core.Holder;

public abstract class ResourceEffect<T> implements EffectAPIEffect {
    protected final Holder<Resource<T>> resource;

    public ResourceEffect(Holder<Resource<T>> resource) {
        this.resource = resource;
    }

    public Holder<Resource<T>> getResource() {
        return resource;
    }

    public static class ResourceHolder<T> {

        private final Holder<Resource<T>> resource;
        private T value;

        public ResourceHolder(Holder<Resource<T>> resource) {
            this.resource = resource;
            this.value = resource.value().defaultValue();
        }

        public Holder<Resource<T>> getResource() {
            return resource;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }
}
