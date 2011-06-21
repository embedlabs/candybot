import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

@SuppressWarnings("serial")
public class Candyman extends JApplet {
	public static Color BGCOLOR = new Color(68,102,170);
	
	private CandyGame candygame; //holds menu, game, level select, and credits panels
	private CardLayout cl; //layout manager for candygame
	private MainMenu mainmenu; //menu with buttons
	private GamePanel gamepanel; //game screen
	private SelectPanel selectpanel; //enter level code
	private CreditPanel creditpanel; //credit screen
	private MenuButton newgamebutton,continuebutton,creditbutton; //buttons lead to game, level select, and credits panels
	private ReturnButton creditreturn,gamereturn,selectreturn,winreturn; //buttons that return to the mainmenu
	private GameText credits,levelselect; //credits text, and levelselect text
	private CodeTextField codetextfield; //field to input level code
	private LevelInfo levelinfo; //level name and code
	private RetryButton retrybutton; //restarts level
	private HintButton hintbutton; //mouse over for hint or tutorial
	private WinPanel winpanel; //when you win, you see this
	
	private int currentlevel; //current level being played
	private int crow,ccolumn,cmrow,cmcolumn,t1row,t1column,t2row,t2column; //coordinates for the candy and the candyman
	private int[][] levelarray = new int[18][24]; //level contents
	private String[] levelindex ={"useless","oxymoron","defenestrate","fluorescent","pineal",
									"loquacity","impetus","raspberry","lemma","oink",
									"q1w2e3r4t5y6"}; //array of level codes
	private String[] tooltips = {"Use the arrow keys to help the Candyman get the candy into the pipe.", //array of hints
								"Just keep doing it...",
								"\"Retry\" is a useful button.",
								"Use boxes to fill gaps.",
								"Bombs explode if they fall down and hit a wall.", // level 5
								"Teleporters are very nice.",
								"Movable walls don't respond to gravity, unlike boxes. They won't fit in teleporters.",
								"Lasers don't hurt items, only the Candyman.",
								"Watch out! Enemies kill the Candyman but are destroyed by lasers. They can push objects.",
								"Nope, no new items. You finished the tutorial. Be warned. This is the last easy level."}; // level 10
	private boolean candymancanmove = true; //is horizontal movement allowed?
	private boolean winlevel = false; //have you won the level yet?
	private boolean explode = false; //should the bomb explode if it touches a fixed wall?
	private boolean gotkilled = false; //did the candyman get burned?
	
	private int maxlevel = 10; //maximum levels, to detect when the game is won
	
	public Image background; //background png
	public Image tileset; //contains all other graphics
	public Image youwin; //win image
	public Image title; //candyman title
	
	public FallThread fallthread; //thread holding falling-related stuff

	boolean[][] enemyarray; //limits each enemy to one movement
	
	public static int WIDTH=720; // Dimensions may be changed later.
	public static int HEIGHT=540;
	
	/*
	 * 0 wall
	 * 1 space
	 * 2 candy
	 * 3 candyman
	 * 4 pipe left
	 * 5 pipe right
	 * 6 pipe left win
	 * 7 pipe right win
	 * 8 box
	 * 9 bomb
	 * 10 teleporter 1
	 * 11 teleporter 2
	 * 12 moveable wall
	 * 13 horizontal laser
	 * 14 vertical laser
	 * 15 cross laser
	 * 16 burned candyman
	 * 17 enemy
	 */
	private final int[][][] levels =
			{
			{{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},//1
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,3,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,0,0,1,0,0,1,1,1,1,1,1,0,0,0,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,4,5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}},
			
			{{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},//2
			{0,1,1,1,1,1,1,1,1,1,1,3,2,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,0,0,0,0,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0},
			{0,4,5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}},
			
