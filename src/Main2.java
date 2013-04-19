import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import com.brackeen.javagamebook.input.GameAction;
import com.brackeen.javagamebook.input.InputManager;

/**
 * A 3D Test to demonstrate drawing polygons.
 */
public class Main2 {

	// create solid-colored polygons
	private SolidPolygon3D treeLeaves = new SolidPolygon3D(new Vector3D(-50,
			-35, 0), new Vector3D(50, -35, 0), new Vector3D(0, 150, 0));

	private SolidPolygon3D treeTrunk = new SolidPolygon3D(new Vector3D(-5, -50,
			0), new Vector3D(5, -50, 0), new Vector3D(5, -35, 0), new Vector3D(
			-5, -35, 0));

	private Transform3D treeTransform = new Transform3D(0, 0, -500);
	private ViewWindow viewWindow;

	private GameAction exit = new GameAction("exit");
	private GameAction smallerView = new GameAction("smallerView",
			GameAction.DETECT_INITAL_PRESS_ONLY);
	private GameAction largerView = new GameAction("largerView",
			GameAction.DETECT_INITAL_PRESS_ONLY);
	private GameAction frameRateToggle = new GameAction("frameRateToggle",
			GameAction.DETECT_INITAL_PRESS_ONLY);
	private GameAction goForward = new GameAction("goForward");
	private GameAction goBackward = new GameAction("goBackward");
	private GameAction goUp = new GameAction("goUp");
	private GameAction goDown = new GameAction("goDown");
	private GameAction goLeft = new GameAction("goLeft");
	private GameAction goRight = new GameAction("goRight");
	private GameAction turnLeft = new GameAction("turnLeft");
	private GameAction turnRight = new GameAction("turnRight");
	private GameAction tiltUp = new GameAction("tiltUp");
	private GameAction tiltDown = new GameAction("tiltDown");
	private GameAction tiltLeft = new GameAction("tiltLeft");
	private GameAction tiltRight = new GameAction("tiltRight");
	// private GameAction mousePressed = new GameAction("mousePressed");

	private boolean isRunning;
	protected ScreenManager2 screen;

	protected PolygonRenderer polygonRenderer;

	protected List<SolidPolygon3D> polygons;

	DebugWindow debugWindow;
	
	long firstFrame;
	int frames;
	long currentFrame;
	int fps;

	/**
	 * Entry point.
	 */
	public static void main(String[] args) {
		new Main2().run();

	}

	/**
	 * Calls init() and gameLoop()
	 * 
	 * @param frame
	 */
	public void run() {
		debugWindow = new DebugWindow();
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
		window.setFont(new Font("Dialog", Font.PLAIN,
				Constants.DEFAULT_FONT_SIZE));
		window.setBackground(Color.blue);
		window.setForeground(Color.white);

		isRunning = true;

		InputManager inputManager = new InputManager(screen.getFrame());
		// inputManager.setCursor(InputManager.INVISIBLE_CURSOR);
		inputManager.setRelativeMouseMode(true);

		inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
		inputManager.mapToKey(goForward, KeyEvent.VK_W);
		inputManager.mapToKey(goForward, KeyEvent.VK_UP);
		inputManager.mapToKey(goBackward, KeyEvent.VK_S);
		inputManager.mapToKey(goBackward, KeyEvent.VK_DOWN);
		inputManager.mapToKey(goLeft, KeyEvent.VK_A);
		inputManager.mapToKey(goLeft, KeyEvent.VK_LEFT);
		inputManager.mapToKey(goRight, KeyEvent.VK_D);
		inputManager.mapToKey(goRight, KeyEvent.VK_RIGHT);
		inputManager.mapToKey(goUp, KeyEvent.VK_PAGE_UP);
		inputManager.mapToKey(goUp, KeyEvent.VK_SPACE);
		inputManager.mapToKey(goDown, KeyEvent.VK_PAGE_DOWN);
		inputManager.mapToKey(goDown, KeyEvent.VK_CONTROL);
		inputManager.mapToMouse(turnLeft, InputManager.MOUSE_MOVE_LEFT);
		inputManager.mapToMouse(turnRight, InputManager.MOUSE_MOVE_RIGHT);
		inputManager.mapToMouse(tiltUp, InputManager.MOUSE_MOVE_UP);
		inputManager.mapToMouse(tiltDown, InputManager.MOUSE_MOVE_DOWN);

		inputManager.mapToKey(tiltLeft, KeyEvent.VK_INSERT);
		inputManager.mapToKey(tiltRight, KeyEvent.VK_DELETE);

		inputManager.mapToKey(smallerView, KeyEvent.VK_SUBTRACT);
		inputManager.mapToKey(smallerView, KeyEvent.VK_MINUS);
		inputManager.mapToKey(largerView, KeyEvent.VK_ADD);
		inputManager.mapToKey(largerView, KeyEvent.VK_PLUS);
		inputManager.mapToKey(largerView, KeyEvent.VK_EQUALS);
		inputManager.mapToKey(frameRateToggle, KeyEvent.VK_R);

		createPolygonRenderer();

		createCube(200, new Vector3D(0, 0, -800));
	}

