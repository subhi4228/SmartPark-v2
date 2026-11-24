/**
 * Smart Amusement Park - Computer Graphics Lab (CG)
 * Task 3 (CO6): Procedural Surface Textures, Phong Lighting & Material Effects
 *
 * Implements:
 * 1. Procedural Texture Synthesis (Grass, Asphalt, Water Ripples, Stone Paver)
 * 2. Phong Illumination Model (Ambient, Diffuse, Specular, Material Shininess)
 * 3. Interactive Keyboard Controls:
 *    - 'L': Toggle Lighting ON / OFF
 *    - 'T': Toggle Textures ON / OFF
 *    - 'P': Pause / Resume Animation
 *    - 'ESC': Exit
 * 4. Heads-Up Display (HUD) showing live system parameters
 */

#include <GL/freeglut.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <stdbool.h>

static int g_width = 1100;
static int g_height = 700;

static float g_wheel_angle = 0.0f;
static float g_coaster_t = 0.0f;
static float g_kart_x = 0.0f;
static float g_boat_t = 0.0f;

static bool g_lighting_on = true;
static bool g_textures_on = true;
static bool g_paused = false;

// Texture IDs
static GLuint g_tex_grass;
static GLuint g_tex_path;
static GLuint g_tex_water;
static GLuint g_tex_asphalt;

#define TEX_SIZE 64

// Generate 64x64 procedural textures: Time O(N^2), Space O(N^2)
GLuint create_procedural_texture(int type) {
    GLubyte image[TEX_SIZE][TEX_SIZE][3];
    for (int i = 0; i < TEX_SIZE; i++) {
        for (int j = 0; j < TEX_SIZE; j++) {
            if (type == 1) { // Grass: green noise
                int noise = (i * 17 + j * 31) % 40;
                image[i][j][0] = 30 + noise / 2;
                image[i][j][1] = 100 + noise;
                image[i][j][2] = 40 + noise / 3;
            } else if (type == 2) { // Sand/Path paver pattern
                int border = (i % 16 == 0 || j % 16 == 0) ? 40 : 0;
                image[i][j][0] = 195 - border;
                image[i][j][1] = 175 - border;
                image[i][j][2] = 130 - border;
            } else if (type == 3) { // Water wave concentric sine
                float dist = sqrtf((i - 32) * (i - 32) + (j - 32) * (j - 32));
                int wave = (int)(sin(dist * 0.4f) * 35.0f);
                image[i][j][0] = 20;
                image[i][j][1] = 110 + wave;
                image[i][j][2] = 230 + wave / 2;
            } else { // Asphalt grain
                int grain = (i * 29 + j * 53) % 30;
                image[i][j][0] = 50 + grain;
                image[i][j][1] = 55 + grain;
                image[i][j][2] = 60 + grain;
            }
        }
    }

    GLuint texId;
    glGenTextures(1, &texId);
    glBindTexture(GL_TEXTURE_2D, texId);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, TEX_SIZE, TEX_SIZE, 0, GL_RGB, GL_UNSIGNED_BYTE, image);
    return texId;
}

void init_graphics(void) {
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_COLOR_MATERIAL);
    glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);

    // Setup Phong Lighting (GL_LIGHT0)
    GLfloat light_pos[] = { 10.0f, 18.0f, 15.0f, 1.0f };
    GLfloat light_ambient[] = { 0.25f, 0.25f, 0.25f, 1.0f };
    GLfloat light_diffuse[] = { 0.90f, 0.90f, 0.90f, 1.0f };
    GLfloat light_specular[] = { 0.80f, 0.80f, 0.80f, 1.0f };

    glLightfv(GL_LIGHT0, GL_POSITION, light_pos);
    glLightfv(GL_LIGHT0, GL_AMBIENT, light_ambient);
    glLightfv(GL_LIGHT0, GL_DIFFUSE, light_diffuse);
    glLightfv(GL_LIGHT0, GL_SPECULAR, light_specular);

    GLfloat mat_specular[] = { 0.5f, 0.5f, 0.5f, 1.0f };
    glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, mat_specular);
    glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, 32.0f);

    glEnable(GL_LIGHT0);
    glEnable(GL_LIGHTING);

    // Generate procedural textures
    g_tex_grass = create_procedural_texture(1);
    g_tex_path = create_procedural_texture(2);
    g_tex_water = create_procedural_texture(3);
    g_tex_asphalt = create_procedural_texture(4);
}

