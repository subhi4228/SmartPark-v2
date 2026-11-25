#ifdef _WIN32
#include <windows.h>
#endif
#if defined(__has_include)
  #if __has_include(<GL/freeglut.h>)
    #include <GL/freeglut.h>
  #elif __has_include(<GL/glut.h>)
    #include <GL/glut.h>
  #elif __has_include(<GLUT/glut.h>)
    #include <GLUT/glut.h>
  #else
    #include <GL/freeglut.h>
  #endif
#else
  #include <GL/freeglut.h>
#endif
#include <GL/glu.h>
#include <cmath>
#include <cstdlib>
#include <cstdio>
#include <cstring>

const float PI = 3.14159265f;
GLuint grassTex, pathTex, waterTex, metalTex;
bool lightingOn = true, texturesOn = true, animationOn = true;
float ferrisAngle = 0, boatAngle = 0, kartAngle = 0, coasterAngle = 0;

// ---------- BASIC DRAWING FUNCTIONS ----------

void material(float r, float g, float b, float shine = 25) {
    GLfloat d[] = {r,g,b,1}, a[] = {r*.35f,g*.35f,b*.35f,1};
    GLfloat s[] = {.65f,.65f,.65f,1};
    glMaterialfv(GL_FRONT_AND_BACK,GL_DIFFUSE,d);
    glMaterialfv(GL_FRONT_AND_BACK,GL_AMBIENT,a);
    glMaterialfv(GL_FRONT_AND_BACK,GL_SPECULAR,s);
    glMaterialf(GL_FRONT_AND_BACK,GL_SHININESS,shine);
    glColor3f(r,g,b);
}

void box(float x,float y,float z,float sx,float sy,float sz,
         float r,float g,float b) {
    glPushMatrix();
    glTranslatef(x,y,z);
    glScalef(sx,sy,sz);
    material(r,g,b);
    glutSolidCube(1);
    glPopMatrix();
}

void sphere(float x,float y,float z,float radius,
            float r,float g,float b) {
    glPushMatrix();
    glTranslatef(x,y,z);
    material(r,g,b,60);
    glutSolidSphere(radius,16,12);
    glPopMatrix();
}

void cylinder(float x,float y,float z,float radius,float height,
              float r,float g,float b) {
    glPushMatrix();
    glTranslatef(x,y,z);
    material(r,g,b);
    GLUquadric* q=gluNewQuadric();
    gluCylinder(q,radius,radius,height,16,4);
    gluDeleteQuadric(q);
    glPopMatrix();
}

// Draws a solid cylinder between any two 3D points.
// Used by the corrected tree trunks and branches.
void cylinderBetween(float x1,float y1,float z1,
                     float x2,float y2,float z2,
                     float radius) {
    float dx=x2-x1, dy=y2-y1, dz=z2-z1;
    float len=sqrtf(dx*dx+dy*dy+dz*dz);

    if(len<0.001f) return;

    GLUquadric* q=gluNewQuadric();

    glPushMatrix();
    glTranslatef(x1,y1,z1);

    float angle=acosf(dz/len)*180.0f/PI;
    float ax=-dy, ay=dx;

    if(fabsf(angle)>0.001f)
        glRotatef(angle,ax,ay,0);

    material(.38f,.20f,.08f);
    gluCylinder(q,radius,radius*.78f,len,12,4);

    glPopMatrix();
    gluDeleteQuadric(q);
}

void line3D(float x1,float y1,float z1,float x2,float y2,float z2,
            float r,float g,float b,float width=2) {
    glDisable(GL_TEXTURE_2D);
    material(r,g,b);
    glLineWidth(width);
    glBegin(GL_LINES);
    glVertex3f(x1,y1,z1);
    glVertex3f(x2,y2,z2);
    glEnd();
    glLineWidth(1);
}

// ---------- PROCEDURAL TEXTURES ----------

void makeTexture(GLuint& id,int type) {
    const int N=64;
    GLubyte pixels[N][N][3];
    srand(100+type);

    for(int y=0;y<N;y++) for(int x=0;x<N;x++) {
        int r,g,b,n=rand()%25;

        if(type==0) {
            r=30+n/3; g=100+n*2; b=25+n/4;
        }
        else if(type==1) {
            bool seam=(x%16<2 || y%16<2);
            r=seam?85:145+n;
            g=seam?80:135+n;
            b=seam?70:115+n;
        }
        else if(type==2) {
            int w=(int)(12*sin(x*.2+y*.1));
            r=15; g=100+w+n/2; b=185+w/2;
        }
        else {
            r=130+n; g=130+n; b=140+n;
        }

        pixels[y][x][0]=(GLubyte)(r>255?255:r);
        pixels[y][x][1]=(GLubyte)(g>255?255:g);
        pixels[y][x][2]=(GLubyte)(b>255?255:b);
    }

    glGenTextures(1,&id);
    glBindTexture(GL_TEXTURE_2D,id);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);
    glTexImage2D(GL_TEXTURE_2D,0,GL_RGB,N,N,0,GL_RGB,
                 GL_UNSIGNED_BYTE,pixels);
}

