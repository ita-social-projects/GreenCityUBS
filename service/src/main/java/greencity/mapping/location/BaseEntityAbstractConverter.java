package greencity.mapping.location;

import greencity.entity.user.locations.BaseEntityForEnAndUkNames;

public abstract class BaseEntityAbstractConverter<S> {
    /**
     * Converts the given source to an instance of the given targetClass.
     *
     * @param source      the source object to convert
     * @param targetClass the target class to convert to
     * @param <T>         the type of the target class
     * @return an instance of the target class
     */
    protected abstract <T extends BaseEntityForEnAndUkNames> T convert(S source, Class<T> targetClass);
}
