package tests;

import java.awt.MouseInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import windmouse.WindMouse;

class WindMouseTest
{
	private WindMouse hmm;

	@BeforeEach
	void setUp() throws Exception
	{
		this.hmm = new WindMouse();
	}

	@Test
	void test()
	{
		this.hmm.moveMouse(300, 300, 1, 1).forEach(this.hmm::execute);
		var mousePos = MouseInfo.getPointerInfo().getLocation();
		Assertions.assertTrue(mousePos.x >= 299);
		Assertions.assertTrue(mousePos.x <= 301);
		Assertions.assertTrue(mousePos.y >= 299);
		Assertions.assertTrue(mousePos.y <= 301);
	}
}
