package com.embed.candy.engine;

import static com.embed.candy.constants.EngineConstants.LASER_CROSS;
import static com.embed.candy.constants.EngineConstants.LASER_HORIZONTAL;
import static com.embed.candy.constants.EngineConstants.LASER_VERTICAL;
import static com.embed.candy.constants.EngineConstants.PIPE_LEFT;
import static com.embed.candy.constants.EngineConstants.PIPE_LEFT_ICE;
import static com.embed.candy.constants.EngineConstants.PIPE_RIGHT;
import static com.embed.candy.constants.EngineConstants.PIPE_RIGHT_ICE;

class Conditionals {

	static boolean isLaser(final int type) {
		switch (type) {
		case LASER_HORIZONTAL:
		case LASER_VERTICAL:
		case LASER_CROSS:
			return true;
		default:
			return false;
		}
	}

	static boolean isPipe(final int type) {
		switch (type) {
		case PIPE_LEFT:
		case PIPE_RIGHT:
		case PIPE_LEFT_ICE:
		case PIPE_RIGHT_ICE:
			return true;
		default:
			return false;
		}
	}

	/**
	 * A master conditional statement.
	 */

	static boolean condition(final int row, final int column, final int rowDirection, final int columnDirection) {
		final boolean condition;
		if (rowDirection == -1) {
			condition = (row == 0);
		} else if (rowDirection == 1) {
			condition = (row == 17);
		} else if (columnDirection == -1) {
			condition = (column == 0);
		} else {
			condition = (column == 23);
		}
		return condition;
	}
}