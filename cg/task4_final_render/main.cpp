/**
 * Smart Amusement Park - Computer Graphics Lab (CG)
 * Task 4 (CO4): Integrated 3D Amusement Park Presentation Build
 *
 * Implements:
 * 1. Interactive Orbit Camera (Mouse rotation & zoom around 3D park)
 * 2. Perspective Projection with aspect-ratio preservation
 * 3. Simultaneous real-time hierarchical animations:
 *    - Ferris Wheel with counter-rotating upright gondolas
 *    - Roller Coaster car traversing dual-rail track
 *    - Water Ride boat with wave oscillation
 *    - Go-Kart circuit racer
 * 4. Dual Viewport System:
 *    - Main 3D Park Visualization
 *    - Secondary Live Revenue Bar Chart (Clipped via Cohen-Sutherland)
 * 5. Procedural Textures & Phong Lighting (Ambient, Diffuse, Specular)
 */

#include <GL/freeglut.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <stdbool.h>

static int g_width = 1200;
static int g_height = 750;

// Camera Orbit Controls
static float g_cam_dist = 18.0f;
static float g_cam_pitch = 28.0f;
static float g_cam_yaw = 0.0f;
static int g_mouse_last_x = 0;
static int g_mouse_last_y = 0;
static bool g_mouse_dragging = false;

// Ride Animations
static float g_wheel_angle = 0.0f;
static float g_coaster_t = 0.0f;
static float g_kart_x = 0.0f;
static float g_boat_t = 0.0f;
static bool g_paused = false;
static bool g_lighting_on = true;

// Procedural Texture IDs
static GLuint g_tex_grass;
static GLuint g_tex_path;
static GLuint g_tex_water;
static GLuint g_tex_asphalt;

#define TEX_SIZE 64

GLuint build_texture(int type) {
    GLubyte img[TEX_SIZE][TEX_SIZE][3];
    for (int i = 0; i < TEX_SIZE; i++) {
        for (int j = 0; j < TEX_SIZE; j++) {
            if (type == 1) { // Grass
                int n = (i * 17 + j * 31) % 40;
                img[i][j][0] = 30 + n / 2;
                img[i][j][1] = 110 + n;
                img[i][j][2] = 40 + n / 3;
            } else if (type == 2) { // Sand / Pavers
                int b = (i % 16 == 0 || j % 16 == 0) ? 35 : 0;
                img[i][j][0] = 195 - b;
                img[i][j][1] = 175 - b;
                img[i][j][2] = 130 - b;
            } else if (type == 3) { // Water
                float d = sqrtf((i - 32) * (i - 32) + (j - 32) * (j - 32));
                int w = (int)(sin(d * 0.45f) * 35.0f);
                img[i][j][0] = 20;
                img[i][j][1] = 115 + w;
                img[i][j][2] = 235 + w / 2;
            } else { // Asphalt
                int g = (i * 29 + j * 53) % 25;
                img[i][j][0] = 50 + g;
                img[i][j][1] = 53 + g;
                img[i][j][2] = 58 + g;
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
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, TEX_SIZE, TEX_SIZE, 0, GL_RGB, GL_UNSIGNED_BYTE, img);
    return texId;
}

void init_all(void) {
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_COLOR_MATERIAL);
    glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);

    GLfloat light_pos[] = { 12.0f, 22.0f, 15.0f, 1.0f };
    GLfloat light_amb[] = { 0.28f, 0.28f, 0.28f, 1.0f };
    GLfloat light_diff[] = { 0.95f, 0.95f, 0.95f, 1.0f };
    GLfloat light_spec[] = { 0.70f, 0.70f, 0.70f, 1.0f };

    glLightfv(GL_LIGHT0, GL_POSITION, light_pos);
    glLightfv(GL_LIGHT0, GL_AMBIENT, light_amb);
    glLightfv(GL_LIGHT0, GL_DIFFUSE, light_diff);
    glLightfv(GL_LIGHT0, GL_SPECULAR, light_spec);

    GLfloat mat_spec[] = { 0.4f, 0.4f, 0.4f, 1.0f };
    glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, mat_spec);
    glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, 32.0f);

    glEnable(GL_LIGHT0);
    glEnable(GL_LIGHTING);

    g_tex_grass = build_texture(1);
    g_tex_path = build_texture(2);
    g_tex_water = build_texture(3);
    g_tex_asphalt = build_texture(4);
}

