import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Tetorisu {
	private static final int WIDTH = 485 + 16;
	private static final int HEIGHT = 495 + 20;

	public static void main(String[] args) {
		JFrame gameFrame = new JFrame();
		gameFrame.getContentPane().add(new CanvasBoard(WIDTH, HEIGHT));

		gameFrame.setTitle("GameFrame");
		gameFrame.setSize(WIDTH, HEIGHT);
		gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gameFrame.setResizable(false);
		gameFrame.setVisible(true);

	}
}

class CanvasBoard extends JPanel implements KeyListener, Runnable {
	private int WIDTH; //画面横幅
	private int HEIGHT; //画面縦幅
	private final int GRID_X = 12; //マップの横マス数
	private final int GRID_Y = 24; //マップの縦マス数
	private BufferedImage blockImage; //ブロック画像
	private GameField GField; //ゲームフィールド管理クラス
	private Block Brk; //ブロック管理クラス
	private boolean running = false, gameflag = false;
	private int speed;
	private Thread t1;
	private boolean started = false; 

	/* コンストラクタ */
	CanvasBoard(int width, int height) {
		WIDTH = width;
		HEIGHT = height;
		try {
			blockImage = ImageIO.read(new File("./images/pinkublk.gif"));
		} catch (Exception e) {
			System.out.println("image file not found.");
			e.printStackTrace();
		}
		GField = new GameField();
		Brk = new Block();
		this.setFocusable(true);
		this.addKeyListener(this);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		/* 背景色描画 */
		g.setColor(new Color(255, 255, 200));
		g.fillRect(0, 0, WIDTH, HEIGHT);

		/* マップ描画 */
		for (int y = 0; y < GRID_Y; y++) {
			for (int x = 0; x < GRID_X; x++) {
				if (GField.getMapInfo(x, y) == true) {
					g.drawImage(blockImage, x * GField.getGridWidth(), y * GField.getGridHeight(),
							GField.getGridWidth(), GField.getGridHeight(), this);
				}
			}
		}
		/* ブロック描画 */
		for (int i = 0; i < Brk.getMaxLineCnt(); i++) {
			for (int j = 0; j < Brk.getMaxRowCnt(); j++) {
				if (Brk.getBlockFlag()[i][j] == true) {
					g.drawImage(blockImage, Brk.getPosX(i, j), Brk.getPosY(i, j), Brk.getBlockWidth(),
							Brk.getBlockHeight(), this);
				}
			}
		}
		//次に落下させるブロック描画
		g.setColor(Color.pink);
		g.drawString("NextBlock",250,45);
		for(int i = 0, y = 0; i < Brk.getMaxLineCnt(); i++, y += Brk.getBlockHeight()) {
			for(int j = 0, x = 0; j < Brk.getMaxRowCnt(); j++, x += Brk.getBlockWidth()) {
				if(Brk.getNextBlockFlag(i, j) == true) {
					g.drawImage(blockImage, 230+x, 50+y, Brk.getBlockWidth(), Brk.getBlockHeight(), this);
				}
			}
		}
		//現在のスコアを描画
		g.drawString("Score : " + GField.getGamePoint(), 250,180);
		switch(speed) {
		case 500:
            g.drawString("Level1", 250, 200);
            break;
        case 300:
            g.drawString("Level2", 250, 200);
            break;
        case 250:
            g.drawString("Level3", 250, 200);
            break;
        case 200:
            g.drawString("Level4", 250, 200);
            break;
        case 170:
            g.drawString("Level5", 250, 200);
            break;
        case 140:
            g.drawString("Level6", 250, 200);
            break;
        case 120:
            g.setColor(Color.red);
            g.drawString("Max Level", 250, 200);
            break;
        default:
            g.setColor(Color.red);
            g.drawString("UltraMax Level", 250, 200);
            break;
        }
		//ゲーム終了時、最終スコアを表示
		if(started == true && gameflag == false) {
		    g.setColor(Color.red);
		    g.drawString("gameOver", 90, 200);
		    g.drawString("Score : "+ GField.getGamePoint(), 100, 220);
		}
	}

