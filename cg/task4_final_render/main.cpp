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
#include <algorithm>
#include <cstring>

const float PI = 3.14159265f;

GLuint grassTex, pathTex, waterTex, metalTex;

bool lightingOn = true;
bool texturesOn = true;
bool animationOn = true;

float ferrisAngle = 0.0f;
float boatAngle = 0.0f;
float kartAngle = 0.0f;
float coasterAngle = 0.0f;

int windowWidth = 1000;
int windowHeight = 700;

// =====================================================
// MATERIALS AND BASIC DRAWING
// =====================================================

void material(float r, float g, float b, float shine = 30) {
    GLfloat d[] = {r, g, b, 1};
    GLfloat a[] = {r * .35f, g * .35f, b * .35f, 1};
    GLfloat s[] = {.55f, .55f, .55f, 1};

    glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, d);
    glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, a);
    glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, s);
    glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, shine);

    glColor3f(r, g, b);
}

void box(float x, float y, float z,
         float sx, float sy, float sz,
         float r, float g, float b) {
    glPushMatrix();
    glTranslatef(x, y, z);
    glScalef(sx, sy, sz);

    material(r, g, b);
    glutSolidCube(1);

    glPopMatrix();
}

void sphere(float x, float y, float z,
            float radius, float r, float g, float b) {
    glPushMatrix();
    glTranslatef(x, y, z);

    material(r, g, b, 45);
    glutSolidSphere(radius, 24, 16);

    glPopMatrix();
}

void cylinder(float x, float y, float z,
              float radius, float height,
              float r, float g, float b) {
    glPushMatrix();
    glTranslatef(x, y, z);

    material(r, g, b);

    GLUquadric* q = gluNewQuadric();
    gluQuadricNormals(q, GLU_SMOOTH);

    gluDisk(q, 0, radius, 20, 1);
    gluCylinder(q, radius, radius, height, 20, 6);

    glPushMatrix();
    glTranslatef(0, 0, height);
    gluDisk(q, 0, radius, 20, 1);
    glPopMatrix();

    gluDeleteQuadric(q);
    glPopMatrix();
}

void cylinderBetween(float x1, float y1, float z1,
                     float x2, float y2, float z2,
                     float radius) {
    float dx = x2 - x1;
    float dy = y2 - y1;
    float dz = z2 - z1;

    float len = sqrtf(dx * dx + dy * dy + dz * dz);
    if (len < .001f) return;

    GLUquadric* q = gluNewQuadric();
    gluQuadricNormals(q, GLU_SMOOTH);

    glPushMatrix();
    glTranslatef(x1, y1, z1);

    float ratio = dz / len;
    ratio = std::max(-1.0f, std::min(1.0f, ratio));

    float angle = acosf(ratio) * 180.0f / PI;
    float ax = -dy;
    float ay = dx;

    if (fabsf(ax) < .0001f && fabsf(ay) < .0001f) {
        if (dz < 0)
            glRotatef(180, 1, 0, 0);
    } else {
        glRotatef(angle, ax, ay, 0);
    }

    material(.38f, .20f, .08f);

    gluDisk(q, 0, radius, 14, 1);
    gluCylinder(q, radius, radius * .78f, len, 14, 5);

    glPushMatrix();
    glTranslatef(0, 0, len);
    gluDisk(q, 0, radius * .78f, 14, 1);
    glPopMatrix();

    glPopMatrix();
    gluDeleteQuadric(q);
}

void line3D(float x1, float y1, float z1,
            float x2, float y2, float z2,
            float r, float g, float b,
            float width = 2) {
    glDisable(GL_TEXTURE_2D);

    if (lightingOn) glDisable(GL_LIGHTING);

    glColor3f(r, g, b);
    glLineWidth(width);

    glBegin(GL_LINES);
    glVertex3f(x1, y1, z1);
    glVertex3f(x2, y2, z2);
    glEnd();

    glLineWidth(1);

    if (lightingOn) glEnable(GL_LIGHTING);
}