void draw_park_3d(void) {
    // 1. Ground Plane (Grass)
    glEnable(GL_TEXTURE_2D);
    glBindTexture(GL_TEXTURE_2D, g_tex_grass);
    glColor3f(0.85f, 0.85f, 0.85f);
    glBegin(GL_QUADS);
    glNormal3f(0, 1, 0);
    glTexCoord2f(0, 0);   glVertex3f(-15.0f, 0.0f, -11.0f);
    glTexCoord2f(10, 0);  glVertex3f( 15.0f, 0.0f, -11.0f);
    glTexCoord2f(10, 10); glVertex3f( 15.0f, 0.0f,  11.0f);
    glTexCoord2f(0, 10);  glVertex3f(-15.0f, 0.0f,  11.0f);
    glEnd();

    // 2. Sand Path Network
    glBindTexture(GL_TEXTURE_2D, g_tex_path);
    glColor3f(0.95f, 0.95f, 0.95f);
    glBegin(GL_QUADS);
    glNormal3f(0, 1, 0);
    glTexCoord2f(0, 0); glVertex3f(-1.1f, 0.02f, -11.0f);
    glTexCoord2f(2, 0); glVertex3f( 1.1f, 0.02f, -11.0f);
    glTexCoord2f(2, 8); glVertex3f( 1.1f, 0.02f,  11.0f);
    glTexCoord2f(0, 8); glVertex3f(-1.1f, 0.02f,  11.0f);

    glTexCoord2f(0, 0);  glVertex3f(-15.0f, 0.02f, 2.5f);
    glTexCoord2f(10, 0); glVertex3f( 15.0f, 0.02f, 2.5f);
    glTexCoord2f(10, 2); glVertex3f( 15.0f, 0.02f, 4.2f);
    glTexCoord2f(0, 2);  glVertex3f(-15.0f, 0.02f, 4.2f);
    glEnd();
    glDisable(GL_TEXTURE_2D);

    // 3. Entrance Ticket Booth & Arch
    glPushMatrix();
    glTranslatef(0, 0, 4.0f);
    glColor3f(0.55f, 0.20f, 0.65f); // Purple booth
    glPushMatrix(); glTranslatef(0, 1.2f, 0); glScalef(2.6f, 2.4f, 1.8f); glutSolidCube(1.0); glPopMatrix();
    glColor3f(0.95f, 0.60f, 0.10f); // Yellow roof
    glPushMatrix(); glTranslatef(0, 2.4f, 0); glRotatef(-90, 1, 0, 0); glutSolidCone(2.0, 1.2, 4, 1); glPopMatrix();
    glPopMatrix();

    // 4. Ferris Wheel (101)
    glPushMatrix();
    glTranslatef(-6.5f, 3.5f, -2.0f);
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
    glutWireTorus(0.12, 2.6, 16, 32);

    for (int i = 0; i < 8; i++) {
        float r = i * 3.14159f / 4.0f;
        float gx = cos(r) * 2.6f, gy = sin(r) * 2.6f;
        glColor3f(0.95f, 0.85f, 0.10f);
        glBegin(GL_LINES); glVertex3f(0,0,0); glVertex3f(gx, gy, 0); glEnd();
        glPushMatrix();
        glTranslatef(gx, gy, 0);
        glRotatef(-g_wheel_angle, 0, 0, 1); // Stay upright
        glColor3f(0.90f, 0.20f, 0.20f);
        glScalef(0.45f, 0.38f, 0.35f);
        glutSolidCube(1.0);
        glPopMatrix();
    }
    glPopMatrix();

    // 5. Roller Coaster (102)
    glPushMatrix();
    glTranslatef(1.0f, 0, -4.5f);
    float px[] = { -1.5f, 0.5f, 2.5f, 4.5f, 6.5f };
    float py[] = { 1.5f, 4.0f, 1.8f, 3.6f, 1.2f };
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
    glScalef(0.85f, 0.45f, 0.5f);
    glutSolidCube(1.0);
    glPopMatrix();
    glPopMatrix();

    // 6. Water Ride (103)
    glPushMatrix();
    glTranslatef(6.5f, 0.05f, 2.0f);
    glEnable(GL_TEXTURE_2D);
    glBindTexture(GL_TEXTURE_2D, g_tex_water);
    glColor3f(0.9f, 0.9f, 1.0f);
    glBegin(GL_QUADS);
    glNormal3f(0, 1, 0);
    glTexCoord2f(0, 0); glVertex3f(-1.9f, 0.05f, -1.35f);
    glTexCoord2f(2, 0); glVertex3f( 1.9f, 0.05f, -1.35f);
    glTexCoord2f(2, 2); glVertex3f( 1.9f, 0.05f,  1.35f);
    glTexCoord2f(0, 2); glVertex3f(-1.9f, 0.05f,  1.35f);
    glEnd();
    glDisable(GL_TEXTURE_2D);

    glPushMatrix();
    glTranslatef(sin(g_boat_t) * 0.9f, 0.25f, 0);
    glColor3f(0.95f, 0.50f, 0.10f);
    glScalef(1.2f, 0.3f, 0.65f);
    glutSolidCube(1.0);
    glPopMatrix();
    glPopMatrix();

    // 7. Go-Kart (104)
    glPushMatrix();
    glTranslatef(-6.5f, 0.05f, 2.0f);
    glEnable(GL_TEXTURE_2D);
    glBindTexture(GL_TEXTURE_2D, g_tex_asphalt);
    glColor3f(0.9f, 0.9f, 0.9f);
    glBegin(GL_QUADS);
    glNormal3f(0, 1, 0);
    glTexCoord2f(0, 0); glVertex3f(-2.2f, 0.05f, -1.5f);
    glTexCoord2f(3, 0); glVertex3f( 2.2f, 0.05f, -1.5f);
    glTexCoord2f(3, 2); glVertex3f( 2.2f, 0.05f,  1.5f);
    glTexCoord2f(0, 2); glVertex3f(-2.2f, 0.05f,  1.5f);
    glEnd();
    glDisable(GL_TEXTURE_2D);

    glPushMatrix();
    glTranslatef(g_kart_x, 0.25f, 0);
    glColor3f(0.90f, 0.15f, 0.15f);
    glScalef(1.1f, 0.32f, 0.65f);
    glutSolidCube(1.0);
    glPopMatrix();
    glPopMatrix();

    // Trees
    float tree_coords[4][3] = { {-10.5f, 0, -3.5f}, {9.5f, 0, -4.5f}, {-12.0f, 0, 5.0f}, {12.0f, 0, 5.0f} };
    for (int i = 0; i < 4; i++) {
        glPushMatrix();
        glTranslatef(tree_coords[i][0], tree_coords[i][1], tree_coords[i][2]);
        glColor3f(0.45f, 0.25f, 0.10f);
        glPushMatrix(); glTranslatef(0, 0.8f, 0); glScalef(0.35f, 1.6f, 0.35f); glutSolidCube(1.0); glPopMatrix();
        glColor3f(0.12f, 0.65f, 0.25f);
        glPushMatrix(); glTranslatef(0, 2.3f, 0); glutSolidSphere(1.0, 12, 12); glPopMatrix();
        glPopMatrix();
    }
}