	public void keyPressed(KeyEvent e) {
		//右が押されたとき
		if (e.VK_RIGHT == e.getKeyCode()) {
			if (GField.canMoveRight(Brk) == true) {
				for (int i = 0; i < Brk.getMaxLineCnt(); i++) {
					for (int j = 0; j < Brk.getMaxRowCnt(); j++) {
						Brk.setRePosX(i, j, Brk.getBlockWidth());
					}
				}
			}
			repaint();
		} else if (e.VK_LEFT == e.getKeyCode()) {
			if (GField.canMoveLeft(Brk) == true) {
				for (int i = 0; i < Brk.getMaxLineCnt(); i++) {
					for (int j = 0; j < Brk.getMaxRowCnt(); j++) {
						Brk.setRePosX(i, j, (-1) * Brk.getBlockWidth());
					}
				}
			}
			repaint();
		} else if (e.VK_DOWN == e.getKeyCode()) {
			if (GField.canMoveDown(Brk) == true) {
				for (int i = 0; i < Brk.getMaxLineCnt(); i++) {
					for (int j = 0; j < Brk.getMaxRowCnt(); j++) {
						Brk.setRePosY(i, j, Brk.getBlockHeight());
					}
				}
			} else {
				//ブロック着地処理
				Brk.changeLife(false);
				GField.landBlock(Brk);
				GField.deleteLineBlock();
				Brk.BlockFlagFalse();
			}
			repaint();
		} else if (e.VK_UP == e.getKeyCode()) {
			/* ブロックの回転処理 */
			if (GField.canRotation(Brk) == true) {
				Brk.rotationBlock();
				repaint();
			}
		} else if (e.VK_SPACE == e.getKeyCode() && gameflag == false) {
			/* ゲーム開始処理を実装 */
			this.gameStart();
		}
	}
	public void keyReleased(KeyEvent e) {
	}
	public void keyTyped(KeyEvent e) {
	}
	//スレッド処理
	public void run() {
		while (running) {
			try {
				if (gameflag == true) {
					//ブロックの速度に合わせてスリープ
					Thread.sleep(speed);
					//スコアに応じて落下速度変化
					if(GField.getGamePoint() > 50000) {
						speed = 100;
					}else if(GField.getGamePoint() > 10000){
                        speed = 120;  //120ミリ秒(0.12秒)
                    }else if(GField.getGamePoint() > 8000){
                        speed = 140;  //140ミリ秒(0.14秒)
                    }else if(GField.getGamePoint() > 5000){
                        speed = 170;  //170ミリ秒(0.17秒)
                    }else if(GField.getGamePoint() > 3000){
                        speed = 200;  //200ミリ秒(0.2秒)
                    }else if(GField.getGamePoint() > 2000){
                        speed = 250;  //250ミリ秒(0.25秒)
                    }else if(GField.getGamePoint() > 1000){
                        speed = 300;  //300ミリ秒(0.3秒)
                    }
					//新しいブロックセット
					Brk.setBlockIfNeeded();

					//下へ移動できるか判定
					if (GField.canMoveDown(Brk) == true) {
						for (int i = 0; i < Brk.getMaxLineCnt(); i++) {
							for (int j = 0; j < Brk.getMaxRowCnt(); j++) {
								Brk.setRePosY(i, j, Brk.getBlockHeight());
							}
						}
					} else {
						//下に移動できない場合は着地
						Brk.changeLife(false);
						GField.landBlock(Brk);
						GField.deleteLineBlock();
						Brk.BlockFlagFalse();
						chkGameOver(GField.getMapInfoArray());
					}
					//再描画
					repaint();
				} else {
					//なにもしない
				}
			} catch (InterruptedException e) {
			}
		}
	}

