/*
	ミニRPGゲーム

	コンパイル方法
		> javac -encoding utf-8 MiniRPG.java

	実行方法
		> java MiniRPG
	
	動作正常 2021.1.8 確認
*/

import java.awt.*;
import java.awt.event.*;

class MiniRPG {
	public static void main(String[] args) {
		MyWindow mw = new MyWindow();
		Thread th = new Thread(mw);
		th.start();
	}
}

// キャラクター用の抽象クラス
abstract class Character {
	private int life;
	String name;
	int x, y;
	int steps;		// 何歩歩いたか（アニメーションで使用）
	int type;		// プレイヤーキャラは1勇者、2ドラゴン、敵は色を管理
	
	abstract void move( int left, int top, int right, int bottom );
	abstract int attack( Character[] chr );
	abstract void draw( Graphics g, Image img );
	
	public int getLife() { return this.life; }
	public void setLife( int life ) { this.life = life; }
	public int calcLife( int val ) {
		this.life += val;
		if( this.life < 0 ) this.life = 0;
		return this.life;
	}
}

class PlCharacter extends Character {
	final int[] XP = { 0, 4, 6, 4, 0, -4, -6, -4 };
	final int[] YP = { -6, -4, 0, 4, 6, 4, 0, -4 };
	static int braveman;
	static int dragon;
	int xp, yp;
	
	PlCharacter( String name, int x, int y, int dir ) {
		braveman++;
		this.name = name;
		this.x = x;
		this.y = y;
		this.xp = XP[dir];
		this.yp = YP[dir];
		this.steps = 0;
		this.type = 1;
		setLife( 3 );
	}
	
	PlCharacter( int x, int y ) {
		dragon++;
		this.name = "ドラゴン";
		this.x = x;
		this.y = y;
		this.xp = -28;
		this.yp = 16;
		this.steps = 0;
		this.type = 2;
		setLife( 8 );
	}
	
	public void move( int left, int top, int right, int bottom ) {
		steps++;
		if( y < top ) { y += steps * steps; return; }
		x += xp;
		y += yp;
		if( x < left || right < x ) { x -= xp; xp = -xp; }
		if( y < top || bottom < y ) { y -= yp; yp = -yp; }
	}
	
	public int attack( Character[] c ) {
		for( int i=0; i<c.length; i++ ) {
			if( c[i] instanceof EmyCharacter ) {
				if( ( (x-c[i].x) * (x-c[i].x) + ( y-c[i].y ) * ( y - c[i].y ) ) < 64 * 64 ) return i;
			}
		}
		return -1;
	}
	
	public void draw( Graphics g, Image img ) {
		if( type == 1 ) {
			int dir = 0;
			if( xp > 0 ) dir = 1;
			if( yp > 0 ) dir = 2;
			if( xp < 0 ) dir = 3;
			int sx = 1200 + ( steps % 2 ) * 32;
			int sy = 240 + dir * 48;
			g.drawImage( img, x-16, y-24, x+16, y+24, sx, sy, sx+32, sy+48, null );
			g.setColor( Color.WHITE );
			g.drawString( name, x+24, y+16 );
		}
		if( type == 2 ) {
			int sx = 1200;
			int sy = 432;
			g.drawImage( img, x-61, y-48, x+61, y+48, sx, sy, sx+122, sy+96, null );
		}
		for( int i=0; i<getLife(); i++ ) g.drawImage( img, x+24+i*16, y-16, x+40+i*16, y, 1264, 240, 1280, 256, null );
	}
}

class EmyCharacter extends Character {
	static int enemy;
	
	EmyCharacter( int x, int y, int col ) {
		enemy++;
		this.name = "ハニワスライム";
		this.x = x;
		this.y = y;
		this.steps = 0;
		this.type = col;
		setLife( col+1 );
	}
	
	public void move( int left, int top, int right, int bottom ) {
		steps++;
		x = x + (int)( Math.random() * 11 ) - 5;
		y = y + (int)( Math.random() * 11 ) - 5;
		if( x < left ) x = left;
		if( x > right ) x = right;
		if( y < top ) y = top;
		if( y > bottom ) y = bottom;
	}
	
