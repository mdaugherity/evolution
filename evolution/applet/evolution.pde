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
float BALLX = 340.0;
float BALLY = 10;
PFont gfont; // guy status updates


void setup() {
  size(800, 400);
  smooth();
  g = new Guy[NUMGUYS];
  //for (int i=0; i<NUMGUYS; i++) g[i] = new Guy(i, random(50, width-50), 2 );  // Guy(id, x, speed)
  for (int i=0; i<NUMGUYS; i++) g[i] = new Guy(i, width/2, 2 );  // Guy(id, x, speed)
  NUMALIVE = NUMGUYS;
  b = new Ball(BALLX, BALLY, 50, 5.0);
  b.reset();

  //gfont = loadFont("ArialMT-14.vlw");
  gfont = loadFont("CourierNewPSMT-14.vlw");
  textFont(gfont);
}

void draw() {
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
      if (rand < 0.15) method = 1;  // crossover 1->2
      if (rand>=0.15 && rand<0.3) method = 2;  // crossover 2->1
      if (rand>=0.3 && rand<0.5) method = 3;  // mutate 1
      if (rand>=0.5 && rand<0.7) method = 4;  // mutate 2

      print(j +": ");

      if (method==0) {  // generate new random chromosome
        print("random\t\t");
        for (i=0; i<NUMCHROME; i++) {
          if (random(0, 1) < 0.5) ch3[i]=0;
          else ch3[i]=1;
        }
      }
      if (method==1) {  // crossover 1->2
        int pos = int( random(1, NUMCHROME-1) );
        print("cross 1->2 at " + pos + "\t");
        for (i=0; i<NUMCHROME; i++) {
          if (i<pos) ch3[i] = ch1[i];
          else ch3[i] = ch2[i];
        }
      }
      if (method==2) {  // crossover 2->1
        int pos = int( random(1, NUMCHROME-1) );
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
          if (random(0, 1) < 0.15) {
            if (ch3[i]==0) ch3[i]=1;
            if (ch3[i]==1) ch3[i]=0;
          }
        }
      }
      if (method==4) {  // mutate 2
        print("mutate 2\t");
        for (i=0; i<NUMCHROME; i++) {
          ch3[i] = ch2[i];
          if (random(0, 1) < 0.1) {
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

void keyPressed() {
  if (key=='k') {
    for (int i=0; i<NUMGUYS; i++) g[i].ALIVE=false;
    NUMALIVE = 0;
  }
}