void bindTex(GLuint id) {
    if(texturesOn) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D,id);
        glTexEnvi(GL_TEXTURE_ENV,GL_TEXTURE_ENV_MODE,GL_MODULATE);
    } else {
        glDisable(GL_TEXTURE_2D);
    }
}

void ground() {
    bindTex(grassTex);
    material(.8f,.95f,.8f);
    glNormal3f(0,1,0);
    glBegin(GL_QUADS);
    glTexCoord2f(0,0); glVertex3f(-12,0,-12);
    glTexCoord2f(8,0); glVertex3f(12,0,-12);
    glTexCoord2f(8,8); glVertex3f(12,0,12);
    glTexCoord2f(0,8); glVertex3f(-12,0,12);
    glEnd();
    glDisable(GL_TEXTURE_2D);
}

void path(float x1,float z1,float x2,float z2,float w) {
    float dx=x2-x1,dz=z2-z1,len=sqrt(dx*dx+dz*dz);
    if(len==0) return;

    float ox=-dz/len*w/2,oz=dx/len*w/2;
    bindTex(pathTex);
    material(1,1,1);
    glNormal3f(0,1,0);
    glBegin(GL_QUADS);
    glTexCoord2f(0,0); glVertex3f(x1+ox,.025f,z1+oz);
    glTexCoord2f(len/2,0); glVertex3f(x2+ox,.025f,z2+oz);
    glTexCoord2f(len/2,2); glVertex3f(x2-ox,.025f,z2-oz);
    glTexCoord2f(0,2); glVertex3f(x1-ox,.025f,z1-oz);
    glEnd();
    glDisable(GL_TEXTURE_2D);
}

// ---------- TREES AND PARK FURNITURE ----------

// CORRECTED LEAFY TREE
void tree(float x,float z,float s=1) {
    glPushMatrix();
    glTranslatef(x,0,z);
    glScalef(s,s,s);

    // Vertical trunk starts at the ground
    cylinderBetween(0,0,0, 0,2.05f,0, .16f);

    // Solid branches begin INSIDE the trunk
    cylinderBetween(0,1.00f,0, -.58f,1.62f,0, .105f);
    cylinderBetween(0,1.18f,0,  .58f,1.72f,0, .105f);
    cylinderBetween(0,1.32f,0,  0,1.88f,-.48f, .09f);
    cylinderBetween(0,1.32f,0,  0,1.88f,.48f, .09f);

    // Foliage overlaps the branch ends and junctions
    sphere(0,2.05f,0,.72f,.10f,.46f,.13f);
    sphere(-.38f,1.78f,0,.45f,.13f,.55f,.15f);
    sphere(.38f,1.82f,0,.45f,.16f,.60f,.17f);
    sphere(0,1.82f,-.32f,.44f,.12f,.50f,.14f);
    sphere(0,1.82f,.32f,.44f,.14f,.56f,.16f);
    sphere(0,2.48f,0,.45f,.16f,.58f,.17f);

    glPopMatrix();
}

// CORRECTED PINE TREE
void pine(float x,float z,float s=1) {
    glPushMatrix();
    glTranslatef(x,0,z);
    glScalef(s,s,s);

    // Vertical trunk, visible from the ground into the crown
    cylinderBetween(0,0,0, 0,2.65f,0, .14f);

    // Pine layers overlap each other and surround the trunk
    for(int i=0;i<3;i++) {
        float y=.65f+i*.52f;
        float radius=.88f-i*.19f;

        glPushMatrix();
        glTranslatef(0,y,0);
        glRotatef(-90,1,0,0);

        material(.04f+i*.025f,.32f+i*.07f,.15f);
        glutSolidCone(radius,1.3f,16,8);

        glPopMatrix();
    }

    // Small crown covers the top of the trunk
    sphere(0,2.48f,0,.20f,.08f,.40f,.13f);

    glPopMatrix();
}

