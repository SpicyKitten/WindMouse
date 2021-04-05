package windmouse;

public record Movement(Movement.Type type, Point destination, long delay)
{
	public Movement
	{
	}

	public Movement(int destX, int destY)
	{
		this(new Point(destX, destY));
	}

	public Movement(Point destination)
	{
		this(Type.MOUSE_MOVEMENT, destination, 0);
	}

	public Movement(long delay)
	{
		this(Type.DELAY, null, delay);
	}

	public enum Type
	{
		MOUSE_MOVEMENT, DELAY
	}
}
