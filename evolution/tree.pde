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

  void update() {  // update feat and thresh based on chromosome
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
    thresh = (float)(chr+1)/128.0;

    //println("Update node " +id + ":\t"); 
    //println(chrome);
    //for (i=0; i<GENESIZE; i++) print(chrome[i]);  
    //println("\t" + feat + ", " + sign +", " + chr + ", " + thresh);
  }

  void Print() { // prints node info to screen
    if (leaf) println("leaf " + id +":\t"+ cat);
    else {
      print("node " + id + ":\t" + par + "\t" + feat + "\t" + sign + "\t" + thresh + "\t" + lid + "\t" + rid + "\t");
      for (int i=0; i<GENESIZE; i++) print(chrome[i]); 
      println("");
    }
  }

  int climb(float[] x) {
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
      if (random(0, 1) < 0.5) chrome[i] = 0;
      else chrome[i] = 1;
    }
    //println("TREE:");
    //println(chrome);

    update();
  }

  int decide(float[] x) {
    int curr=root;
    while (n[curr].leaf==false) curr = n[curr].climb(x);
    int ret = n[curr].cat; 
    //println("decide\t" + x[0] + "\t" + x[1] + "\t" + curr + "\t" + ret);
    return ret;
  }

  void Print() {  // prints tree info to screen 
    println("---TREE---");
    println("\tpar\tfeat\tsign\tthresh\tLeft\tRight\tGene");
    int i;
    for (i=0; i<NUMNODES; i++) n[i].Print();
    for (i=0; i<NUMCHROME; i++) print( nf(chrome[i], 0) );
    println("\n----------");
  }

  void update() {  // applies chromosome to all (non-leaf) nodes and updates
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