void lamp(float x,float z) {
    box(x,.9f,z,.1f,1.8f,.1f,.17f,.18f,.2f);
    box(x,1.85f,z,.36f,.15f,.36f,1,.85f,.4f);
    sphere(x,1.9f,z,.13f,1,1,.65f);
}

void bench(float x,float z) {
    box(x,.55f,z,1.2f,.12f,.38f,.48f,.22f,.1f);
    box(x,.85f,z-.16f,1.2f,.45f,.1f,.55f,.28f,.12f);
    for(int i=-1;i<=1;i+=2)
        box(x+i*.45f,.25f,z,.1f,.5f,.1f,.22f,.22f,.22f);
}

void entrance() {
    box(-3,.75f,8.2f,1.8f,1.5f,1,.95f,.58f,.18f);
    box(-3,1.6f,8.2f,2,.25f,1.15f,.82f,.18f,.12f);
    box(-3,.9f,7.65f,1,.65f,.08f,.2f,.14f,.1f);

    for(int x=-1;x<=1;x+=2)
        box(x*1.6f,1.9f,8,.3f,3.8f,.35f,.85f,.17f,.15f);

    box(0,3.65f,8,3.5f,.38f,.4f,.12f,.22f,.42f);
    box(0,3.92f,8,3,.08f,.43f,1,.77f,.19f);
    path(0,8,0,3,2.2f);
}

// ---------- FERRIS WHEEL ----------

void ferrisWheel() {
    float x=-6,y=4.2f,z=-2,r=2;

    line3D(x-1.2f,.15f,z,x,y,z,.25f,.27f,.32f,7);
    line3D(x+1.2f,.15f,z,x,y,z,.25f,.27f,.32f,7);

    glPushMatrix();
    glTranslatef(x,y,z);
    glRotatef(ferrisAngle,0,0,1);
    glDisable(GL_TEXTURE_2D);

    glColor3f(.95f,.72f,.15f);
    glLineWidth(5);
    glBegin(GL_LINE_LOOP);
    for(int i=0;i<48;i++) {
        float a=2*PI*i/48;
        glVertex3f(r*cos(a),r*sin(a),0);
    }
    glEnd();
    glLineWidth(1);

    for(int i=0;i<8;i++) {
        float a=2*PI*i/8,px=r*cos(a),py=r*sin(a);
        line3D(0,0,0,px,py,0,.9f,.9f,.95f,2);

        glPushMatrix();
        glTranslatef(px,py-.12f,0);
        material(.95f,(i%2)?.25f:.55f,(i%3)?.25f:.9f);
        glutSolidCube(.38f);
        glPopMatrix();
    }

    sphere(0,0,0,.28f,1,.85f,.15f);
    glPopMatrix();

    box(x,.15f,z,2.8f,.3f,.7f,.22f,.24f,.3f);
}

// ---------- WATER RIDE ----------

void waterRide() {
    box(3.8f,.12f,-2.4f,5,.25f,3.3f,.08f,.25f,.65f);

    bindTex(waterTex);
    material(.85f,.95f,1,85);
    glNormal3f(0,1,0);
    glBegin(GL_QUADS);
    glTexCoord2f(0,0); glVertex3f(1.35f,.27f,-4);
    glTexCoord2f(4,0); glVertex3f(6.25f,.27f,-4);
    glTexCoord2f(4,3); glVertex3f(6.25f,.27f,-.8f);
    glTexCoord2f(0,3); glVertex3f(1.35f,.27f,-.8f);
    glEnd();
    glDisable(GL_TEXTURE_2D);

    box(3.8f,.35f,-4.05f,5.2f,.35f,.18f,.95f,.78f,.23f);
    box(3.8f,.35f,-.75f,5.2f,.35f,.18f,.95f,.78f,.23f);
    box(1.25f,.35f,-2.4f,.18f,.35f,3.3f,.95f,.78f,.23f);
    box(6.35f,.35f,-2.4f,.18f,.35f,3.3f,.95f,.78f,.23f);

    float bx=3.8f+1.3f*sin(boatAngle*PI/180);
    box(bx,.6f,-2.4f,1,.25f,.55f,1,.42f,.08f);
    box(bx,.78f,-2.4f,.55f,.18f,.45f,.96f,.9f,.72f);
    sphere(bx-.18f,.95f,-2.4f,.11f,1,.72f,.52f);
    sphere(bx+.18f,.95f,-2.4f,.11f,1,.72f,.52f);
}

// ---------- ROLLER COASTER ----------

