----
Activity.java (Added)

Added:
public Date getDate()
public String getMessage()

----
ActivitiesController.java (Added)

Added:
public ActivitiesController(final RequestControllerObserver observer)
public ActivitiesController(final Session aSession, final RequestControllerObserver observer)
public List<Activity> getActivities()
public void loadBuddyActivities()
public void loadGameActivities()

----
Challenge.java

Removed:
public void setCompletedAt(final Date completedAt)
public void setCreatedAt(final Date createdAt)
public void setContenderSkill(final Integer contenderSkill)
public void setContestantSkill(final Integer contestantSkill)

Added:
public boolean isPlayableForUser(final User user)

Modified:
from public Money getPrice() to public Money getPrize()

----
ChallengesController.java

Removed:
public Challenge getChallengeForIndex(final int aIndex)

Modified:
from public void requestChallengeHistory() to public void loadChallengeHistory()
from public void requestOpenChallenges() to public void loadOpenChallenges()

----
ClientObserver.java

Modified:
from com.scoreloop.client.android.core.ClientObserver to package com.scoreloop.client.android.core.model.ClientObserver

----
FacebookSocialProvider.java (Removed)

Removed:
public static final String IDENTIFIER

----
MessageController.java

Added:
public static final String MessageControllerInvitationTarget

Modified:
from public boolean isPostAllowed() to public boolean isSubmitAllowed() 
from public void postMessage() to public void submitMessage()

----
MySpaceSocialProvider.java (Removed)

Removed:
public static final String IDENTIFIER

----
RankingController.java

Modified:
from public void requestRankingForScore(final Score score) to public void loadRankingForScore(final Score score)
from public void requestRankingForScoreResult(final Double aScore, final Map<String, Object> aContext) to public void loadRankingForScoreResult(final Double aScore, final Map<String, Object> aContext)
from public void requestRankingForUserInGameMode(final User user, final int mode) to public void loadRankingForUserInGameMode(final User user, final Integer mode)

----
Score.java

Modified:
from public Score(final Double aResult, final Map<String, Object> aContext, final User aUser) to public Score(final Double aResult, final Map<String, Object> aContext)

----
ScoreController.java

Removed:
public void submitScoreResult(final Double aScore, final Map<String, Object> aContext)

----
ScoresController.java

Removed:
public SearchList getBuddiesSearchList()
public SearchList getDefaultSearchList()
public SearchList getGlobalSearchList()
public SearchList getTwentyFourHourSearchList()
public SearchList getUserCountrySearchList()
public Range getLoadedRange() 
public Range getRange()
public Score getScoreForIndex(final int aIndex)
public void setRange(final Range aRange)
public void loadRange()

Added:
public void loadRangeAtRank(final int rank)

----
SearchList.java

Added:
public static SearchList getUserNationalityScoreSearchList()

Modified:
from public static SearchList buddiesScoreSearchList() to public static SearchList getBuddiesScoreSearchList()
from public static SearchList defaultScoreSearchList() to public static SearchList getDefaultScoreSearchList()
from public static SearchList globalScoreSearchList() to public static SearchList getGlobalScoreSearchList()
from public static SearchList localScoreSearchList() to public static SearchList getLocalScoreSearchList()
from public static SearchList twentyFourHourScoreSearchList() to public static SearchList getTwentyFourHourScoreSearchList()
from public static SearchList userCountryScoreSearchList() to public static SearchList getUserCountryLocationScoreSearchList()

----
SessionObserver.java

Modified:
from com.scoreloop.client.android.core.SessionObserver to package com.scoreloop.client.android.core.model.SessionObserver

----
SocialProvider.java

Added:
public static final String FACEBOOK_IDENTIFIER
public static final String MYSPACE_IDENTIFIER
public static final String TWITTER_IDENTIFIER
public static List<SocialProvider> getSupportedProviders() 

----
SocialProviderController.java

Added:
public SocialProvider getSocialProvider()

----
SocialProviderControllerObserver.java

Modified:
from public void didFail(Throwable error) to public void didFail(SocialProviderController controller, Throwable error)
from public void didEnterInvalidCredentials() to public void didEnterInvalidCredentials(SocialProviderController controller)
from public void didSucceed() to public void didSucceed(SocialProviderController controller)
from public void userDidCancel() to public void userDidCancel(SocialProviderController controller)

----
TwitterSocialProvider.java (Removed)

Removed:
public static final String IDENTIFIER

----
User.java

Removed:
public Score getLastScore()

Added:
public Activity getLastActivity() 

----
UserController.java

Modified:
from public void requestUserContext() to public void loadUserContext()
from public void updateUserContext() to public void submitUserContext()
from public void requestUser() to public void loadUser()
from public void requestUserWithIdentifier(String anIdentifier) to public void loadUserWithIdentifier(String anIdentifier)
from public void requestBuddies() to public void loadBuddies()
from public void requestUserDetail() to public void loadUserDetail()
from public void updateUser() to public void submitUser()

----
UsersController.java

Removed:
public void setRange(Range aRange)
public Range getRange()
public void loadRange()
public User getUserForIndex(int anIndex)

Added:

Modified:
from public void setLimit(Integer aLimit) to public void setSearchLimit(int searchLimit)
from public Integer getLimit() to public int getSearchLimit()
from public void searchByLocalAddressBook(Context aContext) to public void searchByLocalAddressBook()

----eof