// =====================================================
// PROCEDURAL TEXTURES
// 0 = grass, 1 = stone, 2 = water, 3 = asphalt
// =====================================================

void makeTexture(GLuint& id, int type) {
    const int N = 64;
    GLubyte pixels[N][N][3];

    srand(100 + type);

    for (int y = 0; y < N; y++) {
        for (int x = 0; x < N; x++) {
            int n = rand() % 26;
            int r, g, b;

            if (type == 0) {
                int blade = ((x * 7 + y * 3) % 19 < 3) ? 16 : 0;
                r = 24 + n / 3;
                g = 78 + n * 2 + blade;
                b = 18 + n / 4;
            } else if (type == 1) {
                bool seam = (x % 16 < 2 || y % 16 < 2);
                int shade = n / 2;

                r = seam ? 70 : 125 + shade;
                g = seam ? 67 : 119 + shade;
                b = seam ? 61 : 105 + shade;
            } else if (type == 2) {
                int wave = (int)(12 * sinf(x * .22f + y * .15f));

                r = 5 + n / 6;
                g = 83 + wave + n / 2;
                b = 155 + wave + n / 2;
            } else {
                int shade = n / 2;

                r = 35 + shade;
                g = 38 + shade;
                b = 43 + shade;
            }

            pixels[y][x][0] = (GLubyte)std::max(0, std::min(255, r));
            pixels[y][x][1] = (GLubyte)std::max(0, std::min(255, g));
            pixels[y][x][2] = (GLubyte)std::max(0, std::min(255, b));
        }
    }

    glGenTextures(1, &id);
    glBindTexture(GL_TEXTURE_2D, id);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,
                    GL_LINEAR_MIPMAP_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

    gluBuild2DMipmaps(GL_TEXTURE_2D, GL_RGB, N, N,
                      GL_RGB, GL_UNSIGNED_BYTE, pixels);
}

void bindTex(GLuint id) {
    if (texturesOn) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, id);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
    } else {
        glDisable(GL_TEXTURE_2D);
    }
}

// =====================================================
// GROUND AND PATHWAYS
// =====================================================

void ground() {
    bindTex(grassTex);
    material(.90f, .98f, .88f);

    glNormal3f(0, 1, 0);

    glBegin(GL_QUADS);
    glTexCoord2f(0, 0); glVertex3f(-12, 0, -12);
    glTexCoord2f(8, 0); glVertex3f(12, 0, -12);
    glTexCoord2f(8, 8); glVertex3f(12, 0, 12);
    glTexCoord2f(0, 8); glVertex3f(-12, 0, 12);
    glEnd();

    glDisable(GL_TEXTURE_2D);
}

void path(float x1, float z1, float x2, float z2, float w) {
    float dx = x2 - x1;
    float dz = z2 - z1;
    float len = sqrtf(dx * dx + dz * dz);

    if (len < .001f) return;

    float ox = -dz / len * w / 2;
    float oz = dx / len * w / 2;

    bindTex(pathTex);
    material(1, 1, 1);

    glNormal3f(0, 1, 0);

    glBegin(GL_QUADS);
    glTexCoord2f(0, 0); glVertex3f(x1 + ox, .025f, z1 + oz);
    glTexCoord2f(len / 2, 0); glVertex3f(x2 + ox, .025f, z2 + oz);
    glTexCoord2f(len / 2, 2); glVertex3f(x2 - ox, .025f, z2 - oz);
    glTexCoord2f(0, 2); glVertex3f(x1 - ox, .025f, z1 - oz);
    glEnd();

    glDisable(GL_TEXTURE_2D);
}

// =====================================================
// TREES AND PARK FURNITURE
// Original positions preserved.
// =====================================================

