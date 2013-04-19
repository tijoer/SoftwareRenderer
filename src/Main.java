import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.geom.GeneralPath;

import javax.swing.ImageIcon;

import com.brackeen.javagamebook.input.GameAction;
import com.brackeen.javagamebook.input.InputManager;

/**
 * A 3D Test to demonstrate drawing polygons.
 */
public class Main {

	// create solid-colored polygons
	private SolidPolygon3D treeLeaves = new SolidPolygon3D(new Vector3D(-50,
			-35, 0), new Vector3D(50, -35, 0), new Vector3D(0, 150, 0));

	private SolidPolygon3D treeTrunk = new SolidPolygon3D(new Vector3D(-5, -50,
			0), new Vector3D(5, -50, 0), new Vector3D(5, -35, 0), new Vector3D(
			-5, -35, 0));

	private Transform3D treeTransform = new Transform3D(0, 0, -500);
	private ViewWindow viewWindow;

	private GameAction exit = new GameAction("exit");
	private GameAction zoomIn = new GameAction("zoomIn");
	private GameAction zoomOut = new GameAction("zoomOut");

	private boolean isRunning;
	protected ScreenManager2 screen;
	protected int fontSize = Constants.DEFAULT_FONT_SIZE;

	/**
	 * Entry point.
	 */
	public static void main(String[] args) {
		new Main().run();

	}

	/**
	 * Calls init() and gameLoop()
	 * 
	 * @param frame
	 */
	public void run() {
		init();
		gameLoop();
	}

	/**
	 * Signals the game loop that it's time to quit
	 */
	public void stop() {
		isRunning = false;
	}

	/**
	 * Exits the VM from a daemon thread. The daemon thread waits 2 seconds then
	 * calls System.exit(0). Since the VM should exit when only daemon threads
	 * are running, this makes sure System.exit(0) is only called if neccesary.
	 * It's neccesary if the Java Sound system is running.
	 */
	public void lazilyExit() {
		Thread thread = new Thread() {
			public void run() {
				// first, wait for the VM exit on its own.
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ex) {
				}
				// system is still running, so force an exit
				System.exit(0);
			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Sets full screen mode and initiates and objects.
	 * 
	 * @param frame
	 */
	public void init() {

		screen = new ScreenManager2();

		Window window = screen.getFrame();
		window.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		window.setBackground(Color.blue);
		window.setForeground(Color.white);

		isRunning = true;

		InputManager inputManager = new InputManager(screen.getFrame());
		// inputManager.setCursor(InputManager.INVISIBLE_CURSOR);
		inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
		inputManager.mapToKey(zoomIn, KeyEvent.VK_UP);
		inputManager.mapToKey(zoomOut, KeyEvent.VK_DOWN);

		// make the view window the entire screen
		viewWindow = new ViewWindow(0, 0, Constants.WIDTH,
				Constants.HEIGHT,
				(float) Math.toRadians(Constants.VIEW_ANGLE));

		// give the polygons color
		treeLeaves.setColor(new Color(0x008000));
		treeTrunk.setColor(new Color(0x714311));
	}

	public Image loadImage(String fileName) {
		return new ImageIcon(fileName).getImage();
	}

	/**
	 * Runs through the game loop until stop() is called.
	 */
	public void gameLoop() {
		long startTime = System.currentTimeMillis();
		long currTime = startTime;

		while (isRunning) {
			long elapsedTime = System.currentTimeMillis() - currTime;
			currTime += elapsedTime;

			// update
			update(elapsedTime);

			// draw the screen
			Graphics2D g = screen.getGraphics();
			draw(g);
			g.dispose();
			screen.update();
		}
	}

	public void update(long elapsedTime) {
		if (exit.isPressed()) {
			stop();
			return;
		}

		// cap elapsedTime
		elapsedTime = Math.min(elapsedTime, 100);

		// rotate around the y axis
		treeTransform.rotateAngleY(0.002f * elapsedTime);

		// allow user to zoom in/out
		if (zoomIn.isPressed()) {
			treeTransform.getLocation().z += 0.5f * elapsedTime;
		}
		if (zoomOut.isPressed()) {
			treeTransform.getLocation().z -= 0.5f * elapsedTime;
		}
	}

	public void draw(Graphics2D g) {
		// erase background
		g.setColor(Color.black);
		g.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);

		// draw message
		g.setColor(Color.white);
		g.drawString("Press up/down to zoom. Press Esc to exit.", 5, fontSize);

		// draw the tree polygons
		trandformAndDraw(g, treeTrunk);
		trandformAndDraw(g, treeLeaves);
	}

	/**
	 * Projects and draws a polygon onto the view window.
	 */
	private void trandformAndDraw(Graphics2D g, SolidPolygon3D poly) {
		Polygon3D transformedPolygon = new Polygon3D();
		
		transformedPolygon.setTo(poly);

		// translate and rotate the polygon
		transformedPolygon.add(treeTransform);

		// project the polygon to the screen
		transformedPolygon.project(viewWindow);

		// convert the polygon to a Java2D GeneralPath and draw it
		GeneralPath path = new GeneralPath();
		Vector3D v = transformedPolygon.getVertex(0);
		path.moveTo(v.x, v.y);
		for (int i = 1; i < transformedPolygon.getNumVertices(); i++) {
			v = transformedPolygon.getVertex(i);
			path.lineTo(v.x, v.y);
		}
		g.setColor(poly.getColor());
		g.fill(path);
	}
}