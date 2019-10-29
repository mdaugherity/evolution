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

  void reset() {  // bring us back to life
    ALIVE = true;
    x = width/2;
    vx = speed;
    score = 0;
    life = 0;
    distx = 0;
    dist2x = 0;
    t.update();  // updates tree based on new chrome
  }

  void update() {
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
        else vx= -1.0*speed;

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
        score = life; // XXX trying score as lifetime
        if (life%4==0) frame++;
        if (frame>=FRAMES) frame=0;
      }
    }
  }

  void draw() {

    if (ALIVE) {

      //DEBUG - show thresholds on screen
      if (id==0) {
        int i;
        for (i=0; i<t.NUMNODES; i++) {
          if (t.n[i].leaf==false) {
            if (t.n[i].feat==0) stroke(0, 255, 0); // green is for guy
            if (t.n[i].feat==1) stroke(0, 0, 255); // blue is for ball
            int tx = int(t.n[i].thresh*width);
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

  void drawStatus() {  // prints running status update
    String s = "";
    if(id<10) s+=" ";
    s += nf(id, 0) + ": ";

    if (countdown<0) {  // generation is active
      if (ALIVE) s+="Alive";
      else s+="Dead ";
      s+="  " + int(score);
    }
    else {  // generation is all dead, show new chrome
       fill(200, 40, 220); // XXX reset, move down
       int i;
       for(i=0; i<t.NUMCHROME; i++) s+=t.chrome[i];  // XXX fill across... base on countdown?
    }    
    
    textFont(gfont, 12);
    textAlign(LEFT, CENTER);
    if (ALIVE) fill(160);    
    else fill(30);
    if (ALIVE && id==0) fill(155, 163, 255);
    if (ALIVE && id==1) fill(255, 157, 237);
    text(s, statusx, statusy);
  }
}