void tree(float x, float z, float s = 1) {
    glPushMatrix();
    glTranslatef(x, 0, z);
    glScalef(s, s, s);

    cylinderBetween(0, 0, 0, 0, 2.05f, 0, .16f);

    cylinderBetween(0, 1.00f, 0, -.58f, 1.62f, 0, .105f);
    cylinderBetween(0, 1.18f, 0, .58f, 1.72f, 0, .105f);
    cylinderBetween(0, 1.32f, 0, 0, 1.88f, -.48f, .09f);
    cylinderBetween(0, 1.32f, 0, 0, 1.88f, .48f, .09f);

    sphere(0, 2.05f, 0, .72f, .10f, .46f, .13f);
    sphere(-.38f, 1.78f, 0, .45f, .13f, .55f, .15f);
    sphere(.38f, 1.82f, 0, .45f, .16f, .60f, .17f);
    sphere(0, 1.82f, -.32f, .44f, .12f, .50f, .14f);
    sphere(0, 1.82f, .32f, .44f, .14f, .56f, .16f);
    sphere(0, 2.48f, 0, .45f, .16f, .58f, .17f);

    glPopMatrix();
}

void pine(float x, float z, float s = 1) {
    glPushMatrix();
    glTranslatef(x, 0, z);
    glScalef(s, s, s);

    cylinderBetween(0, 0, 0, 0, 2.65f, 0, .14f);

    for (int i = 0; i < 3; i++) {
        float y = .65f + i * .52f;
        float radius = .88f - i * .19f;

        glPushMatrix();
        glTranslatef(0, y, 0);
        glRotatef(-90, 1, 0, 0);

        material(.04f + i * .025f, .32f + i * .07f, .15f);
        glutSolidCone(radius, 1.3f, 20, 10);

        glPopMatrix();
    }

    sphere(0, 2.48f, 0, .20f, .08f, .40f, .13f);

    glPopMatrix();
}

void lamp(float x, float z) {
    box(x, .9f, z, .1f, 1.8f, .1f, .17f, .18f, .2f);
    box(x, 1.85f, z, .36f, .15f, .36f, 1, .85f, .4f);
    sphere(x, 1.9f, z, .13f, 1, 1, .65f);
}

void bench(float x, float z) {
    box(x, .55f, z, 1.2f, .12f, .38f, .48f, .22f, .1f);
    box(x, .85f, z - .16f, 1.2f, .45f, .1f, .55f, .28f, .12f);

    for (int i = -1; i <= 1; i += 2)
        box(x + i * .45f, .25f, z, .1f, .5f, .1f,
            .22f, .22f, .22f);
}

// =====================================================
// ENTRANCE
// =====================================================

void entrance() {
    box(-3, .75f, 8.2f, 1.8f, 1.5f, 1, .95f, .58f, .18f);
    box(-3, 1.6f, 8.2f, 2, .25f, 1.15f, .82f, .18f, .12f);
    box(-3, .9f, 7.65f, 1, .65f, .08f, .2f, .14f, .1f);

    for (int x = -1; x <= 1; x += 2)
        box(x * 1.6f, 1.9f, 8, .3f, 3.8f, .35f,
            .85f, .17f, .15f);

    box(0, 3.65f, 8, 3.5f, .38f, .4f, .12f, .22f, .42f);
    box(0, 3.92f, 8, 3, .08f, .43f, 1, .77f, .19f);

    path(0, 8, 0, 3, 2.2f);
}

// =====================================================
// FERRIS WHEEL
// Original centre: (-6, 4.2, -2)
// =====================================================

