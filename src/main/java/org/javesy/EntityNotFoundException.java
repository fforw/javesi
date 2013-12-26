package org.javesy;

public class EntityNotFoundException
    extends JavesyRuntimeException
{
    private static final long serialVersionUID = -287722023809611330L;

    public EntityNotFoundException(String message)
    {
        super(message);
    }
}
