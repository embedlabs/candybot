====
package com.scoreloop.client.android.core.controller

----
AchievementsController.java

Added:
public boolean getForceInitialSync()
public boolean hadInitialSync()
public void setForceInitialSync(final boolean forceInitialSync)

----
ActivitiesController.java

Added:
public void loadCommunityActivities()

====
package com.scoreloop.client.android.core.model

----
Activity.java

Added:
public String getImageUrl(ImageSize imageSize)
public enum ImageSize

----
Client.java

Added:
public String getInfoString()

----
Session.java

Added:
public boolean isOwnedByUser(User user)

====eof