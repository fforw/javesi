package org.javesi.exception;


public class JavesyRuntimeException
    extends RuntimeException
{
    public JavesyRuntimeException(String message)
    {
        super(message);
    }

    public JavesyRuntimeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public JavesyRuntimeException(Throwable t)
    {
        super(t);
    }

}
