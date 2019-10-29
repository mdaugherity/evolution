class Ball {
  float x;
  float y;
  int rad;

  float vx;
  float vy;
  float g = 0.15;

  Ball(float ix, float iy, int irad, float ivx) {
    x = ix;
    y = iy;
    rad = irad;
    vx = ivx;
    vy = 0;
  }

  void reset() {
    //x = random(10, width-10);
    x = BALLX;
    //y = 10;
    y = BALLY;
    vx = 5.0;
    vy = 0;
    //if ( random(0, 1)<0.5 ) vx = -5.0;
    //println("RESET: " + x + ",    " + vx);
  }

  void update() {
    x += vx;
    y += vy;

    vy+=g;

    if (x<0) {   // letting the ball reach corners
      //x = rad;
      x = 0;
      vx *= -0.99;
    }
    if (x>width) {  // letting the ball reach corners
      //x = width-rad;
      x = width;
      vx *= -0.99;
    }
    if (y>height-rad) {
      //y -= vy;
      y = height - rad;
      vy -= g;

      vy *= -0.985;
    }
  }

  void draw() {
    ellipseMode(RADIUS);
    fill(255);
    ellipse(x, y, rad, rad);
  }
}

