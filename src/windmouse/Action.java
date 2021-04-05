package windmouse;

public record Action(Action.Type type, Point destination, long delay)
{
	public Action
	{
	}

	public Action(int destX, int destY)
	{
		this(new Point(destX, destY));
	}

	public Action(Point destination)
	{
		this(Type.MOVEMENT, destination, 0);
	}

	public Action(long delay)
	{
		this(Type.DELAY, null, delay);
	}

	public enum Type
	{
		MOVEMENT, DELAY
	}
}
