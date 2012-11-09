package hellomisterme.gimbal;

import hellomisterme.gimbal.graphics.Render;
import hellomisterme.gimbal.input.KeyInput;
import hellomisterme.gimbal.io.Savegame;
import hellomisterme.gimbal.world.World;
import hellomisterme.gimbal.world.testWorld;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

/**
 * The Game handles display and management of game objects.
 * 
 * @version Pre-Alpha.0.04
 * @since 10-14-12
 * @author David Aaron Suddjian
 */
public class Game extends Canvas implements Runnable {
	private static final long serialVersionUID = 1L;

	public static int width = 800;
	public static int height = width * 10 / 16;
	public static String title = "Gimbal";
	public static final int TICKS_PER_SECOND = 60;

	private Thread thread;
	private JFrame frame;
	boolean running = false;

	private Render render;
	private BufferedImage image;

	private World world;

	private boolean screenshotOrdered = false;
	private boolean ioOrdered = false;

	public Game() {
		Dimension size = new Dimension(width, height);
		setPreferredSize(size);

		// initialize visual elements
		frame = new JFrame();
		render = new Render(width, height);
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		render.pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		// initialize world
		world = new testWorld(width, height);

		// initialize keyboard input
		addKeyListener(new KeyInput());
		KeyInput.readSettingsFile();
	}

	/**
	 * Starts running this Game
	 */
	public synchronized void start() {
		createBufferStrategy(3);
		
		running = true;
		thread = new Thread(this);
		thread.start();
	}

	/**
	 * This is it. The game loop. If something happens in the game, it begins here.
	 * 
	 * Called by this Game's Thread thread.
	 */
	public void run() {
		
		int totalFrames = 0; // the total number of frames generated
		int totalSeconds = 0; // the number of times totalFrames has been updated
		int frameCount = 0; // FPS timer variable
		int tickCount = 0; // TPS timer variable
		long lastRecord = System.currentTimeMillis(); // the last time frameCount and tickCount were written to console

		// regulate tick frequency
		double ns = 1000000000.0 / (double) TICKS_PER_SECOND; // time between ticks
		double delta = 0; // difference between now and the last tick
		long lastTime = System.nanoTime();
		
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;

			while (delta >= 1) {
				tick();
				delta--;
				tickCount++;
			}

			render();
			frameCount++;

			// count and print the FPS, TPS, AVG, and SEC to console and titlebar
			if (System.currentTimeMillis() >= lastRecord + 1000) {
				totalFrames += frameCount;
				totalSeconds++;
				frame.setTitle(title + "   -   FPS: " + frameCount + ",   AVG: " + (totalFrames / totalSeconds) + ",   TPS: " + tickCount + ",   SEC: " + totalSeconds);
				frameCount = 0;
				tickCount = 0;
				lastRecord = System.currentTimeMillis();
			}
		}
		stop();
	}

	/**
	 * Calls the world's tick() and callTick() methods, and checks for over-all game-related events, like screenshot key presses
	 */
	private void tick() {
		// if the screenshot key is pressed
		if (KeyInput.pressed(KeyInput.screenshot)) {
			if (screenshotOrdered == false) { // if the screenshot key was up before
				render.screenshot();
				screenshotOrdered = true; // remember that screenshot was pressed
			}
		} else { // screenshot key not pressed
			screenshotOrdered = false; 
		}
		
		// if an io key is pressed
		if (KeyInput.pressed(KeyInput.save)) {
			if (ioOrdered == false) { // if an io key was up before
				Savegame.saveData(world, "quicksave");
				ioOrdered = true; // remember that io was ordered
			}
		} else if (KeyInput.pressed(KeyInput.load)) {
			if (ioOrdered == false) { // if the io key was up before
				Savegame.loadData(world, "quicksave");
				ioOrdered = true; // remember that io was ordered
			}
		} else { // io keys not pressed
			ioOrdered = false; 
		}
		
		world.tick();
		world.callTick();
	}

	/**
	 * Calls render method of render, and displays the buffered image
	 */
	private void render() {
		BufferStrategy strategy = getBufferStrategy();

		render.render(world.getEntities());

		Graphics g = strategy.getDrawGraphics();
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
		g.dispose();

		strategy.show();
	}

	/**
	 * Stops running the game
	 */
	public synchronized void stop() {
		running = false;
		try {
			thread.join(); // end thread, can't have that dangling there
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Game game = new Game();

		game.frame.setResizable(false);
		game.frame.setTitle(title);
		game.frame.add(game);
		game.frame.pack(); // automatically set size
		game.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		game.requestFocus();
		game.frame.setLocationRelativeTo(null); // center
		game.frame.setVisible(true);

		game.start();
	}

}