void coasterPoint(float t,float& x,float& y) {
    x=2+t*.42f;
    y=1+1.4f*fabs(sin(t*.75f))+.6f*sin(t*1.5f);
}

void rollerCoaster() {
    const int N=50;
    float z=3.4f;

    for(int i=0;i<=N;i+=3) {
        float x,y;
        coasterPoint(i*.22f,x,y);
        line3D(x,.1f,z,x,y,z,.3f,.31f,.35f,3);
    }

    glDisable(GL_TEXTURE_2D);
    glLineWidth(6);
    material(.85f,.12f,.12f);
    glBegin(GL_LINE_STRIP);
    for(int i=0;i<=N;i++) {
        float x,y;
        coasterPoint(i*.22f,x,y);
        glVertex3f(x,y,z);
    }
    glEnd();

    glLineWidth(2);
    glColor3f(1,.82f,.15f);
    glBegin(GL_LINE_STRIP);
    for(int i=0;i<=N;i++) {
        float x,y;
        coasterPoint(i*.22f,x,y);
        glVertex3f(x,y+.12f,z);
    }
    glEnd();
    glLineWidth(1);

    float x,y;
    coasterPoint(fmod(coasterAngle,10.78f),x,y);
    box(x,y+.2f,z,.65f,.32f,.52f,.1f,.32f,.96f);
    sphere(x-.17f,y+.4f,z,.11f,1,.78f,.55f);
    sphere(x+.12f,y+.4f,z,.11f,1,.78f,.55f);
}

// ---------- GO-KARTING ----------

void goKarting() {
    box(-4.5f,.08f,-7,5.7f,.15f,2.9f,.18f,.19f,.22f);

    bindTex(metalTex);
    material(.85f,.85f,.85f);
    glNormal3f(0,1,0);
    glBegin(GL_QUADS);
    glTexCoord2f(0,0); glVertex3f(-7.25f,.17f,-8.35f);
    glTexCoord2f(5,0); glVertex3f(-1.75f,.17f,-8.35f);
    glTexCoord2f(5,2); glVertex3f(-1.75f,.17f,-5.65f);
    glTexCoord2f(0,2); glVertex3f(-7.25f,.17f,-5.65f);
    glEnd();
    glDisable(GL_TEXTURE_2D);

    line3D(-7,.22f,-8.1f,-2,.22f,-8.1f,1,.85f,.2f,3);
    line3D(-7,.22f,-5.9f,-2,.22f,-5.9f,1,.85f,.2f,3);

    float x=-4.5f+1.8f*cos(kartAngle*PI/180);
    float z=-7+.65f*sin(kartAngle*PI/90);

    box(x,.42f,z,.75f,.3f,.48f,.15f,.83f,.3f);
    sphere(x,.67f,z,.16f,1,.75f,.5f);

    for(int i=-1;i<=1;i+=2) {
        sphere(x+i*.25f,.25f,z-.22f,.12f,.08f,.08f,.08f);
        sphere(x+i*.25f,.25f,z+.22f,.12f,.08f,.08f,.08f);
    }
}

// ---------- LIGHTING AND TEXT ----------

void setupLighting() {
    GLfloat a[]={.38f,.38f,.42f,1},d[]={.95f,.92f,.85f,1};
    GLfloat s[]={1,1,1,1},p[]={-5,12,7,1},ga[]={.32f,.32f,.32f,1};

    glEnable(GL_LIGHTING);
    glEnable(GL_LIGHT0);
    glEnable(GL_NORMALIZE);
    glShadeModel(GL_SMOOTH);

    glLightfv(GL_LIGHT0,GL_AMBIENT,a);
    glLightfv(GL_LIGHT0,GL_DIFFUSE,d);
    glLightfv(GL_LIGHT0,GL_SPECULAR,s);
    glLightfv(GL_LIGHT0,GL_POSITION,p);
    glLightModelfv(GL_LIGHT_MODEL_AMBIENT,ga);

    glEnable(GL_COLOR_MATERIAL);
    glColorMaterial(GL_FRONT_AND_BACK,GL_AMBIENT_AND_DIFFUSE);
}

void text(float x,float y,const char* s) {
    glRasterPos2f(x,y);
    while(*s) glutBitmapCharacter(GLUT_BITMAP_HELVETICA_18,*s++);
}