	//ゲーム開始メソッド
	public void gameStart() {
		//ブロック落下速度初期化
		speed = 500;
		//フィールドを新しく初期化
		GField = new GameField();
		//ゲーム開始フラグをON
		gameflag = true;
		//スレッド起動フラグON
		running = true;
		//ゲームを始めたことがあるかどうか
		started = true;
		//スレッドインスタンス
		t1 = new Thread(this);
		//スレッド起動
		t1.start();
	}
	//ゲームオーバー判定メソッド
	public void chkGameOver(boolean isMapInfo[][]) {
		boolean flag = false;
		for(int j = 1; j <GRID_X-1; j++) {
			//上部３マスにブロックがおかれていたら
			if(isMapInfo[2][j] == true) {
				//ゲームオーバーフラグセット
				flag = true;
			}
		}
		//ゲームオーバー時に初期化
		if(flag == true) {
			gameflag = false;
			running = false;
			repaint();
		}
	}
}

/* ブロックを管理するクラス */
class Block {
	private boolean blockFlag[][]; //ブロックの配置フラグ
	private boolean nextBlockFlag[][];
	private int Bx[][], By[][]; //ブロックの位置座標
	private final int ROW = 5, LINE = 5; //ブロック領域の列数、行数
	private final int BLOCK_HEIGHT = 20, BLOCK_WIDTH = 20; //ブロック領域のセルサイズ
	private int blockKind;
	private boolean isLife = false;
	private int nextBlockKind;

	/* コンストラクタ */
	Block() {
		Bx = new int[LINE][ROW];
		By = new int[LINE][ROW];
		blockFlag = new boolean[LINE][ROW];
		nextBlockFlag = new boolean[LINE][ROW];
		for (int i = 0, countY = 0; i < LINE; i++, countY += BLOCK_HEIGHT) {
			for (int j = 0, countX = 60; j < ROW; j++, countX += BLOCK_WIDTH) {
				Bx[i][j] = countX;
				By[i][j] = countY;
			}
		}
	}