			{{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},//3
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,3,2,1,1,1,1,1,1,1,1,1,1,0},
			{0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,0,0,0,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,0,0,0,0,0,1,1,0,0,1,1,1,0,0,0,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,0,0,0,0,0,1,1,0,0,0,0,1,1,0,0,0,0,1,1,0},
			{0,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,0,0,0,1,1,0,1,1,0,1,0,0,0,1,1,1,1,0,0,0,0},
			{1,1,1,1,1,1,1,1,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,0},
			{1,0,0,0,0,1,0,1,1,1,1,1,1,1,1,1,1,0,0,0,0,1,1,0},
			{1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,0},
			{0,4,5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}},
			
			{{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},//4
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,8,1,1,1,1,2,3,1,1,1,1,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,0,0,0,0,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,0,0,0,0,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,0},
			{1,0,0,0,0,0,0,1,1,1,1,0,0,1,0,0,0,0,1,1,1,1,1,0},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,4,5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}},
			
			{{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},//5
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,9,1,1,2,3,1,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,0},
			{0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,0,0,0,0,0,1,1,1,0,0,0,0,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,0},
			{0,0,0,0,0,0,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,0,0,0,0,0,1,1,0,0,0,0,0,1,1,1,0,0,0,0,0,0,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,0},
			{0,4,5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}},
			
			{{0,0,0,0,11,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},//6
			{0,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,2,3,1,0},
			{0,1,1,1,0,0,0,0,1,1,1,0,1,1,1,1,1,1,1,0,0,0,0,0},
			{0,1,1,1,1,0,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,0,1,1,1,1,1,0,1,1,1,1,0,0,0,0,1,1,1,0},
			{0,1,1,0,0,0,1,0,0,1,1,0,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,0,0,0,1,1,1,1,0,0,0,0,1,1,0,0,0,0,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,0,1,1,1,1,0,1,1,1,1,1,0,0,0,1,1,1,0},
			{0,1,1,1,1,1,0,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,0,0,1,1,1,0,1,1,1,0,1,1,1,1,1,1,1,0},
			{0,1,1,0,1,1,1,1,1,1,1,0,1,1,1,0,0,0,1,1,1,0,0,0},
			{0,1,1,0,0,0,1,1,1,1,1,0,1,1,1,1,1,1,1,0,0,0,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,0,0,0,10,0,0,0,0,0,0,0,0,0},
			{0,4,5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}},
			
			{{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},//7
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,2,3,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,12,12,12,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,1,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0},
			{0,4,5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}},
			
			{{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},//8
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,8,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,3,1,0},
			{0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,0,0,0,0,13,13,0,1,1,1,1,1,1,0,0,0,0,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,0,1,1,1,1,0},
			{0,1,1,1,1,1,1,0,0,0,0,1,1,1,1,1,1,1,0,1,1,1,1,0},
			{0,1,0,0,0,0,13,13,13,13,13,13,13,13,0,0,0,0,0,13,13,0,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,14,1,1,1,1,1,0},
			{0,1,1,1,0,0,0,1,1,1,1,0,0,0,0,0,1,14,1,1,1,1,1,0},
			{0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,14,1,0,0,0,0,1,1,0},
			{0,1,1,1,1,1,1,1,0,0,0,0,1,1,1,14,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,1,0,1,1,1,1,1,0},
			{0,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,14,1,1,1,1,1,0},
			{0,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,0,0,0,0,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,14,1,1,1,0},
			{0,4,5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}},
			
			{{0,0,0,0,0,11,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},//9
			{0,1,1,1,1,1,1,1,1,17,0,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,2,3,1,1,0},
			{0,1,1,1,0,0,0,1,1,1,0,1,1,1,1,1,1,0,0,0,0,0,0,0},
			{0,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,0,0,0,1,0,1,1,1,1,0,0,0,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,0,1,1,1,1,0,1,0,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,0,1,1,1,1,0,1,0,1,1,1,1,1,0},
			{0,1,1,0,0,1,0,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,0,0,0,1,1,1,0,1,0,1,0,1,1,1,0,1,0,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,0,1,0,1,0,1,1,1,0,1,0,1,1,0},
			{0,0,0,0,1,1,1,0,0,0,0,1,0,0,0,1,1,1,0,0,0,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,0,0,0},
			{0,1,1,0,0,0,1,1,1,1,0,1,1,1,0,0,0,1,1,1,1,0,1,0},
			{0,1,1,1,0,1,1,1,1,1,0,1,1,1,0,1,1,1,1,1,0,0,1,0},
			{0,1,1,1,0,1,1,1,1,1,0,17,1,1,0,1,1,1,1,1,1,1,1,0},
			{0,4,5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,10,0}},
			
			{{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},//10
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,9,1,1,8,1,1,1,1,1,1,1,1,1,1,1,1,2,3,1,0},
			{0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,13,13,0,0,0,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,14,1,1,1,0},
			{0,1,1,1,1,1,1,0,0,0,0,0,0,0,0,1,1,0,1,14,1,1,1,0},
			{0,1,0,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,1,14,1,1,1,0},
			{0,1,0,0,0,0,1,1,1,1,1,1,1,1,1,1,14,1,1,14,1,1,1,0},
			{0,1,0,0,0,0,0,0,0,0,0,0,13,13,13,13,15,0,1,0,0,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,0},
			{0,1,1,1,1,0,1,0,1,1,1,0,1,1,1,1,14,1,1,1,0,0,0,0},
			{0,1,1,1,1,0,0,0,1,1,1,0,1,1,0,0,0,0,0,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,0},
			{0,4,5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}},
			
			};
	
	public static void main(String[] args) { //code to make it runnable as an application
		JFrame frame = new JFrame("Candyman");
		frame.setLayout(new GridBagLayout());
		frame.getContentPane().setBackground(BGCOLOR);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		Candyman candyman = new Candyman();
		candyman.init();
		frame.add(candyman, new GridBagConstraints());
		//frame.pack();
        frame.setVisible(true);
	}
	
	@Override
	public void init() {
		candygame = new CandyGame(); //creates CardLayout containing menu, game, and other panes
		setContentPane(candygame); //shows the candygame
	}
	
	public class CandyGame extends JPanel {
		
		public CandyGame() {
			super(new CardLayout());
			
			try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());} catch (Exception e) {e.printStackTrace();}
			//attempts to change theme to local platform
	
			setPreferredSize(new Dimension(Candyman.WIDTH,Candyman.HEIGHT));
			setBackground(BGCOLOR); //sets background color
			
			background = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("background.jpg")); //all images are obtained from codebase
			tileset = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("tileset.png"));
			youwin = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("youwin.png"));
			title = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("title.png"));
	
			mainmenu = new MainMenu(); //creates all the panels containing game stuff
			creditpanel = new CreditPanel();
			gamepanel = new GamePanel();
			selectpanel = new SelectPanel();
			winpanel = new WinPanel();
			
			add(mainmenu,"MAINMENU"); //adds all panes to cardlayout
			add(gamepanel,"GAMEPANEL");
			add(creditpanel,"CREDITPANEL");
			add(selectpanel,"SELECTPANEL");
			add(winpanel,"WINPANEL");
			cl = (CardLayout)(getLayout());
			cl.show(this,"MAINMENU");
		}
	}

	public class MainMenu extends CandymanPanel {
		public MainMenu() {
			super("mainmenu",null);
			newgamebutton = new MenuButton("newgamebutton","New Game",3); //adds buttons for actions to main menu
			continuebutton = new MenuButton("continuebutton","Continue Game",4);
			creditbutton = new MenuButton("creditbutton","Credits",5);
			add(newgamebutton);
			add(continuebutton);
			add(creditbutton);
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(title,0,0,this); //allows for title image to show
		}
	}

	public class GamePanel extends CandymanPanel implements KeyListener {
		
		public GamePanel() {
			super("gamepanel",null);
			
			gamereturn = new ReturnButton(); //adds button to return to main menu from the game
			gamereturn.setBounds(Candyman.WIDTH-145,Candyman.HEIGHT-25,140,20);
			add(gamereturn);
			
			levelinfo = new LevelInfo(); //displays level number and level code
			levelinfo.setBounds(0,0,Candyman.WIDTH,30);
			add(levelinfo);
			
			retrybutton = new RetryButton(); //lets user restart level
			retrybutton.setBounds(Candyman.WIDTH-295,Candyman.HEIGHT-25,140,20);
			add(retrybutton);
			
			hintbutton = new HintButton(); //shows hint or tutorial in form of a tooltip
			hintbutton.setBounds(Candyman.WIDTH-445,Candyman.HEIGHT-25,140,20);
			add(hintbutton);
			
			addKeyListener(this); //listen for controls from player
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(background,0,0,this); //draws background
			
			for (int row=0;row<18;row++) { //draws level array
				for (int column=0;column<24;column++) {
					int i = levelarray[row][column];
					if (i!=1) {
						g.drawImage(tileset,
							column*Candyman.WIDTH/24,row*Candyman.WIDTH/24,
							column*Candyman.WIDTH/24+Candyman.WIDTH/24,
							row*Candyman.WIDTH/24+Candyman.WIDTH/24,
							(i%10)*30, (i/10)*30, (i%10)*30+30, (i/10)*30+30, this);
					}
				}
			}
			
			getToolkit().sync(); //supposedly makes flickering or other graphics problems nicer
		}
	
		@Override
		public void keyPressed(KeyEvent e) {
			int keycode = e.getKeyCode();
			switch (keycode) { //depending on input, performs certain action on the levelarray
			case KeyEvent.VK_UP: arrayvert("up");break;
			case KeyEvent.VK_DOWN: arrayvert("down");break;
			case KeyEvent.VK_LEFT: arrayhoriz("left");break;
			case KeyEvent.VK_RIGHT: arrayhoriz("right");break;
			}
		}
		
		@Override
		public void keyTyped(KeyEvent e) {}
		@Override
		public void keyReleased(KeyEvent e) {}
	}

	public class SelectPanel extends CandymanPanel {
		ConvenientPanel middlethird,bottomthird;
		
		public SelectPanel() {
			super("selectpanel",new GridLayout(3,1)); //layout consists of "Enter Code:", the field to enter it, and then a return to menu button
			
			levelselect = new GameText("<html><center><br /><br /><br /><br />Enter Code:</center></html>","levelselect");
			add(levelselect);
			
			middlethird = new ConvenientPanel();
			codetextfield = new CodeTextField();
			middlethird.add(codetextfield);
			add(middlethird);
			
			bottomthird = new ConvenientPanel();
			selectreturn = new ReturnButton();
			selectreturn.setHorizontalAlignment(SwingConstants.CENTER);
			bottomthird.add(selectreturn);
			add(bottomthird);
		}
	}

	public class CreditPanel extends CandymanPanel {
		ConvenientPanel bottomhalf;
		
		public CreditPanel() {
			super("creditpanel",new GridLayout(2,1)); //2 components are credit text, and return to menu button
			
			credits = new GameText("<html><center><br />Concept and code by Anshul Ramachandran and Prem Nair.<br /></center></html>","credits");
			add(credits);
			
			creditreturn = new ReturnButton();
			creditreturn.setHorizontalAlignment(SwingConstants.CENTER);
			bottomhalf = new ConvenientPanel();
			bottomhalf.add(creditreturn);
			add(bottomhalf);
		}
	}

	public class CandymanPanel extends JPanel { //basis for other panels
		
		public CandymanPanel(String name,LayoutManager layoutmanager) {
			super();
			setBackground(BGCOLOR);
			setName(name);
			setLayout(layoutmanager);
			setPreferredSize(new Dimension(600,600));
		}
	}

	public class WinPanel extends CandymanPanel {
		
		public WinPanel() {
			super("winpanel",null);
			
			winreturn = new ReturnButton(); //only has a return to menu button, besides background image of "you win"
			winreturn.setBounds(Candyman.WIDTH-145,Candyman.HEIGHT-25,140,20);
			add(winreturn);
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(youwin,0,0,this); //background win message
		}
	}

	public class ConvenientPanel extends JPanel { //gap filler for gridlayout
		
		public ConvenientPanel() {
			super();
			setLayout(new FlowLayout());
			setOpaque(false);
		}
	}

	public class GameText extends JLabel {
		
		public GameText(String text,String name) {
			super(text);
			setFont(new Font("Arial",Font.ITALIC,Candyman.WIDTH/24));
			setForeground(Color.white);
			setHorizontalAlignment(SwingConstants.CENTER);
			setName(name);
		}
	}

	public class LevelInfo extends GameText { //displays level number and code
		public LevelInfo() {
			super("","levelinfo");
			setPreferredSize(new Dimension(Candyman.WIDTH,30));
			setBackground(BGCOLOR);
	
			setFont(new Font("Arial",Font.PLAIN,25));
		}
	}

	public class CodeTextField extends JTextField implements ActionListener { //you input level code here
		
		public CodeTextField() {
			super();
			setPreferredSize(new Dimension(Candyman.WIDTH/4,20));
			addActionListener(this);
			setHorizontalAlignment(SwingConstants.CENTER);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			String levelcode = codetextfield.getText();
			codetextfield.setText("");
			int index = Arrays.asList(levelindex).indexOf(levelcode);
			if (index != -1) {
				currentlevel = index+1;
				initializeLevel();
				cl.show(candygame,"GAMEPANEL");
				gamepanel.requestFocusInWindow(); //important, otherwise arrow keys do nothing
			}
		}
	}

	public class MenuButton extends JButton implements ActionListener { //generic menu button
		
		public MenuButton(String name,String text,int position) {
			super();
			setFont(new Font("Arial",Font.ITALIC,Candyman.WIDTH/24));
			setBounds(Candyman.WIDTH/4,Candyman.HEIGHT*position/6,Candyman.WIDTH/2,Candyman.HEIGHT/6);
			setText("<html><center>"+text+"</center></html>");
			setOpaque(false);
			setName(name);
			addActionListener(this);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source.equals(newgamebutton)) {
				currentlevel=1;
				initializeLevel();
				cl.show(candygame,"GAMEPANEL");
				gamepanel.requestFocusInWindow();
			} else if (source.equals(continuebutton)) {
				cl.show(candygame,"SELECTPANEL");
				codetextfield.requestFocusInWindow();
			} else if (source.equals(creditbutton)) {
				cl.show(candygame,"CREDITPANEL");
			}
		}
	}

	public class ReturnButton extends JButton implements ActionListener { //return to main menu, used several times
		
		public ReturnButton() {
			super("Return to Menu");
			setFont(new Font("Arial",Font.ITALIC,12));
			setOpaque(false);
			setPreferredSize(new Dimension(140,20));
			addActionListener(this);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			cl.show(candygame, "MAINMENU");
			codetextfield.setText("");
		}
	}

	public class RetryButton extends JButton implements ActionListener { //retry level if stuck
		
		public RetryButton() {
			super("Retry");
			setFont(new Font("Arial",Font.ITALIC,12));
			setOpaque(false);
			setPreferredSize(new Dimension(140,20));
			addActionListener(this);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			try {fallthread.join(5000);} catch (InterruptedException ie) {ie.printStackTrace();}
			initializeLevel();
			gamepanel.repaint();
			gamepanel.requestFocusInWindow(); //important
		}
	}

	public class HintButton extends JButton implements ActionListener { //tooltip text gives hints
		
		public HintButton() {
			super("Hint");
			setFont(new Font("Arial",Font.ITALIC,12));
			setOpaque(false);
			setPreferredSize(new Dimension(140,20));
			ToolTipManager.sharedInstance().registerComponent(this);
			ToolTipManager.sharedInstance().setInitialDelay(0);
			addActionListener(this);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			gamepanel.requestFocusInWindow(); //important if accidentally clicked
		}
	}

	public class FallThread extends Thread {
		@Override
		public void run() {
			boxfall(); //all boxes fall down
			bombfall(); //all bombs fall down
			candyfall(); //animation where candy falls gradually
			if (gotkilled) {
				gotkilled=false;
				diecandyman();
			} else {
				win();
			}//did the player win yet? level or game?
			candymancanmove = true;
		}

		private void boxfall() {
			for (int row=0;row<18;row++) {
				for (int column=0;column<24;column++) {
					if (levelarray[17-row][column]==8) {
						int boxrow = 17-row;
						int boxcolumn = column;
						while (true) { //while a box can fall, fall
							if (boxrow==17) {break;}
							int next = levelarray[boxrow+1][boxcolumn];
							if (next==1||next==13||next==14||next==15) {
								levelarray[boxrow+1][boxcolumn] = 8;
								int next2 = levels[currentlevel-1][boxrow][boxcolumn];
								if (next2==13||next2==14||next2==15){
									levelarray[boxrow][boxcolumn]=levels[currentlevel-1][boxrow][boxcolumn];
								} else {
									levelarray[boxrow][boxcolumn] = 1;
								}
								boxrow++;
							} else if (next==10) {
								if (levelarray[t2row+1][t2column]==1) {
									levelarray[t2row+1][t2column]=8;
									int next2 = levels[currentlevel-1][boxrow][boxcolumn];
									if (next2==13||next2==14||next2==15){
										levelarray[boxrow][boxcolumn]=levels[currentlevel-1][boxrow][boxcolumn];
									} else {
										levelarray[boxrow][boxcolumn] = 1;
									}
									boxrow=t2row+1;
									boxcolumn=t2column;
								} else {break;}
							} else {break;}
						}
					}
				}
			}
			gamepanel.repaint();
		}

		private void bombfall() {
			for (int row=0;row<18;row++) {
				for (int column=0;column<24;column++) {
					if (levelarray[17-row][column]==9) {
						int bombrow = 17-row;
						int bombcolumn = column;
						while (true) { //while a box can fall, fall
							if (bombrow==17) {break;}
							if (levelarray[bombrow+1][bombcolumn]==0||levelarray[bombrow+1][bombcolumn]==12||levelarray[bombrow+1][bombcolumn]==17) {
								if (explode) {
									levelarray[bombrow+1][bombcolumn] = 1;
									if (levels[currentlevel-1][bombrow][bombcolumn]==13||levels[currentlevel-1][bombrow][bombcolumn]==14||levels[currentlevel-1][bombrow][bombcolumn]==15){
										levelarray[bombrow][bombcolumn]=levels[currentlevel-1][bombrow][bombcolumn];
									} else {
										levelarray[bombrow][bombcolumn] = 1;
									}
									break;
								} else {break;}
							} else if (levelarray[bombrow+1][bombcolumn]==10) {
								if (levelarray[t2row+1][t2column]==1) {
									levelarray[t2row+1][t2column]=9;
									if (levels[currentlevel-1][bombrow][bombcolumn]==13||levels[currentlevel-1][bombrow][bombcolumn]==14||levels[currentlevel-1][bombrow][bombcolumn]==15){
										levelarray[bombrow][bombcolumn]=levels[currentlevel-1][bombrow][bombcolumn];
									} else {
										levelarray[bombrow][bombcolumn] = 1;
									}
									bombrow=t2row+1;
									bombcolumn=t2column;
									explode=true;
								} else {break;}
							} else if (levelarray[bombrow+1][bombcolumn]==1||levelarray[bombrow+1][bombcolumn]==13||levelarray[bombrow+1][bombcolumn]==14||levelarray[bombrow+1][bombcolumn]==15) {
								levelarray[bombrow+1][bombcolumn] = 9;
								if (levels[currentlevel-1][bombrow][bombcolumn]==13||levels[currentlevel-1][bombrow][bombcolumn]==14||levels[currentlevel-1][bombrow][bombcolumn]==15){
									levelarray[bombrow][bombcolumn]=levels[currentlevel-1][bombrow][bombcolumn];
								} else {
									levelarray[bombrow][bombcolumn] = 1;
								}
								bombrow++;
								explode=true;
							} else {break;}
						}
						explode=false;
					}
				}
			}
			gamepanel.repaint();
		}

		private void candyfall() {
			while (true) {
				if ((crow==17)||(crow==-1)) {break;} //if the candy is at the bottom, or level complete, break
				int next = levelarray[crow+1][ccolumn]; //next block to go through
				if (next==1||next==13||next==14||next==15) { //if block is empty space, go through it
					try {Thread.sleep(75);} catch (Exception e) {e.printStackTrace();}
					levelarray[crow+1][ccolumn] = 2;
					if (levels[currentlevel-1][crow][ccolumn]==13||levels[currentlevel-1][crow][ccolumn]==14||levels[currentlevel-1][crow][ccolumn]==15){
						levelarray[crow][ccolumn]=levels[currentlevel-1][crow][ccolumn];
					} else {
						levelarray[crow][ccolumn] = 1;
					}
					crow++;
					gamepanel.paintImmediately(0,0,Candyman.WIDTH,Candyman.HEIGHT);
				} else if ((next==4)||(next==5)) { //if block is pipe, change pipe color to tiles 6,7
					levelarray[crow+1][ccolumn] = 2+next;
					levelarray[crow+1][ccolumn-(2*next)+9] = 11-next;
					if (levels[currentlevel-1][crow][ccolumn]==13||levels[currentlevel-1][crow][ccolumn]==14||levels[currentlevel-1][crow][ccolumn]==15){
						levelarray[crow][ccolumn]=levels[currentlevel-1][crow][ccolumn];
					} else {
						levelarray[crow][ccolumn] = 1;
					}
					crow = -1;
					ccolumn = -1;
					winlevel=true;
					gamepanel.paintImmediately(0,0,Candyman.WIDTH,Candyman.HEIGHT);
				} else if (next==10) {
					if (levelarray[t2row+1][t2column]==1) {
						try {Thread.sleep(75);} catch (Exception e) {e.printStackTrace();}
						levelarray[t2row+1][t2column]=2;
						if (levels[currentlevel-1][crow][ccolumn]==13||levels[currentlevel-1][crow][ccolumn]==14||levels[currentlevel-1][crow][ccolumn]==15){
							levelarray[crow][ccolumn]=levels[currentlevel-1][crow][ccolumn];
						} else {
							levelarray[crow][ccolumn] = 1;
						}
						crow=t2row+1;
						ccolumn=t2column;
						gamepanel.paintImmediately(0,0,Candyman.WIDTH,Candyman.HEIGHT);
					} else {break;}
				} else {break;}
			}
		}
	}
	private void enemyactuallymove(int row, int column, int rowchange, int columnchange) {
		// TODO check for bugs
		int next = levelarray[row+rowchange][column+columnchange];
		if (next==1) {
			levelarray[row+rowchange][column+columnchange]=17;
			levelarray[row][column]=1;
			enemyarray[row+rowchange][column+columnchange]=true;
		} else if (next==13||next==14||next==15) {
			levelarray[row][column]=1;
		} else if (next==3) {
			levelarray[row][column]=1;
			levelarray[row+rowchange][column+columnchange]=16;
			gotkilled=true;
		} else if (next==2||next==8||next==9||next==10||next==11) {
			if (columnchange!=0&&columnchange*column<11.5*columnchange+10.5) {
				int next2=levelarray[row][column+2*columnchange];
				if (next2==1||next2==13||next2==14||next2==15) {
					levelarray[row][column+2*columnchange]=next;
					levelarray[row][column+columnchange]=17;
					levelarray[row][column]=1;
					if (next==2) {
						ccolumn+=columnchange;
					} else if (next==10) {
						t1column+=columnchange;
					} else if (next==11) {
						t2column+=columnchange;
					}
					int next3=levels[currentlevel-1][row][column+columnchange];
					if (next3==13||next3==14||next3==15) {
						levelarray[row][column+columnchange]=next3;
					} else {
						enemyarray[row][column+columnchange]=true;
					}
				}
			}
		} else if (next==10&&rowchange==1) {
			int next4=levelarray[t2row+1][t2column];
			if (next4==1) {
				levelarray[t2row+1][t2column]=17;
				levelarray[row][column]=1;
				enemyarray[t2row+1][t2column]=true;
			} else if (next4==13||next4==14||next4==15) {
				levelarray[row][column]=1;
			}
		} else if (next==11&&rowchange==-1) {
			int next5=levelarray[t1row-1][t1column];
			if (next5==1) {
				levelarray[t1row-1][t1column]=17;
				levelarray[row][column]=1;
				enemyarray[t1row+1][t1column]=true;
			} else if (next5==13||next5==14||next5==15) {
				levelarray[row][column]=1;
			}
		}
	}

	private void enemymove() {
		// TODO check for bugs
		enemyarray = new boolean[18][24];
		for (int row=0;row<18;row++) {
			for (int column=0;column<24;column++) {
				if (levelarray[row][column]==17&&(!enemyarray[row][column])) {
					float slope = ((float)(row-cmrow))/((float)(cmcolumn-column));
					float absslope = Math.abs(slope);
					if (cmrow<row) {
						if (absslope>=1.0) {
							enemyactuallymove(row,column,-1,0);
						} else {
							enemyactuallymove(row,column,0,(int)Math.signum(slope));
						}
					} else if (cmrow>row) {
						if (absslope>=1.0) {
							enemyactuallymove(row,column,1,0);
						} else {
							enemyactuallymove(row,column,0,-(int)Math.signum(slope));
						}
					} else {
						if (cmcolumn>column) {
							enemyactuallymove(row,column,0,1);
						} else {
							enemyactuallymove(row,column,0,-1);
						}
					}
				}
			}
		}
	}

	private void initializeLevel() { //sets up for new level
		loadArray(); //load "levelarray" from "levels"
		levelinfo.setText("Level "+currentlevel+"            Code: "+levelindex[currentlevel-1]); //keeps levelinfo up-to-date
		hintbutton.setToolTipText(tooltips[currentlevel-1]); //updates hint
	}
	
	private void loadArray() { //loads levelarray from levels, done carefully to avoid issues relating to references
		for (int row=0;row<18;row++) {
			for (int column=0;column<24;column++) {
				levelarray[row][column]=levels[currentlevel-1][row][column];
				switch (levelarray[row][column]) {
				case 2: crow=row;ccolumn=column; break;
				case 3: cmrow=row;cmcolumn=column; break;
				case 10: t1row=row;t1column=column; break;
				case 11: t2row=row;t2column=column; break;
				default: break;
				}
			}
		}
	}

	private void arrayhoriz(String direction) {
		if (candymancanmove) {
			candymancanmove = false;
			if ((direction.equals("left"))&&(0<cmcolumn)) {
				int next = levelarray[cmrow][cmcolumn-1];
				if (next==1) {
					levelarray[cmrow][cmcolumn-1] = 3;
					levelarray[cmrow][cmcolumn] = 1;
					cmcolumn--;enemymove();
				} else if (next==13||next==14||next==15||next==17) {
					levelarray[cmrow][cmcolumn-1] = 3;
					levelarray[cmrow][cmcolumn] = 1;
					cmcolumn--;
					gotkilled=true;
				} else {
					if ((next==2||next==8||next==9||next==10||next==11||next==12)&&(cmcolumn>1)) {
						if (levelarray[cmrow][cmcolumn-2]==1||levelarray[cmrow][cmcolumn-2]==13||levelarray[cmrow][cmcolumn-2]==14||levelarray[cmrow][cmcolumn-2]==15) {
							levelarray[cmrow][cmcolumn-2] = next;
							levelarray[cmrow][cmcolumn-1] = 3;
							levelarray[cmrow][cmcolumn] = 1;
							if (next==2) {
								ccolumn--;
							} else if (next==10) {
								t1column--;
							} else if (next==11) {
								t2column--;
							}
							cmcolumn--;enemymove();
							int next2=levels[currentlevel-1][cmrow][cmcolumn];
							if (next2==13|next2==14||next2==15) {
								gotkilled=true;
							}
						}
					}
				}
			}
			if ((direction.equals("right"))&&(23>cmcolumn)) {
				int next = levelarray[cmrow][cmcolumn+1];
				if (next==1) {
					levelarray[cmrow][cmcolumn+1] = 3;
					levelarray[cmrow][cmcolumn] = 1;
					cmcolumn++;enemymove();
				} else if (next==13||next==14||next==15||next==17) {
					levelarray[cmrow][cmcolumn+1] = 3;
					levelarray[cmrow][cmcolumn] = 1;
					cmcolumn++;
					gotkilled=true;
				} else {
					if ((next==2||next==8||next==9||next==10||next==11||next==12)&&(cmcolumn<22)) {
						if (levelarray[cmrow][cmcolumn+2]==1||levelarray[cmrow][cmcolumn+2]==13||levelarray[cmrow][cmcolumn+2]==14||levelarray[cmrow][cmcolumn+2]==15) {
							levelarray[cmrow][cmcolumn+2] = next;
							levelarray[cmrow][cmcolumn+1] = 3;
							levelarray[cmrow][cmcolumn] = 1;
							if (next==2) {
								ccolumn++;
							} else if (next==10) {
								t1column++;
							} else if (next==11) {
								t2column++;
							}
							cmcolumn++;enemymove();
							int next2=levels[currentlevel-1][cmrow][cmcolumn];
							if (next2==13|next2==14||next2==15) {
								gotkilled=true;
							}
						}
					}
				}
			}
			gamepanel.repaint();
			fallthread = new FallThread();
			fallthread.start(); //start thread to update positions due to falling
		}
	}
	
	private void arrayvert(String direction) { //if the candyman can move vertically, he will (you can't move objects vertically)
		if (candymancanmove) {
			candymancanmove=false;
			if ((direction.equals("up")) && (0 < cmrow)) {
				int next = levelarray[cmrow - 1][cmcolumn];
				if (next == 1) { //normal moving up
					levelarray[cmrow - 1][cmcolumn] = 3;
					levelarray[cmrow][cmcolumn] = 1;
					cmrow--;enemymove();
				} else if (next == 11) { //candyman teleportation
					if (levelarray[t1row - 1][t1column] == 1) {
						levelarray[t1row - 1][t1column] = 3;
						levelarray[cmrow][cmcolumn] = 1;
						cmrow = t1row - 1;
						cmcolumn = t1column;
					} else if (levelarray[t1row-1][t1column]==13||levelarray[t1row-1][t1column]==14||levelarray[t1row-1][t1column]==15) {
						levelarray[t1row - 1][t1column] = 3;
						levelarray[cmrow][cmcolumn] = 1;
						cmrow = t1row - 1;
						cmcolumn = t1column;
						gotkilled=true;
					}
				} else if (next == 12) { //moveable wall
					if (1 < cmrow) {
						if (levelarray[cmrow - 2][cmcolumn] == 1) {
							levelarray[cmrow - 2][cmcolumn] = 12;
							levelarray[cmrow - 1][cmcolumn] = 3;
							levelarray[cmrow][cmcolumn] = 1;
							cmrow--;enemymove();
						}
					}
				} else if (next==13||next==14||next==15||next==17) { //lasers
					levelarray[cmrow - 1][cmcolumn] = 3;
					levelarray[cmrow][cmcolumn] = 1;
					cmrow--;
					gotkilled=true;
				}
			} else if ((direction.equals("down")) && (17 > cmrow)) {
				int next = levelarray[cmrow + 1][cmcolumn];
				if (next == 1) { //normal moving down
					levelarray[cmrow + 1][cmcolumn] = 3;
					levelarray[cmrow][cmcolumn] = 1;
					cmrow++;enemymove();
				} else if (next == 10) { //candyman teleportation
					if (levelarray[t2row + 1][t2column] == 1) {
						levelarray[t2row + 1][t2column] = 3;
						levelarray[cmrow][cmcolumn] = 1;
						cmrow = t2row + 1;
						cmcolumn = t2column;
					} else if (levelarray[t2row + 1][t2column] == 13||levelarray[t2row + 1][t2column] == 14||levelarray[t2row + 1][t2column] == 15) {
						levelarray[t2row + 1][t2column] = 3;
						levelarray[cmrow][cmcolumn] = 1;
						cmrow = t2row + 1;
						cmcolumn = t2column;
						gotkilled=true;
					}
				} else if (next == 12) { //moveable wall
					if (16 > cmrow) {
						if (levelarray[cmrow + 2][cmcolumn] == 1) {
							levelarray[cmrow + 2][cmcolumn] = 12;
							levelarray[cmrow + 1][cmcolumn] = 3;
							levelarray[cmrow][cmcolumn] = 1;
							cmrow++;enemymove();
						}
					}
				} else if (next==13||next==14||next==15||next==17) { //lasers
					levelarray[cmrow + 1][cmcolumn] = 3;
					levelarray[cmrow][cmcolumn] = 1;
					cmrow++;
					gotkilled=true;
				}
			}
			gamepanel.repaint();
			fallthread = new FallThread();
			fallthread.start(); //start thread to update positions due to falling
		}
	}

	private void diecandyman() { //change the candyman to hurt, then reset level
		winlevel=false;
		creditpanel.requestFocusInWindow();
		levelarray[cmrow][cmcolumn]=16;
		gamepanel.paintImmediately(0,0,Candyman.WIDTH,Candyman.HEIGHT);
		try {Thread.sleep(1000);} catch (Exception e) {e.printStackTrace();}
		initializeLevel();
		gamepanel.repaint();
		gamepanel.requestFocusInWindow();
	}
	
	private void win() {
		if (winlevel) { //if win, go to next level, or win the game
			creditpanel.requestFocusInWindow();
			try {Thread.sleep(1000);} catch (Exception e) {e.printStackTrace();}
			if (currentlevel<maxlevel) {
				currentlevel++;
				winlevel = false;
				initializeLevel();
				gamepanel.repaint();
				gamepanel.requestFocusInWindow();
			} else {
				winlevel = false;
				cl.show(candygame,"WINPANEL");
			}
		}
	}
}
