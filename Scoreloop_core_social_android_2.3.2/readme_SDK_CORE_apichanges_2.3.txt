====
package com.scoreloop.client.android.core.controller

----
RequestController.java (Added)

public Exception getError()
public boolean isCachedResponseUsed()
public void setCachedResponseUsed(final boolean useCache)

----
RequestQueuedException.java (Removed)

----
ScoreController.java

Removed:
public ScoreController(final Session aSession, RequestControllerObserver observer, boolean isQueueing)
public ScoreController(RequestControllerObserver observer, boolean isQueueing)

Added:
public void setShouldSubmitScoreLocally(final boolean submitLocally)
public boolean shouldSubmitScoreLocally()

----
ScoresController.java

Added:
public void setLocalScoreComparator(final Comparator<Score> comparator)
public Comparator<Score> getLocalScoreComparator()
public Score getLocalScoreToSubmit() 

====
package com.scoreloop.client.android.core.model

----
Client.java

Modified:
from Client(final Context anApplication, final ClientObserver anObserver)
  to Client(final Context anApplication, final String aSecret, final ClientObserver anObserver)

----
ScoreComparator.java (Added)

Added:
public ScoreComparator(boolean ascResult, boolean ascMinorResult)

----
ScoreFormatter.java (Added)

Added:
public static enum ScoreFormatKey
public ScoreFormatter(String format)
public String formatScore(Score score)
public String formatScore(Score score, ScoreFormatKey formatKey)
public String[] getDefinedModesNames(int minMode, int modeCount)

----
ScoreSubmitException.java (Added)

Added:
public ScoreSubmitException(String message, Throwable throwable)

----
SearchList.java

Enabled:
public static SearchList getLocalScoreSearchList()

====eof