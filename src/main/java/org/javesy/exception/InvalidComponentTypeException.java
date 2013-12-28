package org.javesy.exception;

public class InvalidComponentTypeException
    extends JavesyRuntimeException
{
    private static final long serialVersionUID = -1755123010735318365L;

    public InvalidComponentTypeException(String message)
    {
        super(message);
    }

    public InvalidComponentTypeException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