	/* ブロックをセットするメソッド */
	public void setBlock(int flag) {
		blockKind = flag;
		/* ブロックの配置フラグを初期化 */
		for (int i = 0; i < LINE; i++) {
			for (int j = 0; j < ROW; j++) {
				blockFlag[i][j] = false;
			}
		}
		/*  */
		/* 逆L */
		if (flag == 0) {
			blockFlag[1][1] = true;
			blockFlag[2][1] = true;
			blockFlag[2][2] = true;
			blockFlag[2][3] = true;
			/* L */
		} else if (flag == 1) {
			blockFlag[1][3] = true;
			blockFlag[2][3] = true;
			blockFlag[2][2] = true;
			blockFlag[2][1] = true;
			/* 凸 */
		} else if (flag == 2) {
			blockFlag[1][2] = true;
			blockFlag[2][1] = true;
			blockFlag[2][2] = true;
			blockFlag[2][3] = true;
			/* Z */
		} else if (flag == 3) {
			blockFlag[1][1] = true;
			blockFlag[1][2] = true;
			blockFlag[2][2] = true;
			blockFlag[2][3] = true;
			/* 逆Z */
		} else if (flag == 4) {
			blockFlag[2][1] = true;
			blockFlag[2][2] = true;
			blockFlag[1][2] = true;
			blockFlag[1][3] = true;
			/* 棒 */
		} else if (flag == 5) {
			blockFlag[0][2] = true;
			blockFlag[1][2] = true;
			blockFlag[2][2] = true;
			blockFlag[3][2] = true;
			/* 四角 */
		} else {
			blockFlag[1][2] = true;
			blockFlag[1][3] = true;
			blockFlag[2][2] = true;
			blockFlag[2][3] = true;
		}
	}
	//ネクストブロックのセットメソッド
	public void setNextBlock(int flag) {
		for(int i = 0; i < LINE; i++) {
			for(int j = 0; j < ROW; j++) {
				nextBlockFlag[i][j]=false;
			}
		}
		/* 逆L */
		if (flag == 0) {
			nextBlockFlag[1][1] = true;
			nextBlockFlag[2][1] = true;
			nextBlockFlag[2][2] = true;
			nextBlockFlag[2][3] = true;
			/* L */
		} else if (flag == 1) {
			nextBlockFlag[1][3] = true;
			nextBlockFlag[2][3] = true;
			nextBlockFlag[2][2] = true;
			nextBlockFlag[2][1] = true;
			/* 凸 */
		} else if (flag == 2) {
			nextBlockFlag[1][2] = true;
			nextBlockFlag[2][1] = true;
			nextBlockFlag[2][2] = true;
			nextBlockFlag[2][3] = true;
			/* Z */
		} else if (flag == 3) {
			nextBlockFlag[1][1] = true;
			nextBlockFlag[1][2] = true;
			nextBlockFlag[2][2] = true;
			nextBlockFlag[2][3] = true;
			/* 逆Z */
		} else if (flag == 4) {
			nextBlockFlag[2][1] = true;
			nextBlockFlag[2][2] = true;
			nextBlockFlag[1][2] = true;
			nextBlockFlag[1][3] = true;
			/* 棒 */
		} else if (flag == 5) {
			nextBlockFlag[0][2] = true;
			nextBlockFlag[1][2] = true;
			nextBlockFlag[2][2] = true;
			nextBlockFlag[3][2] = true;
			/* 四角 */
		} else {
			nextBlockFlag[1][2] = true;
			nextBlockFlag[1][3] = true;
			nextBlockFlag[2][2] = true;
			nextBlockFlag[2][3] = true;
		}
	}
	/* ブロック領域の最大行数を返すメソッド */
	public int getMaxLineCnt() {
		return LINE;
	}

	/* ブロック領域の最大列数を返すメソッド */
	public int getMaxRowCnt() {
		return ROW;
	}

	/* １ブロック(セル)の縦幅を返すメソッド */
	public int getBlockHeight() {
		return BLOCK_HEIGHT;
	}

	/* １ブロック(セル)の横幅を返すメソッド */
	public int getBlockWidth() {
		return BLOCK_WIDTH;
	}

	/* 現在のブロック種別を返すメソッド */
	public boolean[][] getBlockFlag() {
		return blockFlag;
	}
	
	/*次回のブロック種別を返すメソッド */
	public boolean getNextBlockFlag(int i, int j) {
		return nextBlockFlag[i][j];
	}

	/* ブロックのY座標を設定するメソッド */
	public void setRePosY(int i, int j, int t) {
		By[i][j] += t;
	}

	/* ブロックのX座標を設定するメソッド */
	public void setRePosX(int i, int j, int t) {
		Bx[i][j] += t;
	}

	/* ブロックのX座標を取得するメソッド */
	public int getPosX(int i, int j) {
		return Bx[i][j];
	}

	/* ブロックのY座標を取得するメソッド */
	public int getPosY(int i, int j) {
		return By[i][j];
	}

	//ブロックを回転させるメソッド
	public void rotationBlock() {
		boolean buff[][] = new boolean[LINE][ROW];
		for (int i = 0; i < LINE; i++) {
			for (int j = 0; j < ROW; j++) {
				buff[i][j] = blockFlag[i][j];
			}
		}
		for (int i = 0; i < LINE; i++) {
			for (int j = 0; j < ROW; j++) {
				blockFlag[ROW - 1 - j][i] = buff[i][j];
			}
		}
	}

	//ブロックの種類を取得するメソッド
	public int getBlockKind() {
		return blockKind;
	}

	//ブロック領域を初期化するメソッド
	public void BlockFlagFalse() {
		for (int i = 0; i < LINE; i++) {
			for (int j = 0; j < ROW; j++) {
				blockFlag[i][j] = false;
			}
		}
	}