	public int attack( Character[] c ) { return -1; }
	
	public void draw( Graphics g, Image img ) {
		int sx = 1200 + ( steps % 2 ) * 64;
		int sy = type * 80;
		g.drawImage( img, x-32, y-40, x+32, y+40, sx, sy, sx+64, sy+80, null );
	}
}

class MyWindow extends Frame implements Runnable {
	final int WIDTH = 1200;
	final int HEIGHT = 800;
	final int PL_MAX = 10;
	final int EMY_MAX = 30;
	final int CHR_MAX = PL_MAX + EMY_MAX;
	
	private Character[] chr = new Character[CHR_MAX];
	private int score;
	private Image img;
	
	MyKeyListener key = new MyKeyListener();
	MyMouseListener mouse = new MyMouseListener();
	
	MyWindow() {
		img = getToolkit().getImage( "MiniRPG.png" );
		setSize( WIDTH, HEIGHT );
		setTitle( "召喚勇者RPG" );
		setResizable( false );
		
		addKeyListener( key );
		addMouseListener( mouse );
		addWindowListener( new MyWinListener() );
		
		setVisible( true );
	}
	
	public int rnd( int max ) { return(int)( Math.random() * max ); }
	
	public void run() {
		while( true ) {
			if( mouse.click == 1 ) {
				mouse.click = 0;
				for( int i=0; i<PL_MAX; i++ ) {
					if( chr[i] == null ) {
						if( key.code == KeyEvent.VK_SPACE ) {
							chr[i] = new PlCharacter( WIDTH / 2, 0 );
						}
						else {
							chr[i] = new PlCharacter( "勇者"+i, mouse.x, mouse.y, rnd(8) );
						}
						break;
					}
				}
			}
			
			if( rnd( 1000 ) < 100 ) {
				int n = PL_MAX + rnd( EMY_MAX );
				if( chr[n] == null ) chr[n] = new EmyCharacter( rnd(WIDTH), HEIGHT/2+rnd(HEIGHT/2), rnd(3) );
			}
			
			for( int i=0; i<CHR_MAX; i++ ) {
				if( chr[i] != null ) {
					chr[i].move( 0, HEIGHT/2, WIDTH, HEIGHT );
					int atc = chr[i].attack( chr );
					if( atc >= 0 ) {
						if( chr[i].calcLife(-1) == 0 ) chr[i] = null;
						if( chr[atc].calcLife(-1) == 0 ) {
							chr[atc] = null;
							score++;
						}
					}
				}
			}
			
			repaint();
			try{ Thread.sleep(100); } catch( Exception e ) {}
		}
	}
	
	public void update( Graphics g ) { paint( g ); }
	
	public void paint( Graphics g ) {
		g.drawImage( img, 0, 0, this );
		g.setColor( Color.GREEN ); g.drawString( "勇者召喚数 " + PlCharacter.braveman, 60, 60 );
		g.setColor( Color.CYAN ); g.drawString( "ドラゴン召喚数 " + PlCharacter.dragon, 60, 90 );
		g.setColor( Color.YELLOW ); g.drawString( "魔物出現数 " + EmyCharacter.enemy, 60, 120 );
		g.setColor( Color.PINK ); g.drawString( "討伐数 " + score, 60, 150 );
		g.setColor( Color.WHITE );
		g.drawString( "キー" + key.code + " マウス(" + mouse.x + "," + mouse.y + ")", 1000, 60 );
		for( int i=0; i<CHR_MAX; i++ ) if( chr[i] != null ) chr[i].draw( g, img );
	}
}

class MyWinListener extends WindowAdapter {
	public void windowClosing( WindowEvent e ) { System.exit(0); }
}

class MyKeyListener extends KeyAdapter {
	int code;
	public void keyPressed(KeyEvent e) { code = e.getKeyCode(); }
	public void keyReleased(KeyEvent e) { code = 0; }
}

class MyMouseListener extends MouseAdapter {
	int x, y, click;
	public void mousePressed( MouseEvent e ) {
		x = e.getX();
		y = e.getY();
		click = 1;
	}
}
