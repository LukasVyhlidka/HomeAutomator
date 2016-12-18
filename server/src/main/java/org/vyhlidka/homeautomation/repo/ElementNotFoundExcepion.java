package org.vyhlidka.homeautomation.repo;

public class ElementNotFoundExcepion extends RepositoryException {

    public ElementNotFoundExcepion(final String message) {
        super(message);
    }

    public ElementNotFoundExcepion(final String message, final Throwable cause) {
        super(message, cause);
    }
}
