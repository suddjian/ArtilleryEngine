package hellomisterme.artillery_engine.entities.mob;

import hellomisterme.artillery_engine.Err;
import hellomisterme.artillery_engine.entities.Mover;
import hellomisterme.artillery_engine.graphics.AnimatedSprite;
import hellomisterme.artillery_engine.world.World;

import java.awt.Rectangle;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Describes a basic Mob, with an animation and a hitbox.
 * 
 * @since 11-22-12
 * @author David Aaron Suddjian
 */
public abstract class Mob extends Mover implements Physical {
	
	protected AnimatedSprite animation;
	
	public static final int DEFAULT_ANIM_SPEED = 5;
	protected int animationSpeed = DEFAULT_ANIM_SPEED; // how many ticks between animation frames
	protected int animationTimer = 0; // how many ticks since the last frame update

	/** This Player's hitbox. The xLocation and yLocation variables are relative to the Player's position. */
	public Rectangle hitbox;
	
	/**
	 * If it's time to change frames, do it. Will not work if the image is not an animation.
	 */
	public void animate() {
		if (animationTimer % animationSpeed == 0) {
			animation.next();
		}
		animationTimer++;
	}
	
	/**
	 * If this Mob is outside the world bounds, sets the xLocation and yLocation positions to be exactly at the edge of the world bounds.
	 */
	public void correctOOB() {
		World w = getWorld();
		if (getExactX() >= w.getWidth() - image.getWidth()) { // right
			setPos(w.getWidth() - image.getWidth() - 1, getExactY());
		} else if (getExactX() < 0) { // left
			setPos(0, getExactY());
		}
		if (getExactY() >= w.getHeight() - image.getHeight()) { // bottom
			setPos(getExactX(), w.getHeight() - image.getHeight() - 1);
		} else if (getExactY() < 0) { // top
			setPos(getExactX(), 0);
		}
	}

	/**
	 * Writes this Mob's data in order:
	 * 
	 * <ul>
	 * <li>animationTimer int</li>
	 * <li>animation.frame int</li>
	 * </ul>
	 */
	@Override
	public void save(DataOutputStream out) {
		super.save(out);
		try {
			out.writeInt(animationTimer);
			out.writeInt(animation.getFrame());
		} catch (Exception e) {
			Err.error("Player can't save data!");
			e.printStackTrace();
		}
	}

	/**
	 * Loads a Mob's saved data.
	 * 
	 * @see #save(DataOutputStream)
	 */
	@Override
	public void load(DataInputStream in, String version) {
		super.load(in, version);
		try {
			animationTimer = in.readInt();
			animation.setFrame(in.readInt());
		} catch (Exception e) {
			Err.error("Player can't load data!");
			e.printStackTrace();
		}
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle((int) getExactX() + hitbox.x, (int) getExactY() + hitbox.y, hitbox.width, hitbox.height);
	}
}
