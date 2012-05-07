====
package com.scoreloop.client.android.core.controller

 Removed Classes:
	UserControllerObserver
	ChallengeControllerObserver

 Added Classes:
	PaymentProviderController
	TermsOfServiceController
	TermsOfServiceControlllerObserver
	GameItemController
	GameItemsController
	PaymentController
	PaymentMethodsController
	PaymentProviderController
	PendingPaymentProcessor
	NetworkException
	TermsOfServiceException

 Changed Classes:
	UserController
	AchievementsController
		Removed:
			public boolean hadInitialSync()
		Added:
			public static com.scoreloop.client.android.core.model.AwardList getLocalAwardList(Context context, java.lang.String gameIdentifier, java.lang.String variantOrNull)
			public void checkHadInitialSync(com.scoreloop.client.android.core.model.Continuation continuation)
	ChallengeController
		Removed:
			public ChallengeController(com.scoreloop.client.android.core.model.Session aSession, ChallengeControllerObserver observer)
		Added:
			ChallengeController(RequestControllerObserver observer)
	RankingController
		Added:
			public java.util.Comparator getLocalScoreComparator()
			public void setLocalScoreComparator(java.util.Comparator comparator)
	RequestControllerException
		Added:
			public int getStatusCode()
			public static final int CHALLENGE_INSUFFICIENT_BALANCE
			public static final int CHALLENGE_ALREADY_ASSIGNED_TO_SOMEONE
			public static final int CHALLENGE_ALREADY_ASSIGNED_TO_YOU
			public static final int CHALLENGE_CANNOT_ACCEPT_CHALLENGE
			public static final int CHALLENGE_CANNOT_REJECT_CHALLENGE
	ScoresController
		Added:
			void loadLocalScoresToSubmit()
			public void removeLocalScores()
	UserController
		Removed:
			public UserController(final Session aSession, final UserControllerObserver observer)
			public UserController(final UserControllerObserver observer)
	UsersController:
		Removed:
			public static final LoginSearchOperator LIKE

====
package com.scoreloop.client.android.core.model

 Removed Classes:
 	ScoreComparator
 
 Added Classes:
 	GameItem
 	MoneyFormatter
 	Payment
 	PaymentMethod
 	PaymentProvider
 	Price
 	TermsOfService

 Changed Classes:
	Award
		Removed:
			public AwardList getAwardList()
		Added:
			public AwardCollection getAwardCollection()
			public java.lang.String getAchievedImageURL()
			public java.lang.String getUnachievedImageURL()
	Client
		Added:
			public static void init(Context anApplication, java.lang.String aSecret, ClientObserver anObserver)
			public void destroy()
			public static java.lang.String getSDKVersion()
			public void sessionDidAskUserForTermsOfService(com.scoreloop.client.android.core.model.Session session, java.lang.Boolean accepted)
			public boolean sessionShouldAskUserForTermsOfService(com.scoreloop.client.android.core.model.Session session)
	ClientObserver
		Added:
			public void clientDidAskUserForTermsOfService(com.scoreloop.client.android.core.model.Client client, java.lang.Boolean accepted)
			public boolean clientShouldAskUserForTermsOfService(com.scoreloop.client.android.core.model.Client client)
	Game
		Added:
			public ScoreOrdering getScoreOrdering()
			public void setScoreOrdering(ScoreOrdering scoreOrdering)
	Money
		Added:
			public BigDecimal getAmountInUnits()
	Score
		Added:
			public boolean hasMode()
	ScoreFormatter
		Added:
			public static java.lang.String format(Score score)
			public static java.lang.String format(Score score, ScoreFormatKey formatKey)
			public static ScoreFormatter getDefaultScoreFormatter()
			public static void setDefaultScoreFormatter(ScoreFormatter formatter)
	Session
		Removed:
			public String getPaymentUrl()
		Added:
			public TermsOfService getUsersTermsOfService()
	SessionObserver
		Added:
			public void sessionDidAskUserForTermsOfService(Session session, java.lang.Boolean accepted)
			public boolean sessionShouldAskUserForTermsOfService(Session session)
	User
		Added:
			public void assignImage(final Image image, final Continuation<Boolean> contination)
			public void setNationality(final String nationality)

====
package com.scoreloop.client.android.core.ui
  
 Removed Classes:
  	PaymentDialog

 Added Classes:
 	ProxyActivity

====
package com.scoreloop.client.android.core.addon

 Changed Classes:
 	RSSFeed
 		Removed:
 			interface Continuation
 			public boolean requestAllItems(com.scoreloop.client.android.core.addon.RSSFeed.Continuation)
 		Added:
 			class RequestNextItemCanceledException
 			class State
 			public boolean requestAllItems(com.scoreloop.client.android.core.model.Continuation)
 	RSSItem
 		Added:
 			public java.util.Date getPubDate()

