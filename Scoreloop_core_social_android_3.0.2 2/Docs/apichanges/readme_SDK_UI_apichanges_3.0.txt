====
package com.scoreloop.client.android.ui

 Added Classes:
	GameItemsScreenActivity
	PaymentScreenActivity
	OnPaymentChangedObserver

 Changed Classes:
	ScoreloopManager
		Removed:
			public void loadAchievements(Runnable continuation)
		Added:
			public void loadAchievements(Continuation<Boolean> continuation)
			public void setOnPaymentChangedObserver(OnPaymentChangedObserver observer)
			public String[] getModeNames()
			public void getGameItemDownloadUrl(String gameItemIdentifier, Continuation<String> continuation)
			public void wasGameItemPurchasedBefore(String gameItemIdentifier, Continuation<Boolean> continuation)
			public Boolean hasPendingPaymentForGameItemWithIdentifier(String identifier)
 			public void setAllowToAskUserToAcceptTermsOfService(boolean ask)
			public void askUserToAcceptTermsOfService(Activity activity, Continuation<Boolean> continuation)
			public boolean userRejectedTermsOfService(Continuation<Boolean> notification)

====eof