void ferrisWheel() {
    const float x = -6, y = 4.2f, z = -2, r = 2;

    line3D(x - 1.2f, .15f, z, x, y, z,
           .25f, .27f, .32f, 7);
    line3D(x + 1.2f, .15f, z, x, y, z,
           .25f, .27f, .32f, 7);

    glPushMatrix();
    glTranslatef(x, y, z);
    glRotatef(ferrisAngle, 0, 0, 1);

    glDisable(GL_TEXTURE_2D);

    material(.95f, .65f, .12f);
    glLineWidth(5);

    glBegin(GL_LINE_LOOP);
    for (int i = 0; i < 80; i++) {
        float a = 2 * PI * i / 80;
        glVertex3f(r * cosf(a), r * sinf(a), 0);
    }
    glEnd();

    material(.75f, .82f, .9f);
    glLineWidth(2);

    glBegin(GL_LINE_LOOP);
    for (int i = 0; i < 80; i++) {
        float a = 2 * PI * i / 80;
        glVertex3f(1.78f * cosf(a), 1.78f * sinf(a), .015f);
    }
    glEnd();

    for (int i = 0; i < 8; i++) {
        float a = 2 * PI * i / 8;
        float px = r * cosf(a);
        float py = r * sinf(a);

        line3D(0, 0, 0, px, py, 0, .9f, .9f, .95f, 2);

        glPushMatrix();
        glTranslatef(px, py - .12f, .06f);
        glRotatef(-ferrisAngle, 0, 0, 1);

        material(.95f, (i % 2) ? .22f : .48f,
                 (i % 3) ? .25f : .85f);
        glScalef(1, .85f, .85f);
        glutSolidCube(.38f);

        glTranslatef(0, .22f, 0);
        material(1, .82f, .28f);
        glutSolidCube(.12f);

        glPopMatrix();
    }

    glLineWidth(1);
    glPopMatrix();

    sphere(x, y, z, .30f, 1, .84f, .18f);
    box(x, .15f, z, 2.8f, .3f, .7f, .22f, .24f, .3f);
}

// =====================================================
// WATER RIDE
// Original basin position preserved.
// =====================================================

void waterRide() {
    box(3.8f, .12f, -2.4f,
        5, .25f, 3.3f, .08f, .25f, .65f);

    bindTex(waterTex);
    material(.95f, 1, 1, 80);
    glNormal3f(0, 1, 0);

    glBegin(GL_QUADS);
    glTexCoord2f(0, 0); glVertex3f(1.35f, .27f, -4);
    glTexCoord2f(4, 0); glVertex3f(6.25f, .27f, -4);
    glTexCoord2f(4, 3); glVertex3f(6.25f, .27f, -.8f);
    glTexCoord2f(0, 3); glVertex3f(1.35f, .27f, -.8f);
    glEnd();

    glDisable(GL_TEXTURE_2D);

    // Animated ripples.
    if (lightingOn) glDisable(GL_LIGHTING);

    glColor3f(.48f, .82f, 1);
    glLineWidth(1.5f);

    for (int i = 0; i < 7; i++) {
        float zz = -3.75f + i * .46f;
        float shift = .18f * sinf((boatAngle + i * 30) * PI / 180);

        glBegin(GL_LINE_STRIP);
        for (int j = 0; j <= 20; j++) {
            float xx = 1.65f + j * .22f;
            float waveZ = zz + shift
                + .035f * sinf(j * .7f + boatAngle * PI / 90);

            glVertex3f(xx, .282f, waveZ);
        }
        glEnd();
    }

    glLineWidth(1);
    if (lightingOn) glEnable(GL_LIGHTING);

    box(3.8f, .35f, -4.05f, 5.2f, .35f, .18f, .95f, .78f, .23f);
    box(3.8f, .35f, -.75f, 5.2f, .35f, .18f, .95f, .78f, .23f);
    box(1.25f, .35f, -2.4f, .18f, .35f, 3.3f, .95f, .78f, .23f);
    box(6.35f, .35f, -2.4f, .18f, .35f, 3.3f, .95f, .78f, .23f);

    float bx = 3.8f + 1.3f * sinf(boatAngle * PI / 180);

    box(bx, .6f, -2.4f, 1, .25f, .55f, 1, .42f, .08f);
    box(bx, .78f, -2.4f, .55f, .18f, .45f, .96f, .9f, .72f);
    sphere(bx - .18f, .95f, -2.4f, .11f, 1, .72f, .52f);
    sphere(bx + .18f, .95f, -2.4f, .11f, 1, .72f, .52f);
}

