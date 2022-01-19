package greencity.service;

public interface FormatterService<T, X> {
    /**
     * Method that formats an object.
     */
    T format(X ob);
}
