----
Achievement.java (Added)

Added:
public Award getAward()
public Date getDate()
public String getIdentifier()
public Bitmap getImage()
public int getValue()
public boolean isAchieved()
public boolean needsSubmit() 
public void setAchieved()
public void setValue(final int value)

----
AchievementController.java (Added)

Added:
public AchievementController(final RequestControllerObserver observer)
public AchievementController(final Session aSession, final RequestControllerObserver observer)
public Achievement getAchievement()
public void setAchievement(final Achievement anAchievement)
public void submitAchievement()

----
AchievementsController.java (Added)

Added:
public AchievementsController(final RequestControllerObserver observer)
public AchievementsController(final Session aSession, final RequestControllerObserver observer)
public int countAchievedAwards()
public Achievement getAchievementForAwardIdentifier(final String awardIdentifier)
public List<Achievement> getAchievements()
public AwardList getAwardList()
public User getUser()
public void loadAchievements()
public void setUser(final User user)

----
ActivitiesController.java

Added:
public void loadActivitiesForUser(User user)

----
Award.java (Added)

Added:
public Bitmap getAchievedImage()
public int getAchievingValue()
public Range getCounterRange()
public int getInitialValue()
public String getLocalizedDescription()
public String getLocalizedTitle()
public Money getRewardMoney()
public Bitmap getUnachievedImage()
public boolean isAchievedByValue(final int aValue)
public boolean isValidCounterValue(final int aValue)

----
AwardList.java (Added)

Added:
public List<Award> getAwards()
public Award getAwardWithIdentifier(final String anIdentifier)

----
ChallengeControllerObserver.java

Modified:
from public void onCannotAcceptChallenge(ChallengeController challengeController) 
  to challengeControllerDidFailToAcceptChallenge(ChallengeController challengeController)
from public void onCannotRejectChallenge(ChallengeController challengeController)
  to public void challengeControllerDidFailToRejectChallenge(ChallengeController challengeController)
from public void onInsufficientBalance(ChallengeController challengeController)
  to public void challengeControllerDidFailOnInsufficientBalance(ChallengeController challengeController)

----
Game.java

Added:
public static final String CHARACTERISTIC_AGILITY
public static final String CHARACTERISTIC_STRATEGY
public static final String CHARACTERISTIC_KNOWLEDGE
public String getCharacteristic()
public String getDescription()
public String getDownloadUrl()
public String getImageUrl()
public String getPublisherName()

----
GamesController.java (Added)

Added:
public GamesController(final Session aSession, final RequestControllerObserver observer)
public GamesController(final RequestControllerObserver observer)
public List<Game> getGames()
public boolean getLoadsDevicesPlatformOnly()
public int getRangeLength()
public boolean hasNextRange()
public boolean hasPreviousRange()
public void loadNextRange()
public void loadPreviousRange()
public void loadRangeForFeatured()
public void loadRangeForPopular()
public void loadRangeForNew()
public void loadRangeForBuddies()
public void loadRangeForUser(final User user)
public void setRangeLength(final int rangeLength)
public void setLoadsDevicesPlatformOnly(final boolean loadsDevicesPlatformOnly)

----
MessageController.java

Removed:
public int countReceivers()
public void removeAllReceiversOfType(Class<?> receiverType) 

Added:
public static final Object EMAIL_RECEIVER

Modified:
from public static final String MessageControllerInvitationTarget
  to public static final Object INVITATION_TARGET

----
Ranking.java

Added:
public Integer getTotal()

----
Session.java

Added:
public String getScoreloopAppDownloadUrl()
public String getPaymentUrl() 

----
SocialProviderControllerObserver.java

Modified:
from public void didFail(SocialProviderController controller, Throwable error)
  to public void socialProviderControllerDidFail(SocialProviderController controller, Throwable error)
from public void didEnterInvalidCredentials(SocialProviderController controller)
  to public void socialProviderControllerDidEnterInvalidCredentials(SocialProviderController controller)
from public void didSucceed(SocialProviderController controller)
  to public void socialProviderControllerDidSucceed(SocialProviderController controller)
from public void userDidCancel(SocialProviderController controller)
  to public void socialProviderControllerDidCancel(SocialProviderController controller)

----
UserControllerObserver.java

Modified:
from public void onEmailAlreadyTaken(UserController controller)
  to public void userControllerDidFailOnEmailAlreadyTaken(UserController controller)
from public void onEmailInvalidFormat(UserController controller)
  to public void userControllerDidFailOnInvalidEmailFormat(UserController controller)
from public void onUsernameAlreadyTaken(UserController controller)
  to public void userControllerDidFailOnUsernameAlreadyTaken(UserController controller)

----
UsersController.java

Added:
public boolean isMaxUserCount()
public void loadBuddiesForGame(Game aGame)

----eof