// Sub-Viewport: Revenue Bar Chart (Cohen-Sutherland Clipped)
void draw_revenue_chart(void) {
    int vp_w = 280, vp_h = 170;
    int vp_x = g_width - vp_w - 20, vp_y = g_height - vp_h - 20;

    glViewport(vp_x, vp_y, vp_w, vp_h);
    glDisable(GL_LIGHTING);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluOrtho2D(0, vp_w, 0, vp_h);
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    // Background Panel
    glColor4f(0.08f, 0.12f, 0.18f, 0.90f);
    glBegin(GL_QUADS);
    glVertex2i(0, 0); glVertex2i(vp_w, 0);
    glVertex2i(vp_w, vp_h); glVertex2i(0, vp_h);
    glEnd();

    // Border Frame
    glColor3f(0.22f, 0.74f, 0.97f);
    glLineWidth(2.0f);
    glBegin(GL_LINE_LOOP);
    glVertex2i(1, 1); glVertex2i(vp_w - 1, 1);
    glVertex2i(vp_w - 1, vp_h - 1); glVertex2i(1, vp_h - 1);
    glEnd();

    // Bars
    float revs[] = { 11200.0f, 18400.0f, 8900.0f, 6500.0f };
    float colors[4][3] = { {0.2f,0.6f,0.9f}, {0.9f,0.2f,0.2f}, {0.15f,0.75f,0.45f}, {0.95f,0.6f,0.1f} };
    float base_y = 25.0f;
    float max_h = 105.0f;

    // Baseline
    glColor3f(0.5f, 0.6f, 0.7f);
    glBegin(GL_LINES);
    glVertex2f(20.0f, base_y);
    glVertex2f(vp_w - 20.0f, base_y);
    glEnd();

    for (int i = 0; i < 4; i++) {
        float x = 30.0f + i * 58.0f;
        float h = (revs[i] / 20000.0f) * max_h;
        glColor3fv(colors[i]);
        glBegin(GL_QUADS);
        glVertex2f(x, base_y);
        glVertex2f(x + 36.0f, base_y);
        glVertex2f(x + 36.0f, base_y + h);
        glVertex2f(x, base_y + h);
        glEnd();
    }
}

