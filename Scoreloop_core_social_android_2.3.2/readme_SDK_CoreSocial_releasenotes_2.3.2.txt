+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
+                                                       +
+       CoreSocial SDK for Android                      +
+       Version: 2.3.2                                  +
+                                                       +
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++


Scope and Status:
* 2.3.2: ScoreloopCore: fixed exception in case of missing android id
* 2.3.2: SLDemoUI & SLDemoCore: added proguard configuration
* 2.3.2: ScoreloopCore: fixed missing user object in local score storage
* 2.3.2: ScoreloopUI: fixed list background glitches on some devices

History:
* 2.3.1: ScoreloopCore: fixed exception in case no game modes are defined
* 2.3.1: ScoreloopCore: improved handling of non-unique android id
* 2.3: ScoreloopCore: added support for local offline scores
* 2.3: ScoreloopCore: extended offline achievements
* 2.3: ScoreloopUI: extended Score formatting options (eg. better UI support for time-based scores / mm:ss)
* 2.3: several fixes and improvements, including
       - game secret handling
       - game mode related problems in the leaderboard screen
* 2.2: ScoreloopUI: extended API for submit of Score objects incl level, minor result
* 2.2: ScoreloopUI: added explicit UI for account merge / transfer
* 2.2: ScoreloopCore: extended support for offline Achievements
* 2.2: ScoreloopCore: added Activity images
* 2.2: several fixes and improvements, including
       - ScoreloopUI: PostScoreOverlay doesn't fail anymore on queued scores
       - ScoreloopUI: fixed rare failure of paging in Leaderboards
       - ScoreloopCore: fixed user context handling
       - ScoreloopCore: fixed score mode handling
* 2.1: ScoreloopUI: improved layouts in landscape mode for large displays
* 2.1: ScoreloopUI: improved handling of changes to the display orientation
* 2.1: ScoreloopCore: improved Facebook connect, used for buddy and profile image import, and posting of scores and achievements
* 2.1: ScoreloopCore: added smart request caching for better performance
* 2.1: ScoreloopCore: added experimental queuing of submitted scores in offline situations
* 2.1: ScoreloopCore: added support for Android devices without a SIM card / IMEI
* 2.1: several fixes and improvements
* 2.0.1: adjusted SDK packaging tool for better compatibility with WinRAR
* 2.0.1: ScoreloopUI: re-enabled official support for Android 1.5
* 2.0.1: ScoreloopUI: enhanced support for command line builds
* 2.0.1: ScoreloopUI: fixed issue when game activity uses launchModes to singleTask/singleTop
* 2.0.1: ScoreloopUI.Doc: clarified section about Project Setup
* 2.0.1: ScoreloopCore: enhanced support for resuming after long pause
* 2.0: CoreUI has been renamed to ScoreloopUI
* 2.0: in ScoreloopUI: updated the UI
* 2.0: in ScoreloopUI: added features exposed via ScoreloopUI for easy integration: Achievements, Challenges, News, Virtual Currency Purchases
* 2.0: in ScoreloopUI: updated Social Market for game discovery and promotion
* 2.0: in ScoreloopUI: added user profile picture support
* 2.0: in ScoreloopUI: updated the user profile to show social data
* 2.0: in core library: added RSS feeds
* 2.0: in core library: added image support for Game and User
* 2.0: in core library: added global statistics (buddies, games, achievements)
* 2.0: several fixes and improvements
* 1.1: in coreUI: added screens for game discovery
* 1.1: in coreUI: added screens for user details
* 1.1: in SLDemoCore: added Achievements reference code
* 1.1: in SLDemoCore: improved Challenges reference code
* 1.1: in core library: extended GamesController and Games for better game discovery functionality
* 1.1: in core library: added user-specific Activities
* 1.1: in core library: added Achievements; see Achievement, Award, AwardList, AchievementsController, AchievementController
* 1.1: in core library: API simplified and streamlined; for details please refer to readme_SDK_CORE_apichanges_1.1.txt
* 1.1: several fixes and improvements
* 1.0.1: in core library: bug fix in ChallengeController.submitChallenge()
* 1.0.1: in brandingAssets: updated graphics to match the coreUI look from release 1.0
* 1.0: coreUI: updated look & feel
* 1.0: coreUI: added score posting to social networks
* 1.0: coreUI: added friends management incl. finding friends from several social networks
* 1.0: in core library: activities added to update the player about what friends and other players of the same game are doing
* 1.0: in core library: API simplified and streamlined; for details please refer to readme_SDK_CORE_apichanges_1.0.txt
* 1.0: several fixes and improvements
* 0.91: coreUI: added
* 0.91: in core library: added social network connectivity to Twitter
* 0.91: in core library: added support for game-specific currency
* 0.91: in core library: changed Client to now take Context as parameter, no need to call Device.setContext() any longer
* 0.91: several fixes and improvements
* 0.9: in core library: social network connectivity to MySpace and Facebook added (see SocialProvider* and MessageController)
* 0.9: in core library: buddy support added (see UserController & User)
* 0.9: in core library: finding other users via email, login, etc added (see UsersController)
* 0.9: in core library: challenge life-cyle support and challenge score submit improved (see ChallengeController & ScoreController)
* 0.9: in core library: session-less controller constructors added
* 0.9: in core library: many new properties added (see model classes)
* 0.9: in SLDemoCore: many new core library features used
* 0.9: in SLDemoCore: code in many areas streamlined
* 0.81.Payment: added method for payment url retrieval to Session, for coin purchase
* 0.81: search lists for scores
* 0.81: mode and level support
* 0.81: RankingController
* 0.81: accept & reject added to challenge life cycle
* 0.81: several additions, fixes and improvements
* 0.8: in core library: automatic session authentication introduced
* 0.8: in core library: request cancelation behavior updated
* 0.8: in core library: API documentation added
* 0.8: in BugLanding: new core library features used
* 0.8: in BugLanding: code in many areas streamlined