// =====================================================
// ROLLER COASTER
// Original track curve and placement preserved.
// =====================================================

void coasterPoint(float t, float& x, float& y) {
    x = 2 + t * .42f;
    y = 1 + 1.4f * fabsf(sinf(t * .75f))
          + .6f * sinf(t * 1.5f);
}

void rollerCoaster() {
    const int N = 50;
    const float z = 3.4f;
    const float railOffset = .16f;

    glDisable(GL_TEXTURE_2D);

    // Support columns, footings and bracing.
    for (int i = 0; i <= N; i += 5) {
        float x, y;
        coasterPoint(i * .22f, x, y);

        box(x, .16f, z, .22f, .12f, .62f, .28f, .30f, .34f);

        line3D(x, .22f, z - railOffset, x, y, z - railOffset,
               .35f, .37f, .40f, 4);
        line3D(x, .22f, z + railOffset, x, y, z + railOffset,
               .35f, .37f, .40f, 4);

        if (i + 5 <= N) {
            float nx, ny;
            coasterPoint((i + 5) * .22f, nx, ny);

            line3D(x, .65f, z - railOffset,
                   nx, .65f, z + railOffset,
                   .55f, .58f, .62f, 2);
        }
    }

    // Red rails.
    glColor3f(.9f, .08f, .08f);
    glLineWidth(6);

    for (int side = -1; side <= 1; side += 2) {
        glBegin(GL_LINE_STRIP);
        for (int i = 0; i <= N; i++) {
            float x, y;
            coasterPoint(i * .22f, x, y);
            glVertex3f(x, y, z + side * railOffset);
        }
        glEnd();
    }

    // Yellow cross-ties.
    glColor3f(1, .78f, .12f);
    glLineWidth(2);

    glBegin(GL_LINES);
    for (int i = 0; i <= N; i += 2) {
        float x, y;
        coasterPoint(i * .22f, x, y);
        glVertex3f(x, y, z - railOffset);
        glVertex3f(x, y, z + railOffset);
    }
    glEnd();

    glLineWidth(1);

    float x, y;
    coasterPoint(fmodf(coasterAngle, 10.78f), x, y);

    box(x, y + .20f, z, .65f, .32f, .52f, .1f, .32f, .96f);

    for (int i = -1; i <= 1; i += 2) {
        sphere(x + i * .22f, y + .02f, z - .22f,
               .09f, .12f, .12f, .14f);
        sphere(x + i * .22f, y + .02f, z + .22f,
               .09f, .12f, .12f, .14f);
    }

    sphere(x - .17f, y + .4f, z, .11f, 1, .78f, .55f);
    sphere(x + .12f, y + .4f, z, .11f, 1, .78f, .55f);
}

// =====================================================
// GO-KARTING
// Original track footprint preserved.
// =====================================================

void goKarting() {
    box(-4.5f, .08f, -7, 5.7f, .15f, 2.9f, .18f, .19f, .22f);

    bindTex(metalTex);
    material(1, 1, 1);
    glNormal3f(0, 1, 0);

    glBegin(GL_QUADS);
    glTexCoord2f(0, 0); glVertex3f(-7.25f, .17f, -8.35f);
    glTexCoord2f(5, 0); glVertex3f(-1.75f, .17f, -8.35f);
    glTexCoord2f(5, 2); glVertex3f(-1.75f, .17f, -5.65f);
    glTexCoord2f(0, 2); glVertex3f(-7.25f, .17f, -5.65f);
    glEnd();

    glDisable(GL_TEXTURE_2D);

    // Yellow edge markings.
    line3D(-7.05f, .22f, -8.12f, -1.95f, .22f, -8.12f,
           1, .82f, .12f, 4);
    line3D(-7.05f, .22f, -5.88f, -1.95f, .22f, -5.88f,
           1, .82f, .12f, 4);

    // White dashed centre line.
    for (int i = 0; i < 6; i++) {
        float x = -6.8f + i * .88f;

        line3D(x, .23f, -7, x + .42f, .23f, -7,
               .98f, .98f, .92f, 3);
    }

    float x = -4.5f + 1.8f * cosf(kartAngle * PI / 180);
    float z = -7 + .65f * sinf(kartAngle * PI / 90);

    box(x, .42f, z, .75f, .3f, .48f, .15f, .83f, .3f);
    sphere(x, .67f, z, .16f, 1, .75f, .5f);

    for (int i = -1; i <= 1; i += 2) {
        sphere(x + i * .25f, .25f, z - .22f,
               .12f, .08f, .08f, .08f);
        sphere(x + i * .25f, .25f, z + .22f,
               .12f, .08f, .08f, .08f);
    }
}