	public void createPolygonRenderer() {
		// make the view window the entire screen
		// make the view window the entire screen
		viewWindow = new ViewWindow(0, 0, Constants.WIDTH, Constants.HEIGHT,
				(float) Math.toRadians(Constants.VIEW_ANGLE));

		Transform3D camera = new Transform3D(0, 0, 0);
		polygonRenderer = new SolidPolygonRenderer(camera, viewWindow);
	}

	public void createCube(float size, Vector3D position) {
		polygons = new ArrayList<SolidPolygon3D>();

		SolidPolygon3D polygon;

		polygon = new SolidPolygon3D(new Vector3D(-size, size, -size),
				new Vector3D(size, size, -size), new Vector3D(size, -size,
						-size), new Vector3D(-size, -size, -size));
		polygon.setColor(Color.GREEN);
		polygons.add(polygon);

		polygon = new SolidPolygon3D(new Vector3D(-size, -size, -size),
				new Vector3D(-size, -size, size), new Vector3D(-size, size,
						size), new Vector3D(-size, size, -size));
		polygon.setColor(Color.BLUE);
		polygons.add(polygon);

		polygon = new SolidPolygon3D(new Vector3D(size, size, -size),
				new Vector3D(size, size, size),
				new Vector3D(size, -size, size), new Vector3D(size, -size,
						-size));
		polygon.setColor(Color.RED);
		polygons.add(polygon);

		polygon = new SolidPolygon3D(new Vector3D(-size, -size, size),
				new Vector3D(size, -size, size),
				new Vector3D(size, size, size),
				new Vector3D(-size, size, +size));
		polygon.setColor(Color.YELLOW);
		polygons.add(polygon);

		polygon = new SolidPolygon3D(new Vector3D(-size, size, -size),
				new Vector3D(-size, size, size),
				new Vector3D(size, size, size), new Vector3D(size, size, -size));
		polygon.setColor(Color.MAGENTA);
		polygons.add(polygon);

		polygon = new SolidPolygon3D(new Vector3D(size, -size, -size),
				new Vector3D(size, -size, size), new Vector3D(-size, -size,
						size), new Vector3D(-size, -size, -size));
		polygon.setColor(Color.CYAN);
		polygons.add(polygon);

		for (SolidPolygon3D itt : polygons) {
			itt.add(position);
		}
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
			
			calculateFps();
			//screen.getFrame().setTitle("FPS: " + fps);
			debugWindow.addOrSetInfo("FPS", "FPS: " + fps);

			// draw the screen
			Graphics2D g = screen.getGraphics();
			draw(g);
			g.dispose();
			screen.update();
		}