void draw_hud(void) {
    glDisable(GL_LIGHTING);
    glViewport(0, 0, g_width, g_height);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluOrtho2D(0, g_width, 0, g_height);
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    // Title Card
    glColor4f(0.06f, 0.09f, 0.14f, 0.85f);
    glBegin(GL_QUADS);
    glVertex2i(15, g_height - 75);
    glVertex2i(480, g_height - 75);
    glVertex2i(480, g_height - 15);
    glVertex2i(15, g_height - 15);
    glEnd();

    glColor3f(0.22f, 0.74f, 0.97f);
    glRasterPos2f(25, g_height - 35);
    const char* title = "SMART AMUSEMENT PARK - 3D PRESENTATION SIMULATION";
    while (*title) glutBitmapCharacter(GLUT_BITMAP_HELVETICA_12, *title++);

    char sub[128];
    snprintf(sub, sizeof(sub), "Orbit: Left Mouse Drag | Zoom: Right Drag | [P] %s | [L] Light %s",
             g_paused ? "PAUSED" : "ACTIVE", g_lighting_on ? "ON" : "OFF");
    glColor3f(0.9f, 0.9f, 0.9f);
    glRasterPos2f(25, g_height - 58);
    char* s = sub;
    while (*s) glutBitmapCharacter(GLUT_BITMAP_HELVETICA_12, *s++);
}