	//ブロック領域の座標を初期化するメソッド
	public void setLife() {
		isLife = true;
		for (int i = 0, countY = 0; i < LINE; i++, countY += BLOCK_HEIGHT) {
			for (int j = 0, countX = 60; j < ROW; j++, countX += BLOCK_WIDTH) {
				Bx[i][j] = countX;
				By[i][j] = countY;
			}
		}
	}

	//ブロック状態を変更するメソッド
	public void changeLife(boolean life) {
		isLife = life;
	}

	//ブロックの状態（落下中＝true,着地＝false）を取得するメソッド
	public boolean getLife() {
		return isLife;
	}

	//新しいブロックをセットするメソッド
	public void setBlockIfNeeded() {
		if (this.getLife() == false) {
			this.setBlock(nextBlockKind);
			this.setLife();
			nextBlockKind = (int) (7 * Math.random());
			this.setNextBlock(nextBlockKind);
		}
	}
}

/* ゲームフィールドを管理するクラス */
class GameField {
	private boolean isMapInfo[][];
	private final int GRID_X = 12;
	private final int GRID_Y = 24;
	private final int GRID_HEIGHT = 20, GRID_WIDTH = 20;
	private int gamePoint;

	GameField() {
		gamePoint = 0;

		/* マップ上の各セルにブロックが接地されて
		いるかどうかを管理する2次元配列 */
		isMapInfo = new boolean[GRID_Y][GRID_X];

		/* 左側の壁を設定 */
		for (int j = 0; j < GRID_Y; j++) {
			isMapInfo[j][0] = true;
		}
		/* 右側の壁を設定 */
		for (int j = 0; j < GRID_Y; j++) {
			isMapInfo[j][11] = true;
		}
		/* 下側の壁を設定 */
		for (int i = 1; i < GRID_X; i++) {
			isMapInfo[23][i] = true;
		}
		/* 上記以外のセルをブロックなしで初期化 */
		for (int j = 0; j < GRID_Y - 1; j++) {
			for (int i = 1; i < GRID_X - 1; i++) {
				isMapInfo[j][i] = false;
			}
		}
	}

	/* マップ上の各セルにブロックが接地されているかどうかを
	示すフラグを取得するメソッド */
	public boolean getMapInfo(int x, int y) {
		return isMapInfo[y][x];
	}

	public void setMapInfo(Block blk) {
		for (int i = 0; i < blk.getMaxLineCnt(); i++) {
			for (int j = 0; j < blk.getMaxRowCnt(); j++) {
				if (blk.getBlockFlag()[i][j] == true) {
					isMapInfo[blk.getPosY(i, j) / blk.getBlockHeight()][blk.getPosX(i, j) / blk.getBlockWidth()] = true;
				}
			}
		}
	}

	/* セルの縦幅を返すメソッド */
	public int getGridHeight() {
		return GRID_HEIGHT;
	}

	/* セルの横幅を返すメソッド */
	public int getGridWidth() {
		return GRID_WIDTH;
	}

	//左方向へ移動できるか判定するメソッド
	public boolean canMoveLeft(Block blk) {
		boolean flag = false;
		for (int i = 0; i < blk.getMaxLineCnt(); i++) {
			for (int j = 0; j < blk.getMaxRowCnt(); j++) {
				if (blk.getBlockFlag()[i][j] == true
						&& isMapInfo[blk.getPosY(i, j) / blk.getBlockHeight()][blk.getPosX(i, j) / blk.getBlockWidth()
								- 1] == true) {
					flag = true;
				}
			}
		}
		//左方向へ移動できるなら「true」、できないなら「false」を返す
		if (flag == false) {
			return true;
		} else {
			return false;
		}
	}

