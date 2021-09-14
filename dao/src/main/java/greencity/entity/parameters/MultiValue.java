package greencity.entity.parameters;

public class MultiValue<T> {
    private T value;
    private Class clazz;

    /**
     * Method for .
     */
    public MultiValue(T value) {
        this.value = value;
    }

    /**
     * Method for .
     */
    public T getValue() {
        return value;
    }

    /**
     * Method for .
     */
    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    /**
     * Method for .
     */
    public Class getClazz() {
        return clazz;
    }
}