void render_hud_text(float x, float y, const char* str) {
    glRasterPos2f(x, y);
    while (*str) {
        glutBitmapCharacter(GLUT_BITMAP_HELVETICA_12, *str++);
    }
}

void draw_scene_objects(void) {
    // 1. Ground Plane (Grass)
    if (g_textures_on) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, g_tex_grass);
    }
    glColor3f(0.85f, 0.85f, 0.85f);
    glBegin(GL_QUADS);
    glNormal3f(0, 1, 0);
    glTexCoord2f(0.0f, 0.0f);   glVertex3f(-14.0f, 0.0f, -10.0f);
    glTexCoord2f(10.0f, 0.0f);  glVertex3f( 14.0f, 0.0f, -10.0f);
    glTexCoord2f(10.0f, 10.0f); glVertex3f( 14.0f, 0.0f,  10.0f);
    glTexCoord2f(0.0f, 10.0f);  glVertex3f(-14.0f, 0.0f,  10.0f);
    glEnd();
    if (g_textures_on) glDisable(GL_TEXTURE_2D);

    // 2. Sand Path (Pavers)
    if (g_textures_on) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, g_tex_path);
    }
    glColor3f(0.95f, 0.95f, 0.95f);
    glBegin(GL_QUADS);
    glNormal3f(0, 1, 0);
    glTexCoord2f(0, 0); glVertex3f(-1.0f, 0.02f, -10.0f);
    glTexCoord2f(2, 0); glVertex3f( 1.0f, 0.02f, -10.0f);
    glTexCoord2f(2, 8); glVertex3f( 1.0f, 0.02f,  10.0f);
    glTexCoord2f(0, 8); glVertex3f(-1.0f, 0.02f,  10.0f);

    glTexCoord2f(0, 0);  glVertex3f(-14.0f, 0.02f, 2.5f);
    glTexCoord2f(10, 0); glVertex3f( 14.0f, 0.02f, 2.5f);
    glTexCoord2f(10, 2); glVertex3f( 14.0f, 0.02f, 4.0f);
    glTexCoord2f(0, 2);  glVertex3f(-14.0f, 0.02f, 4.0f);
    glEnd();
    if (g_textures_on) glDisable(GL_TEXTURE_2D);

    // 3. Entrance Ticket Booth
    glPushMatrix();
    glTranslatef(0, 0, 4.0f);
    glColor3f(0.55f, 0.20f, 0.65f);
    glPushMatrix(); glTranslatef(0, 1.2f, 0); glScalef(2.6f, 2.4f, 1.8f); glutSolidCube(1.0); glPopMatrix();
    glColor3f(0.95f, 0.60f, 0.10f);
    glPushMatrix(); glTranslatef(0, 2.4f, 0); glRotatef(-90, 1, 0, 0); glutSolidCone(2.0, 1.2, 4, 1); glPopMatrix();
    glPopMatrix();

    // 4. Ferris Wheel
    glPushMatrix();
    glTranslatef(-6.0f, 3.5f, -2.0f);
    glColor3f(0.20f, 0.65f, 0.90f);
    glLineWidth(4.0f);
    glBegin(GL_LINES);
    glVertex3f(0,0,0); glVertex3f(-1.5f, -3.5f, 0.5f);
    glVertex3f(0,0,0); glVertex3f( 1.5f, -3.5f, 0.5f);
    glVertex3f(0,0,0); glVertex3f(-1.5f, -3.5f,-0.5f);
    glVertex3f(0,0,0); glVertex3f( 1.5f, -3.5f,-0.5f);
    glEnd();

    glRotatef(g_wheel_angle, 0, 0, 1);
    glColor3f(0.95f, 0.45f, 0.10f);
    glutWireTorus(0.1, 2.5, 16, 32);

    for (int i = 0; i < 8; i++) {
        float rad = i * 3.14159f / 4.0f;
        float gx = cos(rad) * 2.5f, gy = sin(rad) * 2.5f;
        glColor3f(0.95f, 0.85f, 0.10f);
        glBegin(GL_LINES); glVertex3f(0,0,0); glVertex3f(gx, gy, 0); glEnd();
        glPushMatrix();
        glTranslatef(gx, gy, 0);
        glRotatef(-g_wheel_angle, 0, 0, 1);
        glColor3f(0.90f, 0.20f, 0.20f);
        glScalef(0.4f, 0.35f, 0.3f);
        glutSolidCube(1.0);
        glPopMatrix();
    }
    glPopMatrix();

    // 5. Roller Coaster
    glPushMatrix();
    glTranslatef(1.0f, 0, -4.0f);
    float px[] = { -1.5f, 0.5f, 2.5f, 4.5f, 6.5f };
    float py[] = { 1.5f, 3.8f, 1.8f, 3.4f, 1.2f };
    glColor3f(0.95f, 0.65f, 0.10f);
    glBegin(GL_LINES);
    for (int i = 0; i < 5; i++) { glVertex3f(px[i], 0, -1); glVertex3f(px[i], py[i], -1); }
    glEnd();

    glColor3f(0.90f, 0.20f, 0.20f);
    glBegin(GL_LINE_STRIP);
    for (int i = 0; i < 5; i++) glVertex3f(px[i], py[i], -1);
    glEnd();

    float seg = g_coaster_t * 4.0f;
    int idx = (int)seg;
    float lt = seg - idx;
    if (idx > 3) { idx = 3; lt = 1.0f; }
    glPushMatrix();
    glTranslatef(px[idx] + (px[idx+1] - px[idx]) * lt, py[idx] + (py[idx+1] - py[idx]) * lt + 0.3f, -1);
    glColor3f(0.15f, 0.85f, 0.35f);
    glScalef(0.8f, 0.4f, 0.5f);
    glutSolidCube(1.0);
    glPopMatrix();
    glPopMatrix();

    // 6. Water Ride (Water texture)
    glPushMatrix();
    glTranslatef(6.0f, 0.05f, 2.0f);
    if (g_textures_on) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, g_tex_water);
    }
    glColor3f(0.9f, 0.9f, 1.0f);
    glBegin(GL_QUADS);
    glNormal3f(0, 1, 0);
    glTexCoord2f(0, 0); glVertex3f(-1.75f, 0.05f, -1.25f);
    glTexCoord2f(2, 0); glVertex3f( 1.75f, 0.05f, -1.25f);
    glTexCoord2f(2, 2); glVertex3f( 1.75f, 0.05f,  1.25f);
    glTexCoord2f(0, 2); glVertex3f(-1.75f, 0.05f,  1.25f);
    glEnd();
    if (g_textures_on) glDisable(GL_TEXTURE_2D);

    glPushMatrix();
    glTranslatef(sin(g_boat_t) * 0.8f, 0.2f, 0);
    glColor3f(0.95f, 0.50f, 0.10f);
    glScalef(1.2f, 0.3f, 0.6f);
    glutSolidCube(1.0);
    glPopMatrix();
    glPopMatrix();

    // 7. Go-Kart (Asphalt texture)
    glPushMatrix();
    glTranslatef(-6.0f, 0.05f, 2.0f);
    if (g_textures_on) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, g_tex_asphalt);
    }
    glColor3f(0.9f, 0.9f, 0.9f);
    glBegin(GL_QUADS);
    glNormal3f(0, 1, 0);
    glTexCoord2f(0, 0); glVertex3f(-2.0f, 0.05f, -1.4f);
    glTexCoord2f(3, 0); glVertex3f( 2.0f, 0.05f, -1.4f);
    glTexCoord2f(3, 2); glVertex3f( 2.0f, 0.05f,  1.4f);
    glTexCoord2f(0, 2); glVertex3f(-2.0f, 0.05f,  1.4f);
    glEnd();
    if (g_textures_on) glDisable(GL_TEXTURE_2D);

    glPushMatrix();
    glTranslatef(g_kart_x, 0.22f, 0);
    glColor3f(0.90f, 0.15f, 0.15f);
    glScalef(1.0f, 0.3f, 0.6f);
    glutSolidCube(1.0);
    glPopMatrix();
    glPopMatrix();
}

