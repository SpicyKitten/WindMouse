package windmouse;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import windmouse.Action.Type;

public class WindMouse
{
	private static final ThreadLocalRandom random = ThreadLocalRandom.current();
	private static final double sqrt2 = Math.sqrt(2);
	private static final double sqrt3 = Math.sqrt(3);
	private static final double sqrt5 = Math.sqrt(5);
	private static Robot robot;
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	public Stream<Action> moveMouse(int x, int y, int rx, int ry)
	{
		return this.moveMouse(x, y, rx, ry, 1.44, 1.6);
	}

	public Stream<Action> moveMouse(int x, int y, int rx, int ry, double minSpeed,
		double maxSpeed)
	{
		x += random.nextInt(rx);
		y += random.nextInt(ry);

		x = Math.max(0, Math.min(x, this.screenSize.width - 1));
		y = Math.max(0, Math.min(y, this.screenSize.height - 1));

		var startPos = this.getMousePos();
		if(startPos.x() == x && startPos.y() == y)
		{
			return Stream.empty();
		}
		var randomSpeed = random.nextDouble(minSpeed, maxSpeed);
		var firstMovement = Optional.<Stream<Action>>empty();
		if(Math.hypot(x - startPos.x(), y - startPos.y()) > 170 && Math.random() < 0.23)
		{
			double dX = x - startPos.x();
			double dY = y - startPos.y();
			var magnitude = Math.hypot(dX, dY);
			var dMag = random.nextDouble(1 - randomSpeed / 15, 1 + randomSpeed / 30);
			magnitude = Math.max(magnitude + random.nextInt(30), magnitude * dMag);
			var dTheta = 1.1 * Math.PI / 180d;
			dTheta = random.nextDouble(-dTheta, dTheta);
			var direction = (Math.atan2(dY, dX) + dTheta) % (Math.PI * 2d);
			direction = direction < 0 ? direction + Math.PI * 2d : direction;
			dX = magnitude * Math.cos(direction);
			dY = magnitude * Math.sin(direction);
			var destX = Math.max(0, Math.min(startPos.x() + dX, this.screenSize.width - 1));
			var destY = Math.max(0, Math.min(startPos.y() + dY, this.screenSize.height - 1));
			var actions = this
				.windMouseImpl(startPos.x(), startPos.y(), destX, destY, 9.0, 4.0,
					10.0 / randomSpeed, 15.0 / randomSpeed, 10.0 * randomSpeed, 10.0 * randomSpeed)
				.collect(Collectors.toList());
			if(actions.size() > 0)
			{
				for(var action : actions)
				{
					startPos = action.type() == Type.MOVEMENT ? action.destination()
						: startPos;
				}
			}
			firstMovement = Optional.of(actions.stream());
			randomSpeed = randomSpeed > (minSpeed + maxSpeed) / 2.0
				? random.nextDouble(minSpeed, randomSpeed)
				: random.nextDouble(randomSpeed, maxSpeed);
		}
		var secondMovement = this.windMouseImpl(startPos.x(), startPos.y(), x, y, 9.0, 3.5,
			10.0 / randomSpeed, 15.0 / randomSpeed, 10.0 * randomSpeed, 10.0 * randomSpeed);
		return firstMovement.isEmpty() ? secondMovement
			: Stream.concat(firstMovement.get(), secondMovement);
	}

	/**
	 * Slightly modified version of the WindMouse mouse movement algorithm.
	 * WindMouse was created by Benjamin J. Land.
	 *
	 * @param xNow
	 *            The x start
	 * @param yNow
	 *            The y start
	 * @param xEnd
	 *            The x destination
	 * @param yEnd
	 *            The y destination
	 * @param gravity
	 *            Strength pulling the position towards the destination
	 * @param wind
	 *            Strength pulling the position in random directions
	 * @param minWait
	 *            Minimum relative time per step
	 * @param maxWait
	 *            Maximum relative time per step
	 * @param maxStep
	 *            Maximum size of a step, prevents out of control motion
	 * @param targetArea
	 *            Radius of area around the destination that should trigger
	 *            slowing, prevents spiraling
	 * @result The sequence of actions executed during the mouse movement
	 */
	private Stream<Action> windMouseImpl(double xNow, double yNow, double xEnd, double yEnd,
		double gravity, double wind, double minWait, double maxWait, double maxStep,
		double targetArea)
	{
		Stream.Builder<Action> builder = Stream.builder();
		double dist, veloX = 0, veloY = 0, windX = 0, windY = 0;
		var cx = (int) xNow;
		var cy = (int) yNow;
		while((dist = Math.hypot(xNow - xEnd, yNow - yEnd)) >= 1)
		{
			wind = Math.min(wind, dist);
			if(dist >= targetArea)
			{
				windX = windX / sqrt3 + (2D * Math.random() - 1D) * wind / sqrt5;
				windY = windY / sqrt3 + (2D * Math.random() - 1D) * wind / sqrt5;
			}
			else
			{
				windX /= sqrt2;
				windY /= sqrt2;
				if(maxStep < 3)
				{
					maxStep = Math.random() * 3D + 3D;
				}
				else
				{
					maxStep /= sqrt5;
				}
			}
			veloX += windX + gravity * (xEnd - xNow) / dist;
			veloY += windY + gravity * (yEnd - yNow) / dist;
			var veloMag = Math.hypot(veloX, veloY);
			if(veloMag > maxStep)
			{
				var randomDist = maxStep / 2D + Math.random() * maxStep / 2D;
				veloX = veloX / veloMag * randomDist;
				veloY = veloY / veloMag * randomDist;
			}
			xNow += veloX;
			yNow += veloY;
			var mx = (int) Math.round(xNow);
			var my = (int) Math.round(yNow);
			if(mx < 0 || my < 0 || mx >= this.screenSize.width || my >= this.screenSize.height)
			{
				mx = Math.max(0, Math.min(mx, this.screenSize.width - 1));
				xNow = Math.max(0d, Math.min(xNow, this.screenSize.width - 1));
				my = Math.max(0, Math.min(my, this.screenSize.height - 1));
				yNow = Math.max(0d, Math.min(yNow, this.screenSize.width - 1));
			}
			if(cx != mx || cy != my)
			{
				cx = mx;
				cy = my;
				builder.add(new Action(mx, my));
			}
			var step = Math.hypot(xNow - cx, yNow - cy);
			var delay = Math.round((maxWait - minWait) * (step / maxStep) + minWait);
			builder.add(new Action(delay));
		}
		return builder.build();
	}

	private Point getMousePos()
	{
		var currentMousePos = MouseInfo.getPointerInfo().getLocation();
		return new Point(currentMousePos.x, currentMousePos.y);
	}

	private void sleep(long ms)
	{
		try
		{
			Thread.sleep(ms);
		}
		catch(InterruptedException ex)
		{
		}
	}

	public void execute(Action action)
	{
		if(action.type() == Type.MOVEMENT)
		{
			var x = action.destination().x();
			var y = action.destination().y();
			if(x < 0 || y < 0 || x >= this.screenSize.width || y >= this.screenSize.height)
			{
				x = Math.max(0, Math.min(x, this.screenSize.width - 1));
				y = Math.max(0, Math.min(y, this.screenSize.height - 1));
			}
			try
			{
				if(robot == null)
				{
					robot = new Robot();
				}
				robot.mouseMove(x, y);
			}
			catch(Exception e)
			{
			}
		}
		else if(action.type() == Type.DELAY)
		{
			this.sleep(action.delay());
		}
	}
}
