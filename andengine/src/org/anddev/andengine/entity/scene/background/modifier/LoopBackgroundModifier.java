package org.anddev.andengine.entity.scene.background.modifier;

import org.anddev.andengine.entity.scene.background.IBackground;
import org.anddev.andengine.util.modifier.LoopModifier;

/**
 * @author Nicolas Gramlich
 * @since 15:03:53 - 03.09.2010
 */
public class LoopBackgroundModifier extends LoopModifier<IBackground> implements IBackgroundModifier {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	public LoopBackgroundModifier(final IBackgroundModifier pBackgroundModifier) {
		super(pBackgroundModifier);
	}

	public LoopBackgroundModifier(final IBackgroundModifier pBackgroundModifier, final int pLoopCount) {
		super(pBackgroundModifier, pLoopCount);
	}

	public LoopBackgroundModifier(final IBackgroundModifier pBackgroundModifier, final int pLoopCount, final ILoopBackgroundModifierListener pLoopModifierListener) {
		super(pBackgroundModifier, pLoopCount, pLoopModifierListener, (IBackgroundModifierListener)null);
	}

	public LoopBackgroundModifier(final IBackgroundModifier pBackgroundModifier, final int pLoopCount, final IBackgroundModifierListener pBackgroundModifierListener) {
		super(pBackgroundModifier, pLoopCount, pBackgroundModifierListener);
	}

	public LoopBackgroundModifier(final IBackgroundModifier pBackgroundModifier, final int pLoopCount, final ILoopBackgroundModifierListener pLoopModifierListener, final IBackgroundModifierListener pBackgroundModifierListener) {
		super(pBackgroundModifier, pLoopCount, pLoopModifierListener, pBackgroundModifierListener);
	}

	protected LoopBackgroundModifier(final LoopBackgroundModifier pLoopBackgroundModifier) throws CloneNotSupportedException {
		super(pLoopBackgroundModifier);
	}

	@Override
	public LoopBackgroundModifier clone() throws CloneNotSupportedException {
		return new LoopBackgroundModifier(this);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public interface ILoopBackgroundModifierListener extends ILoopModifierListener<IBackground> {
		// ===========================================================
		// Final Fields
		// ===========================================================

		// ===========================================================
		// Methods
		// ===========================================================
	}
}