		screen.dispose();
		if(debugWindow != null) {
			debugWindow.dispose();
		}
	}

	public void update(long elapsedTime) {
		if (exit.isPressed()) {
			stop();
			return;
		}

		// cap elapsedTime
		elapsedTime = Math.min(elapsedTime, 100);

		float angleChange = 0.0002f * elapsedTime;
		float distanceChange = .5f * elapsedTime;

		Transform3D camera = polygonRenderer.getCamera();
		Vector3D cameraLoc = camera.getLocation();

		// apply movement
		if (goForward.isPressed()) {
			cameraLoc.x -= distanceChange * camera.getSinAngleY();
			cameraLoc.z -= distanceChange * camera.getCosAngleY();
		}
		if (goBackward.isPressed()) {
			cameraLoc.x += distanceChange * camera.getSinAngleY();
			cameraLoc.z += distanceChange * camera.getCosAngleY();
		}
		if (goLeft.isPressed()) {
			cameraLoc.x -= distanceChange * camera.getCosAngleY();
			cameraLoc.z += distanceChange * camera.getSinAngleY();
		}
		if (goRight.isPressed()) {
			cameraLoc.x += distanceChange * camera.getCosAngleY();
			cameraLoc.z -= distanceChange * camera.getSinAngleY();
		}
		if (goUp.isPressed()) {
			cameraLoc.y += distanceChange;
		}
		if (goDown.isPressed()) {
			cameraLoc.y -= distanceChange;
		}

		// look up/down (rotate around x)
		int tilt = tiltUp.getAmount() - tiltDown.getAmount();
		tilt = Math.min(tilt, 200);
		tilt = Math.max(tilt, -200);

		// limit how far you can look up/down
		float newAngleX = camera.getAngleX() + tilt * angleChange
				* Constants.MOUSE_SENSITIVITY;
		newAngleX = Math.max(newAngleX, (float) -Math.PI / 2);
		newAngleX = Math.min(newAngleX, (float) Math.PI / 2);
		camera.setAngleX(newAngleX);

		// turn (rotate around y)
		int turn = turnLeft.getAmount() - turnRight.getAmount();
		// turn = Math.min(turn, 200);
		// turn = Math.max(turn, -200);
		camera.rotateAngleY(turn * angleChange * Constants.MOUSE_SENSITIVITY);

		// tilet head left/right (rotate around z)
		if (tiltLeft.isPressed()) {
			camera.rotateAngleZ(10 * angleChange);
		}
		if (tiltRight.isPressed()) {
			camera.rotateAngleZ(-10 * angleChange);
		}
	}

	public void draw(Graphics2D g) {
		// draw polygons
		polygonRenderer.startFrame(g);
		for (int i = 0; i < polygons.size(); i++) {
			polygonRenderer.draw(g, polygons.get(i));
		}
		polygonRenderer.endFrame(g);
	}

	int calculateFps() {
		frames++;
		currentFrame = System.currentTimeMillis();
		if (currentFrame > firstFrame + 1000) {
			firstFrame = currentFrame;
			fps = frames;
			frames = 0;
		}
		return fps;
	}

	// /**
	// * Projects and draws a polygon onto the view window.
	// */
	// private void trandformAndDraw(Graphics2D g, SolidPolygon3D poly) {
	// Polygon3D transformedPolygon = new Polygon3D();
	//
	// transformedPolygon.setTo(poly);
	//
	// // translate and rotate the polygon
	// transformedPolygon.add(treeTransform);
	//
	// // project the polygon to the screen
	// transformedPolygon.project(viewWindow);
	//
	// // convert the polygon to a Java2D GeneralPath and draw it
	// GeneralPath path = new GeneralPath();
	// Vector3D v = transformedPolygon.getVertex(0);
	// path.moveTo(v.x, v.y);
	// for (int i = 1; i < transformedPolygon.getNumVertices(); i++) {
	// v = transformedPolygon.getVertex(i);
	// path.lineTo(v.x, v.y);
	// }
	// g.setColor(poly.getColor());
	// g.fill(path);
	// }
}