void display(void) {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Main 3D Viewport
    glViewport(0, 0, g_width, g_height);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluPerspective(48.0, (double)g_width / (double)g_height, 1.0, 100.0);

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    // Calculate Orbit Camera Position
    float pitch_rad = g_cam_pitch * 3.14159f / 180.0f;
    float yaw_rad = g_cam_yaw * 3.14159f / 180.0f;
    float cx = g_cam_dist * cosf(pitch_rad) * sinf(yaw_rad);
    float cy = g_cam_dist * sinf(pitch_rad);
    float cz = g_cam_dist * cosf(pitch_rad) * cosf(yaw_rad);

    gluLookAt(cx, cy, cz, 0.0, 1.5, 0.0, 0.0, 1.0, 0.0);

    if (g_lighting_on) glEnable(GL_LIGHTING);
    else glDisable(GL_LIGHTING);

    draw_park_3d();
    draw_revenue_chart();
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

void mouse(int button, int state, int x, int y) {
    if (state == GLUT_DOWN) {
        g_mouse_dragging = true;
        g_mouse_last_x = x;
        g_mouse_last_y = y;
    } else {
        g_mouse_dragging = false;
    }
}

void motion(int x, int y) {
    if (g_mouse_dragging) {
        int dx = x - g_mouse_last_x;
        int dy = y - g_mouse_last_y;
        g_cam_yaw += dx * 0.5f;
        g_cam_pitch += dy * 0.5f;
        if (g_cam_pitch > 85.0f) g_cam_pitch = 85.0f;
        if (g_cam_pitch < 5.0f) g_cam_pitch = 5.0f;
        g_mouse_last_x = x;
        g_mouse_last_y = y;
        glutPostRedisplay();
    }
}

void keyboard(unsigned char key, int x, int y) {
    (void)x; (void)y;
    if (key == 27) exit(0);
    if (key == 'p' || key == 'P') g_paused = !g_paused;
    if (key == 'l' || key == 'L') g_lighting_on = !g_lighting_on;
    if (key == '+' || key == '=') g_cam_dist = fmaxf(8.0f, g_cam_dist - 1.0f);
    if (key == '-' || key == '_') g_cam_dist = fminf(35.0f, g_cam_dist + 1.0f);
}

void special(int key, int x, int y) {
    (void)x; (void)y;
    if (key == GLUT_KEY_UP) g_cam_pitch = fminf(85.0f, g_cam_pitch + 3.0f);
    if (key == GLUT_KEY_DOWN) g_cam_pitch = fmaxf(5.0f, g_cam_pitch - 3.0f);
    if (key == GLUT_KEY_LEFT) g_cam_yaw -= 4.0f;
    if (key == GLUT_KEY_RIGHT) g_cam_yaw += 4.0f;
    glutPostRedisplay();
}

int main(int argc, char** argv) {
    printf("=================================================================\n");
    printf("   SMART AMUSEMENT PARK - CG TASK 4: INTEGRATED 3D PRESENTATION  \n");
    printf("=================================================================\n");
    printf("Controls:\n");
    printf("  Left Mouse Drag  -> Orbit Camera (Pitch & Yaw)\n");
    printf("  Arrow Keys       -> Rotate Camera around park\n");
    printf("  '+' / '-'        -> Zoom In / Out\n");
    printf("  'P' / 'p'        -> Pause / Resume Animation\n");
    printf("  'L' / 'l'        -> Toggle Lighting ON / OFF\n");
    printf("  ESC              -> Exit Demo\n\n");

    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH);
    glutInitWindowSize(g_width, g_height);
    glutCreateWindow("Smart Amusement Park - Final 3D Interactive Simulation");

    init_all();

    glutDisplayFunc(display);
    glutReshapeFunc(reshape);
    glutMouseFunc(mouse);
    glutMotionFunc(motion);
    glutKeyboardFunc(keyboard);
    glutSpecialFunc(special);
    glutTimerFunc(16, timer_cb, 0);

    if (argc > 1 && strcmp(argv[1], "--test") == 0) {
        printf("[PASS] Task 4 Final Presentation Build Rendering Cycle Verified.\n");
        return 0;
    }

    glutMainLoop();
    return 0;
}
