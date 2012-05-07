/*
 * In derogation of the Scoreloop SDK - License Agreement concluded between
 * Licensor and Licensee, as defined therein, the following conditions shall
 * apply for the source code contained below, whereas apart from that the
 * Scoreloop SDK - License Agreement shall remain unaffected.
 * 
 * Copyright: Scoreloop AG, Germany (Licensor)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.scoreloop.client.android.core.demo.labs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.scoreloop.client.android.core.controller.RankingController;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.ScoreController;
import com.scoreloop.client.android.core.model.Challenge;
import com.scoreloop.client.android.core.model.Game;
import com.scoreloop.client.android.core.model.Score;
import com.scoreloop.client.android.core.model.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtentedGamePlayActivity extends Activity {

    // identifiers for our dialogues
    private static final int DIALOG_PROGRESS = 0;
    private static final int DIALOG_SUBMITTED = 1;
    private static final int DIALOG_FAILED = 2;

    // holds a reference to our score fields
    private TextView levelField;
    private TextView scoreField;
    private TextView timeField;
    private TextView expectedRankField;
    private TextView clicksField;
    private ProgressBar progressBar;
    private ProgressBar progressBarGhostGame;

    // track playing state
    private boolean playing = true;

    // elapsed time as minor result
    private final StopWatch stopWatch = new StopWatch();

    // save game play
    private GamePlay gamePlay = new GamePlay();
    private GamePlay gamePlayContender;

    /** action for gui update */
    private final Runnable actionUpdateTime = new Runnable() {
        @Override
        public void run() {
            timeField.setText(stopWatch.getElapsedFormatted());
            if (isChallenge() && gamePlayContender != null) {
                long elapsed = stopWatch.getElapsed();
                progressBarGhostGame.setProgress(gamePlayContender.getScoreForMillis(elapsed));
            }
            if (stopWatch.isRunning()) {
                // resubmit action to main thread if stop watch is still running
                timeField.postDelayed(actionUpdateTime, 100);
            }
        }
    };

    /** action for ranking update */
    private final Runnable actionUpdateExpectedRank = new Runnable() {
        private RankingController rankingController;
        @Override
        public void run() {
            // instantiate ranking controller just once
            if (rankingController == null) {
                rankingController = new RankingController(new RequestControllerObserver() {
                    @Override
                    public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
                        repostAction();
                    }

                    @Override
                    public void requestControllerDidReceiveResponse(RequestController aRequestController) {
                        // update ranking on gui
                        Integer rank = rankingController.getRanking().getRank();
                        Integer total = rankingController.getRanking().getTotal();
                        expectedRankField.setText(rank + "/" + total);
                        repostAction();
                    }
                });
            }
            // calculation of expected score only possible for current progress greater 0
            if (progressBar.getProgress() != 0) {
                // calculate the expected score
                Score expectedScore = calculateExpectedScore(getScore());
                // load expected ranking from server, see request observer above for success action
                rankingController.loadRankingForScore(expectedScore);
            } else {
                repostAction();
            }
        }

        /** re-post action to main thread if stopwatch ist still running */
        private void repostAction() {
            if (stopWatch.isRunning()) {
                expectedRankField.postDelayed(actionUpdateExpectedRank, 5000);
            }
        }
    };

    // adds some points to the score in the text field
    private void addScore(int points) {
        if (playing) {
            // level is calculated based on which button have been clicked
            Integer newLevel = points == 1 ? 1 : points == 10 ? 2 : 3;
            Integer oldLevel = Integer.valueOf(levelField.getText().toString());
            // use the highest level
            levelField.setText("" + Math.max(newLevel, oldLevel));
            // update number ob button clicks
            Integer clicks = Integer.valueOf(clicksField.getText().toString()) + 1;
            clicksField.setText(clicks.toString());
            // update progress bar
            progressBar.incrementProgressBy(points);
            // update score field
            scoreField.setText("" + Math.min(progressBar.getProgress(), progressBar.getMax()) + "/" + progressBar.getMax());
            // update game play with current time and score
            gamePlay.add(stopWatch.getElapsed(), progressBar.getProgress());
            // end of game is reached when player reached 1000 points
            if (progressBar.getProgress() >= progressBar.getMax()) {
                // stop playing
                playing = false;
                stopWatch.stop();
                // wait until gui update
                progressBar.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // submit score
                            submitScore();
                        } catch (JSONException e) {
                            showDialog(DIALOG_FAILED);
                        }
                    }
                }, 20);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (playing) {
            // start watch and gui update actions
            stopWatch.start();
            actionUpdateTime.run();
            actionUpdateExpectedRank.run();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (playing) {
            // stop watch and gui update actions
            stopWatch.stop();
        }
    }

    /**
     * Called when the activity is first created.
     * Used to set up the listeners on our various buttons
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gameplay);

        // initialize views
        levelField = (TextView) findViewById(R.id.scoreLevel);
        scoreField = (TextView) findViewById(R.id.scoreText);
        timeField = (TextView) findViewById(R.id.scoreTime);
        clicksField = (TextView) findViewById(R.id.scoreClicks);
        expectedRankField = (TextView) findViewById(R.id.expectedRank);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(1000);
        // initialize ghost game views
        progressBarGhostGame = (ProgressBar) findViewById(R.id.progressBarGhostGame);
        progressBarGhostGame.setMax(1000);
        // ghost game views are only visible if this is a accepted challenge
        boolean displayGhostGame = isChallenge() && getChallenge().isAccepted();
        progressBarGhostGame.setVisibility(displayGhostGame ? View.VISIBLE : View.GONE);
        findViewById(R.id.ghostGame).setVisibility(displayGhostGame ? View.VISIBLE : View.GONE);

        // parse game play of contender
        if (isChallenge() && getChallenge().getContext() != null) {
            String gamePlayContenderJson = (String)getChallenge().getContext().get("gamePlayContender");
            if (gamePlayContenderJson != null) {
                try {
                    gamePlayContender = new GamePlay(new JSONArray(gamePlayContenderJson));
                } catch (JSONException e) {
                    throw new IllegalStateException("can not parse json game play: " + gamePlayContenderJson);
                }
            }
        }

        // update description with challenge state
        TextView descriptionView = (TextView)findViewById(R.id.description);
        String description = getString(R.string.description);
        if (isChallenge()) {
            descriptionView.setText(getString(R.string.challenge_game) + " " + description);
        } else {
            descriptionView.setText(description);
        }

        // set up click listeners for score buttons
        findViewById(R.id.button_score1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                addScore(1);
            }
        });
        findViewById(R.id.button_score10).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                addScore(10);
            }
        });
        findViewById(R.id.button_score100).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                addScore(100);
            }
        });
        // reset game state (fields, stop watch, game play, ...)
        resetGame();
    }

    /**
     * reset to default values
     */
    private void resetGame() {
        scoreField.setText("0/" + progressBar.getMax());
        clicksField.setText("0");
        levelField.setText("0");
        progressBar.setProgress(0);
        stopWatch.reset();
        gamePlay = new GamePlay();
        playing = true;
    }


    private void submitScore() throws JSONException {
        // this is where you should input your game's score
        Score score = getScore();

        // set up an observer for our request
        RequestControllerObserver observer = new RequestControllerObserver() {

            @Override
            public void requestControllerDidFail(RequestController controller, Exception exception) {
                // something went wrong... possibly no internet connection
                dismissDialog(DIALOG_PROGRESS);
                showDialog(DIALOG_FAILED);
            }

            // this method is called when the request succeeds
            @Override
            public void requestControllerDidReceiveResponse(RequestController controller) {
                // remove the progress dialog
                dismissDialog(DIALOG_PROGRESS);
                // show the success dialog
                showDialog(DIALOG_SUBMITTED);
                // alternatively, you may want to return to the main screen
                // or start another round of the game at this point
                // return to the main screen
            }
        };

        // with the observer, we can create a ScoreController to submit the score
        ScoreController scoreController = new ScoreController(observer);

        // show a progress dialog while we are submitting
        showDialog(DIALOG_PROGRESS);

        if (isChallenge()) {
            // mark as challenge score
            scoreController.setShouldSubmitScoreForChallenge(true);

            Map<String, Object> context = getChallenge().getContext();
            if (context == null) {
                context = new HashMap<String, Object>();
                getChallenge().setContext(context);
            }

            // add contender game play to challenge if challenge is in state created
            if (getChallenge().isCreated()) {
                context.put("gamePlayContender", gamePlay.toJSONArray().toString());
            }
            // add contestant game play to challenge if challenge is in state accepted
            // might be used for game replay
            if (getChallenge().isAccepted()) {
                context.put("gamePlayContestant", gamePlay.toJSONArray().toString());
            }
        }

        // this is the call that submits the score
        scoreController.submitScore(score);
        // please note that the above method will return immediately and reports to
        // the RequestControllerObserver when it's done/failed
    }

    private Score getScore() {
        // read score from text fields
        Integer level = Integer.valueOf(levelField.getText().toString());
        Double minorResult = Double.valueOf(clicksField.getText().toString());
        Double scoreResult = stopWatch.getElapsed() / 1000.0;

        // create the score
        Score score = new Score(scoreResult, null);
        score.setMinorResult(minorResult);
        score.setLevel(level);
        score.setMode(0);
        return score;
    }

    /**
     * calculate the estimated final score at the end of the game
     */
    private Score calculateExpectedScore(Score current) {
        double percentFinished = (double) progressBar.getProgress() / progressBar.getMax();

        HashMap<String, Object> scoreContext = new HashMap<String, Object>();
        scoreContext.put(Game.CONTEXT_KEY_MINOR_RESULT, current.getMinorResult() / percentFinished);
        return new Score(current.getResult() / percentFinished, scoreContext);
    }

    private boolean isChallenge() {
        return Session.getCurrentSession().getChallenge() != null;
    }


    private Challenge getChallenge() {
        return Session.getCurrentSession().getChallenge();
    }
    /**
     * handler to create our dialogs
     */
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROGRESS:
                return ProgressDialog
                        .show(ExtentedGamePlayActivity.this, "", getString(R.string.submitting_your_score));
            case DIALOG_SUBMITTED:
                return (new AlertDialog.Builder(this))
                        .setMessage(R.string.score_was_submitted)
                        .setTitle(R.string.scoreloop)
                        .setIcon(getResources().getDrawable(R.drawable.sl_icon_badge))
                        .setPositiveButton(R.string.awesome, null)
                        .create();
            case DIALOG_FAILED:
                return (new AlertDialog.Builder(this))
                        .setMessage(R.string.score_submit_error)
                        .setPositiveButton(R.string.too_bad, null)
                        .create();
        }
        return null;
    }

    private class StopWatch {
        private long start;
        private long elapsed;
        private boolean running = false;

        public void reset() {
            stop();
            start = 0;
            elapsed = 0;
        }

        public boolean isRunning() {
            return running;
        }

        public void start() {
            if (!running) {
                running = true;
                start = System.currentTimeMillis();
            }
        }

        public void stop() {
            if (running) {
                running = false;
                elapsed += System.currentTimeMillis() - start;
            }
        }

        public long getElapsed() {
            if (running) {
                return elapsed + System.currentTimeMillis() - start;
            } else {
                return elapsed;
            }
        }

        /**
         * format elapsed time
         */
        public String getElapsedFormatted() {
            long millis = getElapsed();
            String hundreds = Integer.toString((int) (millis % 1000) / 10);
            long time = millis / 1000;
            String seconds = Integer.toString((int) (time % 60));
            String minutes = Integer.toString((int) ((time % 3600) / 60));
            String hours = Integer.toString((int) (time / 3600));
            for (int i = 0; i < 2; i++) {
                if (hundreds.length() < 2) {
                    hundreds = "0" + hundreds;
                }
                if (seconds.length() < 2) {
                    seconds = "0" + seconds;
                }
                if (minutes.length() < 2) {
                    minutes = "0" + minutes;
                }
                if (hours.length() < 2) {
                    hours = "0" + hours;
                }
            }
            return hours + ":" + minutes + ":" + seconds + "." + hundreds;
        }

    }


    /**
     * game play for ghost game
     * can be serialized to json
     */
    private class GamePlay {

        private final List<GameStep> steps = new ArrayList<GameStep>();

        private GamePlay() {
        }

        private GamePlay(JSONArray json) throws JSONException {
            for (int i = 0; i < json.length(); i++) {
                JSONObject jsonStep = (JSONObject) json.get(i);
                steps.add(new GameStep(jsonStep));
            }
        }

        public void add(long millis, int score) {
            steps.add(new GameStep(millis, score));
        }

        public JSONArray toJSONArray() throws JSONException {
            final JSONArray json = new JSONArray();
            for (GameStep step : steps) {
                json.put(step.toJSONObject());
            }
            return json;
        }

        public int getScoreForMillis(long millis) {
            for (int i=steps.size() -1;i>=0;i--) {
                GameStep step = steps.get(i);
                if (step.getMillis()<=millis) {
                    return step.getScore();
                }
            }
            return 0;
        }

    }

    /**
     * one step of a game play
     * can be serialized to json
     */
    private class GameStep {
        private final long millis;
        private final int score;

        private GameStep(long millis, int score) {
            this.millis = millis;
            this.score = score;
        }

        public GameStep(final JSONObject json) throws JSONException {
            millis = json.getLong("millis");
            score = json.getInt("score");
        }

        public long getMillis() {
            return millis;
        }

        public int getScore() {
            return score;
        }

        public JSONObject toJSONObject() throws JSONException {
            final JSONObject json = new JSONObject();
            json.put("millis", millis);
            json.put("score", score);
            return json;
        }


    }

}
