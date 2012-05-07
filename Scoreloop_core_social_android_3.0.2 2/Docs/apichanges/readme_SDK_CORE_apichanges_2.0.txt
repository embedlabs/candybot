====
package com.scoreloop.client.android.core.addon

----
RSSFeed.java (Added)

Added:
public static final class class ChainedPolicy
public ChainedPolicy.ChainedPolicy(final Policy ...policies)
public static interface Continuation
void Continuation.withLoadedFeed(List<RSSItem> feed, Exception failure)
public static interface Policy
void Policy.collectItems(RSSFeed controller, List<RSSItem> feed, List<RSSItem> collector)
public static final class RequestNextItemCanceledException
public enum State
State.IDLE
State.PENDING
public static final Policy STANDARD_POLICY
public static final Policy STICKY_POLICY
public static final Policy UNREAD_POLICY
public static final Policy ON_START_POLICY
public RSSFeed(final Session session, final RSSFeedObserver observer)
public RSSFeed(final RSSFeedObserver observer)
public void cancelRequestNextItem()
public RSSItem getDefaultItem()
public RSSItem getLastItem()
public State getState()
public boolean hasSessionReadFlag(final RSSItem item)
public boolean isUnread(final RSSItem item)
public void reloadOnNextRequest()
public void requestNextItem(final Policy policy)
public boolean requestAllItems(final Continuation continuation, final boolean markAsRead, final Policy policy) 
public void setDefaultItem(final RSSItem item)

----
RSSFeedObserver (Added)

Added:
void feedDidFailToReceiveNextItem(RSSFeed feed, Exception exception)
void feedDidReceiveNextItem(RSSFeed feed, RSSItem itemOrNull)
void feedDidRequestNextItem(RSSFeed feed)

----
RSSItem (Added)

Added:
public static void resetPersistentReadFlags(final Context context)
public String getDescription()
public String getIdentifier()
public String getImageUrlString()
public String getLinkUrlString()
public String getTitle()
public boolean hasPersistentReadFlag()
public boolean isSticky()
public void setHasPersistentReadFlag(boolean isRead)

====
package com.scoreloop.client.android.core.controller

----
ChallengesController.java

Removed:
public void setMode(Integer aMode)
public Integer getMode()

Added:
public User getUser()
public void setUser(User user)

----
GamesController.java

Added:
public void loadFirstRange()

----
MessageController.java

Added:
public static final Object RECEIVER_SYSTEM
public static final Object RECEIVER_USER
public static final int	TYPE_ABUSE_REPORT
public static final int TYPE_RECOMMENDATION
public static final int	TYPE_TARGET_INFERRED
public int getMessageType()
public void setMessageType(final int messageType)

Modified:
from public static final Object EMAIL_RECEIVER
  to public static final Object	RECEIVER_EMAIL 	

from public void addReceiverWithUsers(Object receiver, List<User> users)
  to public void addReceiverWithUsers(final Object receiver, final Object... users)

----
UserController.java

Removed:
public void loadUserWithIdentifier(String anIdentifier)

Modified:
from public void setUser(final User aUser)
  to public void setUser(final Entity aUser)

----
UsersController.java

Removed:
public void loadBuddiesForGame(Game aGame)

Added:
public void loadRecommendedBuddies(int count)
public void loadBuddies(User aUser, Game aGame)

====
package com.scoreloop.client.android.core.model

----
Activity.java

Added:
public Entity getUser()
public Entity getGame()

----
Award.java

Added:
public String getIdentifier()

----
AwardList.java

Added:
public List<String> getAwardIdentifiers()

----
Client.java

Added:
public Client(final Context anApplication, final ClientObserver anObserver)
public static Properties getProperties(Context aContext)
scoreloop.properties game.id
scoreloop.properties game.version
scoreloop.properties game.name
scoreloop.properties game.mode.min
scoreloop.properties game.mode.max

----
Entity.java (Added)

Refactored:
String getIdentifier() pulled up from subclasses

----
Game.java

Removed:
public static final String CHARACTERISTIC_AGILITY
public static final String CHARACTERISTIC_STRATEGY
public static final String CHARACTERISTIC_KNOWLEDGE
public String getCharacteristic()
public void setMaxLevel(final Integer maxLevel)
public void setMaxMode(final Integer maxMode)
public void setMinLevel(final Integer minLevel)
public void setMinMode(final Integer minMode)
public void setName(final String name)
public void setVersion(final String version)

Added:
public String[] getPackageNames()

----
ImageSource.java (Added)

Added:
public static final ImageSource IMAGE_SOURCE_DEFAULT
public static final ImageSource IMAGE_SOURCE_SCORELOOP

----
Money.java

Added:
public static String getApplicationCurrencyNameSingular()
public static String getApplicationCurrencyNamePlural()

----
Score.java

Removed:
public Date getUpdatedAt()

----
User.java

Added:
public Integer getBuddiesCounter()
public Integer getGamesCounter()
public Integer getGlobalAchievementsCounter()
public ImageSource getImageSource()
public boolean ownsSession(Session session)
public void setImageSource(ImageSource imageSource)
public void setImageData(String data)
public void setMimeType(String mimeType)

====eof
