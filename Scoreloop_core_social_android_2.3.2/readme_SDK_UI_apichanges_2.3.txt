====
package com.scoreloop.client.android.ui

----
LeaderboardsScreenActivity.java

Added:
public static final int	LEADERBOARD_LOCAL

----
OnScoreSubmitObserver.java

Added:
public static final int	STATUS_SUCCESS_LOCAL_SCORE

----
ScoreloopManager.java

Added:
void submitLocalScores(Runnable continuation)
String[] getModeNames()

Modified:
from void loadAchievements(boolean forceInitialSync, Runnable continuation)
  to void loadAchievements(Runnable continuation)

from void onGamePlayEnded(Score score)
  to void onGamePlayEnded(Score score, Boolean submitLocallyOnly)

----
ScoreloopManagerSingleton.java

Modified:
from public static ScoreloopManager init(final Context context)
  to public static ScoreloopManager init(final Context context, String gameSecret)

====eof