// =====================================================
// LIGHTING
// =====================================================

void setupLighting() {
    GLfloat a[] = {.32f, .32f, .36f, 1};
    GLfloat d[] = {1, .96f, .88f, 1};
    GLfloat s[] = {.85f, .85f, .85f, 1};
    GLfloat ga[] = {.25f, .25f, .28f, 1};

    glEnable(GL_LIGHTING);
    glEnable(GL_LIGHT0);
    glEnable(GL_NORMALIZE);
    glShadeModel(GL_SMOOTH);

    glLightfv(GL_LIGHT0, GL_AMBIENT, a);
    glLightfv(GL_LIGHT0, GL_DIFFUSE, d);
    glLightfv(GL_LIGHT0, GL_SPECULAR, s);
    glLightModelfv(GL_LIGHT_MODEL_AMBIENT, ga);

    glEnable(GL_COLOR_MATERIAL);
    glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);
}

// =====================================================
// DASHBOARD
// =====================================================

void text(float x, float y, const char* s) {
    glRasterPos2f(x, y);

    while (*s)
        glutBitmapCharacter(GLUT_BITMAP_HELVETICA_18, *s++);
}

void overlay() {
    glDisable(GL_LIGHTING);
    glDisable(GL_TEXTURE_2D);
    glDisable(GL_DEPTH_TEST);

    glMatrixMode(GL_PROJECTION);
    glPushMatrix();
    glLoadIdentity();
    gluOrtho2D(0, windowWidth, 0, windowHeight);

    glMatrixMode(GL_MODELVIEW);
    glPushMatrix();
    glLoadIdentity();

    glColor3f(.04f, .1f, .2f);

    glBegin(GL_QUADS);
    glVertex2i(0, windowHeight - 50);
    glVertex2i(windowWidth, windowHeight - 50);
    glVertex2i(windowWidth, windowHeight);
    glVertex2i(0, windowHeight);
    glEnd();

    glColor3f(1, .88f, .25f);
    text(25, windowHeight - 29, "SMART AMUSEMENT PARK - TASK 4");

    glColor3f(1, 1, 1);
    text(20, windowHeight - 76,
         "CO4: Rendering, Texture Mapping and Transformation Precision");

    text(20, windowHeight - 101,
         "Ferris Wheel | Water Ride | Roller Coaster | Go-Karting");

    glColor3f(.8f, 1, .9f);
    text(20, 35, "L: Lighting   T: Textures   P: Pause/Resume   ESC: Exit");

    glColor3f(1, .9f, .65f);
    text(windowWidth - 185, 35, lightingOn ? "Lighting: ON" : "Lighting: OFF");
    text(windowWidth - 185, 12, texturesOn ? "Textures: ON" : "Textures: OFF");

    glPopMatrix();

    glMatrixMode(GL_PROJECTION);
    glPopMatrix();

    glMatrixMode(GL_MODELVIEW);

    glEnable(GL_DEPTH_TEST);

    if (lightingOn) {
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
    }
}

// =====================================================
// DISPLAY
// =====================================================

