package com.embed.candy.swarmservice;

import java.util.Map;

import com.embed.candy.MainMenuActivity;
import com.swarmconnect.SwarmAchievement;
import com.swarmconnect.SwarmAchievement.GotAchievementsMapCB;
import com.swarmconnect.SwarmActiveUser;
import com.swarmconnect.delegates.SwarmLoginListener;

public final class CandySwarmListener implements SwarmLoginListener {
	@Override
	public void loginStarted() {}

	@Override
	public void loginCanceled() {}

	@Override
	public void userLoggedIn(final SwarmActiveUser user) {
		 SwarmAchievement.getAchievementsMap(new GotAchievementsMapCB() {
				@Override
				public void gotMap(final Map<Integer, SwarmAchievement> achievementsMap) {
		            MainMenuActivity.achievements = achievementsMap;
		        }
		    });
		}

	@Override
	public void userLoggedOut() {}
}