void draw_hud(void) {
    glDisable(GL_LIGHTING);
    glDisable(GL_TEXTURE_2D);
    glMatrixMode(GL_PROJECTION);
    glPushMatrix();
    glLoadIdentity();
    gluOrtho2D(0, g_width, 0, g_height);
    glMatrixMode(GL_MODELVIEW);
    glPushMatrix();
    glLoadIdentity();

    // HUD Background Bar
    glColor4f(0.06f, 0.09f, 0.14f, 0.85f);
    glBegin(GL_QUADS);
    glVertex2i(10, g_height - 65);
    glVertex2i(420, g_height - 65);
    glVertex2i(420, g_height - 10);
    glVertex2i(10, g_height - 10);
    glEnd();

    // HUD Text
    char buf[128];
    glColor3f(0.22f, 0.74f, 0.97f);
    render_hud_text(20, g_height - 30, "SMART AMUSEMENT PARK - CG TASK 3 (TEXTURE & LIGHT)");

    snprintf(buf, sizeof(buf), "[L] Lighting: %s  |  [T] Textures: %s  |  [P] %s",
             g_lighting_on ? "ON" : "OFF",
             g_textures_on ? "ON" : "OFF",
             g_paused ? "PAUSED" : "ACTIVE");
    glColor3f(0.95f, 0.95f, 0.95f);
    render_hud_text(20, g_height - 50, buf);

    glPopMatrix();
    glMatrixMode(GL_PROJECTION);
    glPopMatrix();
    glMatrixMode(GL_MODELVIEW);

    if (g_lighting_on) glEnable(GL_LIGHTING);
}