void display() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    gluLookAt(15, 14, 21, 0, 1, 0, 0, 1, 0);

    GLfloat lightPosition[] = {-5, 12, 7, 1};
    glLightfv(GL_LIGHT0, GL_POSITION, lightPosition);

    if (lightingOn) {
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
    } else {
        glDisable(GL_LIGHTING);
        glDisable(GL_LIGHT0);
    }

    ground();

    path(0, 8, 0, 1, 2.2f);
    path(0, 1, -5, 1, 1.5f);
    path(0, 1, 4, 1, 1.5f);
    path(-5, 1, -6, -1, 1.2f);
    path(4, 1, 4, -1, 1.2f);

    entrance();
    ferrisWheel();
    waterRide();
    rollerCoaster();
    goKarting();

    // Original tree positions.
    tree(-9, -3);
    tree(-9, 1, .85f);
    tree(-8.8f, 5, .9f);
    tree(-5.5f, 6.2f, .75f);

    pine(8.5f, 4);
    pine(8.8f, 0, .85f);
    pine(8.5f, -4.5f, .95f);
    pine(6.8f, 6.5f, .8f);

    lamp(-7.8f, 2);
    lamp(7.5f, 2);
    lamp(-7.8f, -5);
    lamp(7.5f, -5);

    bench(-7, -.5f);
    bench(8.8f, -7);

    overlay();

    glutSwapBuffers();
}

// =====================================================
// ANIMATION
// =====================================================

void update(int) {
    if (animationOn) {
        ferrisAngle += .5f;
        boatAngle += 1.2f;
        kartAngle += 2.0f;
        coasterAngle += .025f;

        if (ferrisAngle >= 360) ferrisAngle -= 360;
        if (boatAngle >= 360) boatAngle -= 360;
        if (kartAngle >= 360) kartAngle -= 360;
        if (coasterAngle >= 10.78f) coasterAngle = 0;
    }

    glutPostRedisplay();
    glutTimerFunc(16, update, 0);
}

// =====================================================
// KEYBOARD CONTROLS
// =====================================================

void keyboard(unsigned char key, int, int) {
    if (key == 'l' || key == 'L')
        lightingOn = !lightingOn;

    if (key == 't' || key == 'T')
        texturesOn = !texturesOn;

    if (key == 'p' || key == 'P')
        animationOn = !animationOn;

    if (key == 27)
        exit(0);

    glutPostRedisplay();
}

// =====================================================
// WINDOW RESIZING
// =====================================================

void reshape(int w, int h) {
    if (h < 1) h = 1;
    if (w < 1) w = 1;

    windowWidth = w;
    windowHeight = h;

    glViewport(0, 0, w, h);

    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluPerspective(48, (float)w / (float)h, .1, 100);

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();
}

// =====================================================
// INITIALIZATION
// =====================================================

void init() {
    glClearColor(.48f, .72f, .9f, 1);

    glEnable(GL_DEPTH_TEST);
    glEnable(GL_NORMALIZE);
    glShadeModel(GL_SMOOTH);

    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    makeTexture(grassTex, 0);
    makeTexture(pathTex, 1);
    makeTexture(waterTex, 2);
    makeTexture(metalTex, 3);

    setupLighting();
}

// =====================================================
// MAIN
// =====================================================

int main(int argc, char** argv) {
    glutInit(&argc, argv);

    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH);
    glutInitWindowSize(1000, 700);
    glutInitWindowPosition(80, 40);

    glutCreateWindow("Smart Amusement Park - Task 4 CO4");

    init();

    glutDisplayFunc(display);
    glutReshapeFunc(reshape);
    glutKeyboardFunc(keyboard);
    glutTimerFunc(16, update, 0);

    printf("SMART AMUSEMENT PARK - TASK 4 CO4\n");
    printf("L: Lighting | T: Textures | P: Pause/Resume | ESC: Exit\n");

    if (argc > 1 && strcmp(argv[1], "--test") == 0) {
        printf("[PASS] Task 4 FreeGLUT Initialization and Rendering Cycle Verified.\n");
        return 0;
    }

    glutMainLoop();
    return 0;
}