	//右方向へ移動できるか判定するメソッド 
	public boolean canMoveRight(Block blk) {
		boolean flag = false;
		for (int i = 0; i < blk.getMaxLineCnt(); i++) {
			for (int j = 0; j < blk.getMaxRowCnt(); j++) {
				if (blk.getBlockFlag()[i][j] == true
						&& isMapInfo[blk.getPosY(i, j) / blk.getBlockHeight()][blk.getPosX(i, j) / blk.getBlockWidth()
								+ 1] == true) {
					flag = true;
				}
			}
		}
		//右方向へ移動できるなら「true」、できないなら「false」を返す
		if (flag == false) {
			return true;
		} else {
			return false;
		}
	}

	//下移動できるか判定メソッド
	public boolean canMoveDown(Block blk) {
		boolean flag = false;
		for (int i = 0; i < blk.getMaxLineCnt(); i++) {
			for (int j = 0; j < blk.getMaxRowCnt(); j++) {
				if (blk.getBlockFlag()[i][j] == true
						&& isMapInfo[blk.getPosY(i, j) / blk.getBlockHeight() + 1][blk.getPosX(i, j)
								/ blk.getBlockWidth()] == true) {
					flag = true;
				}
			}
		}
		//下方向へ移動できるなら「true」、できないなら「false」を返す
		if (flag == false) {
			return true;
		} else {
			//ブロックが1行並んだらその行のブロックを消滅させる処理を実装
			return false;
		}
	}

	//ブロックが回転できるか判定するメソッド
	public boolean canRotation(Block blk) {
		boolean ngFlag = false;
		int lineCnt;
		int rowCnt;

		lineCnt = blk.getMaxLineCnt();
		rowCnt = blk.getMaxRowCnt();

		try {
			//四角ブロック
			if (blk.getBlockKind() == 6) {
				ngFlag = true;
				//四角以外
			} else {
				for (int i = 0; i < lineCnt; i++) {
					for (int j = 0; j < rowCnt; j++) {
						if (isMapInfo[blk.getPosY(rowCnt - 1 - j, i) / blk.getBlockHeight()][blk.getPosX(rowCnt - 1 - j,
								i) / blk.getBlockWidth()] == true) {
							ngFlag = true;
						}
					}
				}
			}
		} catch (Exception e) {
			ngFlag = true;
		}

		//回転可能ならtrue,不可能ならfalseを返す
		if (ngFlag == true) {
			return false;
		} else {
			return true;
		}
	}

	//ブロックをゲームフィールドに接地させるメソッド
	public void landBlock(Block blk) {
		for (int i = 0; i < blk.getMaxLineCnt(); i++) {
			for (int j = 0; j < blk.getMaxRowCnt(); j++) {
				if (blk.getBlockFlag()[i][j] == true) {
					isMapInfo[blk.getPosY(i, j) / blk.getBlockHeight()][blk.getPosX(i, j) / blk.getBlockWidth()] = true;

				}
			}
		}
	}

	//ブロックが横1列で消滅させるメソッド
	public void deleteLineBlock() {
		boolean flag = false;
		for (int i = 0; i < GRID_Y - 1; i++) {
			for (int j = 1; j < GRID_X - 1; j++) {
				if (isMapInfo[i][j] == false) {
					flag = true;
				}
			}
			if (flag == false) {
				gamePoint += 100;
				for (int z = 1; z < GRID_X - 1; z++) {
					isMapInfo[i][z] = false;
				}
				for (int k = i; k > 0; k--) {
					for (int z = 1; z < GRID_X - 1; z++) {
						isMapInfo[k][z] = isMapInfo[k - 1][z];
					}
				}
			} else {
				flag = false;
			}
		}
	}
	//スコア取得メソッド
	public int getGamePoint() {
		return gamePoint;
	}
	//フィールド上の全マップのブロック接地状態を取得メソッド
	public boolean[][] getMapInfoArray(){
		return isMapInfo;
	}
}