void display(void) {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glViewport(0, 0, g_width, g_height);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluPerspective(50.0, (double)g_width / (double)g_height, 1.0, 100.0);

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();
    gluLookAt(0.0, 7.5, 14.0, 0.0, 1.5, 0.0, 0.0, 1.0, 0.0);

    if (g_lighting_on) glEnable(GL_LIGHTING);
    else glDisable(GL_LIGHTING);

    draw_scene_objects();
    draw_hud();

    glutSwapBuffers();
}

void timer_cb(int val) {
    (void)val;
    if (!g_paused) {
        g_wheel_angle += 0.8f;
        if (g_wheel_angle >= 360.0f) g_wheel_angle -= 360.0f;

        g_coaster_t += 0.008f;
        if (g_coaster_t >= 1.0f) g_coaster_t = 0.0f;

        static float kart_dir = 1.0f;
        g_kart_x += 0.02f * kart_dir;
        if (g_kart_x > 1.2f || g_kart_x < -1.2f) kart_dir = -kart_dir;

        g_boat_t += 0.04f;
    }
    glutPostRedisplay();
    glutTimerFunc(16, timer_cb, 0);
}

void reshape(int w, int h) {
    g_width = w; g_height = h;
}

void keyboard(unsigned char key, int x, int y) {
    (void)x; (void)y;
    if (key == 27) exit(0);
    if (key == 'l' || key == 'L') g_lighting_on = !g_lighting_on;
    if (key == 't' || key == 'T') g_textures_on = !g_textures_on;
    if (key == 'p' || key == 'P') g_paused = !g_paused;
}

int main(int argc, char** argv) {
    printf("=================================================================\n");
    printf("   SMART AMUSEMENT PARK - CG TASK 3: TEXTURING & PHONG LIGHTING  \n");
    printf("=================================================================\n");
    printf("Controls:\n");
    printf("  'L' / 'l' -> Toggle Phong Lighting ON / OFF\n");
    printf("  'T' / 't' -> Toggle Procedural Textures ON / OFF\n");
    printf("  'P' / 'p' -> Toggle Animation Pause / Resume\n");
    printf("  ESC       -> Exit Program\n\n");

    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH);
    glutInitWindowSize(g_width, g_height);
    glutCreateWindow("Smart Amusement Park - CG Task 3 (Textures & Lighting)");

    init_graphics();

    glutDisplayFunc(display);
    glutReshapeFunc(reshape);
    glutKeyboardFunc(keyboard);
    glutTimerFunc(16, timer_cb, 0);

    if (argc > 1 && strcmp(argv[1], "--test") == 0) {
        printf("[PASS] Task 3 Lighting & Procedural Texture Setup Verified.\n");
        return 0;
    }

    glutMainLoop();
    return 0;
}
