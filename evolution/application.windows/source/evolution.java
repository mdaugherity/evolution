import processing.core.*; 
import processing.xml.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class evolution extends PApplet {

/* NOTES
 Using genetic decision tree:
 - guys motion is controlled by decision tree with two inputs: x1 = scaled guys position (0,1), x2 = scaled ball's position (0,1)
 - tree has two outputs: w1 = go left, w2 = go right
 - inputs and thresholds of each node is evolved with genetic algorithm: each node's chromosome is 9 bits:
 --- bit 0 = feature (0=x1, 1=x2)
 --- bit 1 = sign (0 is <, 1 is >)
 --- bits 2-8 threshold (unsigned) number from [0-127]+1, threshold is this number divided by 128 
 - each node makes decision is (feature) (sign) threshold
 - leaf nodes are hard-coded (see fig 7.15, pg 376)
 - notice that these trees are set by a 63-bit chromosome meaning that there are 2^63 ~= 10^19 possibilities, and the genetic algorithm is a random search for a good one 
 
 TO DO:
 -make window which visualizes tree for single guy 
 -save/load generations or chromosomes
 -more features: ball height, delta guy-ball, danger, flocking/anti-flocking
 
 */

// globals
int NUMGUYS = 16;
Guy[] g;
Ball b;
int NUMALIVE;
int generation = 1;
float BALLX = 340.0f;
float BALLY = 10;
PFont gfont; // guy status updates


public void setup() {
  size(800, 400);
  smooth();
  g = new Guy[NUMGUYS];
  //for (int i=0; i<NUMGUYS; i++) g[i] = new Guy(i, random(50, width-50), 2 );  // Guy(id, x, speed)
  for (int i=0; i<NUMGUYS; i++) g[i] = new Guy(i, width/2, 2 );  // Guy(id, x, speed)
  NUMALIVE = NUMGUYS;
  b = new Ball(BALLX, BALLY, 50, 5.0f);
  b.reset();

  //gfont = loadFont("ArialMT-14.vlw");
  gfont = loadFont("CourierNewPSMT-14.vlw");
  textFont(gfont);
}

public void draw() {
  background(70);
  int i, j;
  //for (i=0; i<NUMGUYS; i++) {
  for (i=NUMGUYS-1; i>=0; i--) {  // drawing in reverse so MOM and DAD always show up on top
    g[i].update();  
    g[i].draw();
    g[i].drawStatus();
  }
  fill(200);
  textFont(gfont);
  String s = "Generation " + generation;
  text(s, g[0].statusx-3, g[0].statusy-17);

  b.update();
  b.draw();


  if (NUMALIVE==0) {

    // determine winners  
    int dad = -1; // the two winners
    int mom = -1; 
    float score1 = 0;
    float score2 = 0;
    for (i=0; i<NUMGUYS; i++) {  // first pass for top score
      if (g[i].score > score1) {
        dad = i;
        score1 = g[i].score;
      }
    }
    for (i=0; i<NUMGUYS; i++) {  // second pass for second score
      if (i==dad) continue; 
      if (g[i].score > score2) {
        mom = i;
        score2 = g[i].score;
      }
    }

    for (i=0; i<NUMGUYS; i++) {  // print generation scores
      print("guy " + i + "\t" + g[i].score);
      if (i==dad) print("\tDAD");
      if (i==mom) print("\tMOM");
      println("");
    }

    int NUMCHROME = g[0].t.NUMCHROME; // make this a global?
    int[] ch1 = new int[NUMCHROME];
    int[] ch2 = new int[NUMCHROME];
    for (i=0; i<NUMCHROME; i++) ch1[i] = g[dad].t.chrome[i];
    for (i=0; i<NUMCHROME; i++) ch2[i] = g[mom].t.chrome[i];

    generation++;
    println("*** GENERATION " + generation + " ***");

    // Set chromosones of next generation
    for (i=0; i<NUMCHROME; i++) g[0].t.chrome[i] = ch1[i];   // make copies of winners
    for (i=0; i<NUMCHROME; i++) g[1].t.chrome[i] = ch2[i];   

    print("0: DAD\t\t");
    for (i=0; i<NUMCHROME; i++) print(g[0].t.chrome[i]);
    println("");
    print("1: MOM\t\t");
    for (i=0; i<NUMCHROME; i++) print(g[1].t.chrome[i]);
    println("");

    int[] ch3 = new int[NUMCHROME]; // our new child
    for (j=2;  j<NUMGUYS; j++) {   // fill in the rest of the guys
      float rand = random(0, 1);
      int method = 0;  // default to new random chrome
      if (rand < 0.15f) method = 1;  // crossover 1->2
      if (rand>=0.15f && rand<0.3f) method = 2;  // crossover 2->1
      if (rand>=0.3f && rand<0.5f) method = 3;  // mutate 1
      if (rand>=0.5f && rand<0.7f) method = 4;  // mutate 2

      print(j +": ");

      if (method==0) {  // generate new random chromosome
        print("random\t\t");
        for (i=0; i<NUMCHROME; i++) {
          if (random(0, 1) < 0.5f) ch3[i]=0;
          else ch3[i]=1;
        }
      }
      if (method==1) {  // crossover 1->2
        int pos = PApplet.parseInt( random(1, NUMCHROME-1) );
        print("cross 1->2 at " + pos + "\t");
        for (i=0; i<NUMCHROME; i++) {
          if (i<pos) ch3[i] = ch1[i];
          else ch3[i] = ch2[i];
        }
      }
      if (method==2) {  // crossover 2->1
        int pos = PApplet.parseInt( random(1, NUMCHROME-1) );
        print("cross 2->1 at " + pos + "\t");
        for (i=0; i<NUMCHROME; i++) {
          if (i<pos) ch3[i] = ch2[i];
          else ch3[i] = ch1[i];
        }
      }
      if (method==3) {  // mutate 1
        print("mutate 1\t");
        for (i=0; i<NUMCHROME; i++) {
          ch3[i] = ch1[i];
          if (random(0, 1) < 0.15f) {
            if (ch3[i]==0) ch3[i]=1;
            if (ch3[i]==1) ch3[i]=0;
          }
        }
      }
      if (method==4) {  // mutate 2
        print("mutate 2\t");
        for (i=0; i<NUMCHROME; i++) {
          ch3[i] = ch2[i];
          if (random(0, 1) < 0.1f) {
            if (ch3[i]==0) ch3[i]=1;
            if (ch3[i]==1) ch3[i]=0;
          }
        }
      }

      for (i=0; i<NUMCHROME; i++) g[j].t.chrome[i] = ch3[i];
      for (i=0; i<NUMCHROME; i++) print(ch3[i]);
      println("");
    }  // end loop j

    b.reset();
    for (i=0; i<NUMGUYS; i++) g[i].reset();
    NUMALIVE = NUMGUYS;
  }  // end if NUMALIVE==0
}

public void keyPressed() {
  if (key=='k') {
    for (int i=0; i<NUMGUYS; i++) g[i].ALIVE=false;
    NUMALIVE = 0;
  }
}

class Ball {
  float x;
  float y;
  int rad;

  float vx;
  float vy;
  float g = 0.15f;

  Ball(float ix, float iy, int irad, float ivx) {
    x = ix;
    y = iy;
    rad = irad;
    vx = ivx;
    vy = 0;
  }

  public void reset() {
    //x = random(10, width-10);
    x = BALLX;
    //y = 10;
    y = BALLY;
    vx = 5.0f;
    vy = 0;
    //if ( random(0, 1)<0.5 ) vx = -5.0;
    //println("RESET: " + x + ",    " + vx);
  }

  public void update() {
    x += vx;
    y += vy;

    vy+=g;

    if (x<0) {   // letting the ball reach corners
      //x = rad;
      x = 0;
      vx *= -0.99f;
    }
    if (x>width) {  // letting the ball reach corners
      //x = width-rad;
      x = width;
      vx *= -0.99f;
    }
    if (y>height-rad) {
      //y -= vy;
      y = height - rad;
      vy -= g;

      vy *= -0.985f;
    }
  }

  public void draw() {
    ellipseMode(RADIUS);
    fill(255);
    ellipse(x, y, rad, rad);
  }
}

class Guy {
  int id;
  float x;
  int y;
  float vx;
  float speed;
  //int cooldown;
  //int MAXCOOL = 100;
  float score;
  int life;
  float lastx, last2x;  // previous positions
  float lastvx;
  float distx, dist2x;  // total distance walked
  boolean ALIVE;

  Tree t;   // his brain

  int statusx; //location of status text
  int statusy; 

  // constants for drawing
  int neck = 8;
  int head = 8;
  int torso = 18;
  int leg = 20;
  int legx = 8;
  int elbowx = 10;
  int elbowy = 5;
  int armx = 3;
  int army = 10;
  int frame;
  int FRAMES = 4;
  int[] dax = {
    0, 2, 0, -2
  };
  int[] dlx = {
    0, -3, -6, -3
  };  



  Guy(int iid, float ix, float ispeed) {
    id = iid;
    int tall = torso + leg;
    y = height - tall;
    x = ix;
    speed = ispeed;
    vx = speed;
    frame = 0;
    score = 0;
    life = 0;
    lastx = 0;
    last2x = 0;
    distx = 0;
    dist2x = 0;
    ALIVE = true;

    statusx = 15;
    statusy = 35 + 16*id;

    t = new Tree();
    t.Print();
  }

  public void reset() {  // bring us back to life
    ALIVE = true;
    x = width/2;
    vx = speed;
    score = 0;
    life = 0;
    distx = 0;
    dist2x = 0;
    t.update();  // updates tree based on new chrome
  }

  public void update() {
    if (ALIVE) {

      //splat
      if ( sqrt((b.x-x)*(b.x-x) + (b.y-y)*(b.y-y))<= b.rad ) {
        println("...guy " + id + " dies at age " + life + "\tscore " + score + "\tdist2 " + dist2x);
        ALIVE = false;
        NUMALIVE--;
      }
      else {

        /*    //OLD AI
         cooldown++;
         if (b.x>x && vx>0 && cooldown>MAXCOOL) {
         vx*=-1.0;
         cooldown=0;
         }
         if (b.x<x && vx<0 && cooldown>MAXCOOL) {
         vx*=-1.0;
         cooldown = 0;
         }
         */

        // decide which direction to run, set vx=(+/-)speed
        float[] input = new float[2];  // what our guy can "see"
        input[0] = x / width;  // my position (scaled from 0-1)
        input[1] = b.x / width; // ball's position (scaled from 0-1)
        int dir = t.decide(input); // 0=left, 1=right
        if (dir==0) vx=speed;
        else vx= -1.0f*speed;

        //update guy
        last2x = lastx;
        lastx = x;
        x += vx;

        //if (x>=width-5) x=10;
        //if (x<5) x=width-10;
        int margin = 15;
        if (x>=width-margin) {
          x=width-margin;
          //vx*=-1.0;
        }
        if (x<margin) {
          x=margin;
          //vx *=-1.0;
        }

        distx += abs(x-lastx);
        dist2x += abs(x-last2x); // looking back 2 steps eliminates "jittering" counting as distance
        life++;
        score = dist2x;  // score determines fitness for breeding
        if (life%4==0) frame++;
        if (frame>=FRAMES) frame=0;
      }
    }
  }

  public void draw() {

    if (ALIVE) {

      //DEBUG - show thresholds on screen
      if (id==0) {
        int i;
        for (i=0; i<t.NUMNODES; i++) {
          if (t.n[i].leaf==false) {
            if (t.n[i].feat==0) stroke(0, 255, 0); // green is for guy
            if (t.n[i].feat==1) stroke(0, 0, 255); // blue is for ball
            int tx = PApplet.parseInt(t.n[i].thresh*width);
            line(tx, height, tx, height-5);
          }
        }
      }
      //---end debug---

      // neck up
      stroke(255);
      fill(255);
      if (id==0) {  // DAD
        stroke(155, 163, 255);
        fill(155, 163, 255);
      }
      if (id==1) { // MOM
        stroke(255, 157, 237);
        fill(255, 157, 237);
      }
      line(x, y, x, y-neck);
      ellipseMode(RADIUS);
      //noFill();
      ellipse(x, y-neck-head, head, head);

      // body and legs
      line(x, y, x, y+torso);
      line(x, y+torso, x-legx-dlx[frame], y+torso+leg);
      line(x, y+torso, x+legx+dlx[frame], y+torso+leg);

      // arms
      line(x, y, x-elbowx, y-elbowy);
      line(x-elbowx, y-elbowy, x-elbowx-armx+dax[frame], y-elbowy-army);
      line(x, y, x+elbowx, y-elbowy);
      line(x+elbowx, y-elbowy, x+elbowx+armx+dax[frame], y-elbowy-army);
    }
    else {  //dead
      stroke(255);
      line(x, y+torso+leg, x-torso, y+torso+leg);
      ellipseMode(RADIUS);
      fill(255);
      ellipse(x+head, y+torso+leg, head, head/2);
      fill(255, 0, 0, 150);
      ellipse(x, y+torso+leg, 20, 3);
    }
  }

  public void drawStatus() {  // prints running status update
    String s = "";
    if(id<10) s+=" ";
    s += nf(id, 0) + ": ";
    if (ALIVE) s+="Alive";
    else s+="Dead ";
    s+="  " + PApplet.parseInt(score);

    textFont(gfont, 12);
    textAlign(LEFT, CENTER);
    if (ALIVE) fill(160);    
    else fill(30);
    if (ALIVE && id==0) fill(155, 163, 255);
    if (ALIVE && id==1) fill(255, 157, 237);
    text(s, statusx, statusy);
  }
}

// Decision Tree classes

class Node {
  int id;
  int par;  // ID of parent
  int lid; // ID of left branch
  int rid; // ID of right branch

  int feat;   // nodes ask is x[feat] (sign) thresh?
  int sign;   // 0 is <,  1 is >  
  int chr;
  float thresh;
  boolean leaf;
  int cat;      // leaf pre-assigned to category

  int GENESIZE = 9;
  int[] chrome;

  Node(int ind, int ipar, int il, int ir) {  // normal constructor
    id = ind;
    par = ipar;
    leaf = false;
    lid = il;
    rid = ir;
    cat = -1;
    chrome = new int[GENESIZE];  // chromosome
  }

  Node(int ind, int ipar, int icat) {  // leaf constructor
    id = ind;
    par = ipar;
    leaf = true;
    cat = icat;
    lid = -1;
    rid = -1;
  }

  public void update() {  // update feat and thresh based on chromosome
    if (leaf) return;
    feat = chrome[0];
    sign = chrome[1];
    chr=0;
    int b=1;
    int i;
    for (i=GENESIZE-1; i>=2; i--) {  // convert 7 bits to number 0-127
      chr += b*chrome[i];
      b*=2;
    }  
    thresh = (float)(chr+1)/128.0f;

    //println("Update node " +id + ":\t"); 
    //println(chrome);
    //for (i=0; i<GENESIZE; i++) print(chrome[i]);  
    //println("\t" + feat + ", " + sign +", " + chr + ", " + thresh);
  }

  public void Print() { // prints node info to screen
    if (leaf) println("leaf " + id +":\t"+ cat);
    else {
      print("node " + id + ":\t" + par + "\t" + feat + "\t" + sign + "\t" + thresh + "\t" + lid + "\t" + rid + "\t");
      for (int i=0; i<GENESIZE; i++) print(chrome[i]); 
      println("");
    }
  }

  public int climb(float[] x) {
    if (leaf) return -1;
    int ret = rid;
    if (sign==0 && (x[feat] < thresh) ) ret=lid;  // sign=0 for <
    if (sign==1 && (x[feat] > thresh) ) ret=lid;
    //println("  climb:  " +id + "\t" +x[feat] + "\t" + sign + "\t" + thresh + "\t" + ret);
    return ret;
  }
}


// ****************************************************************************

class Tree {
  int NUMNODES = 15;  // includes nodes + leaves 
  Node[] n;
  int NUMGENES = 7;  // leaf nodes not included since they don't get genes 
  int GENESIZE = 9;
  int NUMCHROME = NUMGENES*GENESIZE;
  int[] chrome; 
  int NUMFEAT = 2;  // number of input features
  int root = 3;

  Tree() {
    n = new Node[NUMNODES];
    //Numbering scheme preserves branch ordering for chromosomes
    //  Could have implemented a split function to do this automatically, but then gene ordering the chrome is really ugly

    /*  // 2 Layer tree
     n[0] = new Node(0, -1, 1, 2);   // node: id, par, left, right
     n[1] = new Node(1, 0, 0);  //leaf: id, par, cat
     n[2] = new Node(2, 0, 1);  //leaf: id, par, cat
     */

    /*******************  
     3 LAYER TREE
     --------1
     ---0          2
     -3   4     5     6 
     ***************************/
    /*
    n[1] = new Node(1, -1, 0, 2); // ROOT // node: id, par, left, right
     n[0] = new Node(0, 1, 3, 4);  // Layer 1
     n[2] = new Node(2, 1, 5, 6);
     n[3] = new Node(3, 0, 0);  // Layer 2  //leaf: id, par, cat
     n[4] = new Node(4, 0, 1);
     n[5] = new Node(5, 2, 0);
     n[6] = new Node(6, 2, 1);
     */

    /*******************  
     4 Layer Tree
     --------3
     ---1          5
     -0   2     4     6 
     7 8 9 10 11 12 13 14
     L R L  R  L  R  L  R
     *****************************/
    n[3] = new Node(3, -1, 1, 5); // ROOT // node: id, par, left, right
    n[1] = new Node(1, 3, 0, 2);  // Layer 1
    n[5] = new Node(5, 3, 4, 6);
    n[0] = new Node(0, 1, 7, 8);  // Layer 2  
    n[2] = new Node(2, 1, 9, 10);
    n[4] = new Node(4, 5, 11, 12);
    n[6] = new Node(6, 5, 13, 14);
    n[7] = new Node(7, 0, 0);  // Layer 3 //leaf: id, par, cat
    n[8] = new Node(8, 0, 1);
    n[9] = new Node(9, 2, 0);
    n[10] = new Node(10, 2, 1);
    n[11] = new Node(11, 4, 0);
    n[12] = new Node(12, 4, 1);
    n[13] = new Node(13, 6, 0);
    n[14] = new Node(14, 6, 1);

    chrome = new int[NUMCHROME];
    int i;
    for (i=0; i<NUMCHROME; i++) {
      if (random(0, 1) < 0.5f) chrome[i] = 0;
      else chrome[i] = 1;
    }
    //println("TREE:");
    //println(chrome);

    update();
  }

  public int decide(float[] x) {
    int curr=root;
    while (n[curr].leaf==false) curr = n[curr].climb(x);
    int ret = n[curr].cat; 
    //println("decide\t" + x[0] + "\t" + x[1] + "\t" + curr + "\t" + ret);
    return ret;
  }

  public void Print() {  // prints tree info to screen 
    println("---TREE---");
    println("\tpar\tfeat\tsign\tthresh\tLeft\tRight\tGene");
    int i;
    for (i=0; i<NUMNODES; i++) n[i].Print();
    for (i=0; i<NUMCHROME; i++) print( nf(chrome[i], 0) );
    println("\n----------");
  }

  public void update() {  // applies chromosome to all (non-leaf) nodes and updates
    int i;
    int  j;
    for (i=0; i<NUMGENES; i++) {
      for (j=0; j<GENESIZE; j++) {
        int ic = i*GENESIZE + j;
        n[i].chrome[j] = chrome[ic];
      }
      n[i].update();
    }
  }
}

  static public void main(String args[]) {
    PApplet.main(new String[] { "--present", "--bgcolor=#666666", "--stop-color=#cccccc", "evolution" });
  }
}
