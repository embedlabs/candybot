package puzzle;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

public class Puzzle extends BasicGame {

	private Image background;
	private GameContainer container;

	public Puzzle() {
		super("Background");
	}

	public void init(GameContainer container) throws SlickException {
		this.container = container;

		container.setShowFPS(false);
		container.setVSync(true);
		container.setVerbose(false);

		container.setIcon("res/icon.png");

		background = new Image("res/background.png");

	}

	public void render(GameContainer container, Graphics g) throws SlickException {
		background.draw(0, 0, 640, 480);
	}

	public void update(GameContainer container, int delta)
			throws SlickException {

	}

	public void keyPressed(int key, char c) {
		if (key == Input.KEY_S) {
		}
	}

	public void mousePressed(int button, int x, int y) {

	}

	public static void main(String[] argv) {
		try {
			AppGameContainer container = new AppGameContainer(new Puzzle());
			container.start();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
}
