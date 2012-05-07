====
package com.scoreloop.client.android.core.controller

----
ActivitiesController.java

Added:
public void loadActivitiesForGame(Game game)

----
GameController.java (Added)

Added:
public GameController(final Session session, final RequestControllerObserver observer)
public GameController(final RequestControllerObserver observer)
public Game getGame()
public void loadGame()
public void setGame(final Entity gameEntity)

----
RequestControllerException.java (Added)

Added:
public static final int	CODE_BUDDY_ADD_REQUEST_ALREADY_ADDED
public static final int	CODE_BUDDY_REMOVE_REQUEST_ALREADY_REMOVED
public static final int	CODE_SOCIAL_PROVIDER_DISCONNECTED
public static final int	CODE_UNDEFINED
public static final int	DETAIL_USER_UPDATE_REQUEST_EMAIL_TAKEN
public static final int	DETAIL_USER_UPDATE_REQUEST_IMAGE_TOO_LARGE
public static final int	DETAIL_USER_UPDATE_REQUEST_INVALID_EMAIL
public static final int	DETAIL_USER_UPDATE_REQUEST_INVALID_USERNAME
public static final int	DETAIL_USER_UPDATE_REQUEST_UNSUPPORTED_MIME_TYPE
public static final int	DETAIL_USER_UPDATE_REQUEST_USERNAME_TAKEN
public static final int	DETAIL_USER_UPDATE_REQUEST_USERNAME_TOO_SHORT
public static final String INFO_KEY_DISCONNECTED_PROVIDER_IDENTIFIERS

public int getErrorCode()
public String getErrorMessage()
public Map<String, Object> getUserInfo()
public boolean hasDetail(final int aDetail)

----
ScoreController.java

Added:
public ScoreController(final Session aSession, final RequestControllerObserver observer, final boolean isQueueing)
public ScoreController(final RequestControllerObserver observer, final boolean isQueueing)

----
UserController.java

Deprecated:
public UserController(final Session aSession, final UserControllerObserver observer)
public UserController(final UserControllerObserver observer)

Added:
public UserController(final Session aSession, final RequestControllerObserver observer) 
public UserController(final RequestControllerObserver observer)

====
package com.scoreloop.client.android.core.model

----
Activity.java

Added:
public String getTimeAgo()

----
Ranking.java

Added:
public Score getScore() 

----
User.java

Removed:
public void setMimeType(String mimeType)

Added:
public void setImageSource(String imageSource)
public void setImageMimeType(String mimeType)

====eof