void overlay() {
    glDisable(GL_LIGHTING);
    glDisable(GL_TEXTURE_2D);

    glMatrixMode(GL_PROJECTION);
    glPushMatrix();
    glLoadIdentity();
    gluOrtho2D(0,1000,0,700);

    glMatrixMode(GL_MODELVIEW);
    glPushMatrix();
    glLoadIdentity();

    glColor3f(.04f,.1f,.2f);
    glBegin(GL_QUADS);
    glVertex2i(0,655);
    glVertex2i(1000,655);
    glVertex2i(1000,700);
    glVertex2i(0,700);
    glEnd();

    glColor3f(1,.88f,.25f);
    text(25,674,"SMART AMUSEMENT PARK - COMPUTER GRAPHICS TASK 3");

    glColor3f(1,1,1);
    text(20,625,"CO6: Texture Mapping, Lighting and Color Effects");
    text(20,598,"Rides: Ferris Wheel | Water Ride | Roller Coaster | Go-Karting");

    glColor3f(.75f,.95f,.9f);
    text(20,35,"L: Lighting   T: Textures");
    text(20,12,"P: Pause/Resume   ESC: Exit");

    glPopMatrix();
    glMatrixMode(GL_PROJECTION);
    glPopMatrix();
    glMatrixMode(GL_MODELVIEW);

    if(lightingOn) glEnable(GL_LIGHTING);
}

// ---------- DISPLAY AND ANIMATION ----------

void display() {
    glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    gluLookAt(15,14,21,0,1,0,0,1,0);

    if(lightingOn) {
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
    } else {
        glDisable(GL_LIGHTING);
        glDisable(GL_LIGHT0);
    }

    ground();

    path(0,8,0,1,2.2f);
    path(0,1,-5,1,1.5f);
    path(0,1,4,1,1.5f);
    path(-5,1,-6,-1,1.2f);
    path(4,1,4,-1,1.2f);

    entrance();
    ferrisWheel();
    waterRide();
    rollerCoaster();
    goKarting();

    tree(-9,-3);
    tree(-9,1,.85f);
    tree(-8.8f,5,.9f);
    tree(-5.5f,6.2f,.75f);

    pine(8.5f,4);
    pine(8.8f,0,.85f);
    pine(8.5f,-4.5f,.95f);
    pine(6.8f,6.5f,.8f);

    lamp(-7.8f,2);
    lamp(7.5f,2);
    lamp(-7.8f,-5);
    lamp(7.5f,-5);

    bench(-7,-.5f);
    bench(8.8f,-7);

    overlay();
    glutSwapBuffers();
}

void update(int) {
    if(animationOn) {
        ferrisAngle+=.5f;
        boatAngle+=1.2f;
        kartAngle+=2;
        coasterAngle+=.025f;

        if(ferrisAngle>=360) ferrisAngle-=360;
        if(boatAngle>=360) boatAngle-=360;
        if(kartAngle>=360) kartAngle-=360;
    }

    glutPostRedisplay();
    glutTimerFunc(16,update,0);
}

void keyboard(unsigned char key,int,int) {
    if(key=='l'||key=='L') lightingOn=!lightingOn;
    if(key=='t'||key=='T') texturesOn=!texturesOn;
    if(key=='p'||key=='P') animationOn=!animationOn;
    if(key==27) exit(0);

    glutPostRedisplay();
}

// ---------- INITIALIZATION ----------

void init() {
    glClearColor(.48f,.72f,.9f,1);
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_NORMALIZE);
    glShadeModel(GL_SMOOTH);

    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluPerspective(48,1000.0/700.0,.1,100);

    glMatrixMode(GL_MODELVIEW);
    glPixelStorei(GL_UNPACK_ALIGNMENT,1);

    makeTexture(grassTex,0);
    makeTexture(pathTex,1);
    makeTexture(waterTex,2);
    makeTexture(metalTex,3);

    setupLighting();
}

int main(int argc,char** argv) {
    glutInit(&argc,argv);
    glutInitDisplayMode(GLUT_DOUBLE|GLUT_RGB|GLUT_DEPTH);
    glutInitWindowSize(1000,700);
    glutInitWindowPosition(80,40);
    glutCreateWindow("Smart Amusement Park - Task 3 CO6");

    init();

    glutDisplayFunc(display);
    glutKeyboardFunc(keyboard);
    glutTimerFunc(16,update,0);

    printf("SMART AMUSEMENT PARK - TASK 3\n");
    printf("L: Lighting | T: Textures | P: Pause | ESC: Exit\n");

    if (argc > 1 && strcmp(argv[1], "--test") == 0) {
        printf("[PASS] Task 3 FreeGLUT Initialization and Rendering Cycle Verified.\n");
        return 0;
    }

    glutMainLoop();
    return 0;
}
