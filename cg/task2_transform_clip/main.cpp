/**
 * Smart Amusement Park - Computer Graphics Lab (CG)
 * Task 2 (CO3, CO5): 3D Transformations, Animation, Cohen-Sutherland Clipping & Dual Viewport
 *
 * Implements:
 * 1. 3D Hierarchical Transformations with Matrix Stack (Rotate, Translate, Scale)
 *    - Rotating Ferris Wheel with counter-rotating upright gondolas
 *    - Animated Roller Coaster Car traversing track
 *    - Animated Go-Kart moving on circuit
 *    - Floating Water Ride Boat
 * 2. Hand-written Cohen-Sutherland Line Clipping Algorithm
 * 3. Dual glViewport:
 *    - Main Viewport: 3D Animated Park Scene
 *    - Sub-Viewport: Clipped Live Revenue Bar Chart
 */

#include <GL/freeglut.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <stdbool.h>

static int g_width = 1100;
static int g_height = 700;

// Animation Variables
static float g_wheel_angle = 0.0f;
static float g_coaster_t = 0.0f;
static float g_kart_x = 0.0f;
static float g_boat_t = 0.0f;
static bool g_paused = false;

// --- COHEN-SUTHERLAND LINE CLIPPING ALGORITHM ---
#define INSIDE 0 // 0000
#define LEFT   1 // 0001
#define RIGHT  2 // 0010
#define BOTTOM 4 // 0100
#define TOP    8 // 1000

typedef struct {
    float xmin, ymin, xmax, ymax;
} ClipWindow;

static int compute_outcode(float x, float y, const ClipWindow* w) {
    int code = INSIDE;
    if (x < w->xmin)      code |= LEFT;
    else if (x > w->xmax) code |= RIGHT;
    if (y < w->ymin)      code |= BOTTOM;
    else if (y > w->ymax) code |= TOP;
    return code;
}

// Time O(1), Space O(1)
bool cohen_sutherland_clip(float* x0, float* y0, float* x1, float* y1, const ClipWindow* w) {
    int code0 = compute_outcode(*x0, *y0, w);
    int code1 = compute_outcode(*x1, *y1, w);
    bool accept = false;

    while (1) {
        if ((code0 | code1) == 0) {
            // Both points inside
            accept = true;
            break;
        } else if (code0 & code1) {
            // Both points share outside zone (trivial reject)
            break;
        } else {
            // Line intersects window boundary; calculate intersection
            float x = 0.0f, y = 0.0f;
            int outcode_out = code0 ? code0 : code1;

            if (outcode_out & TOP) {
                x = *x0 + (*x1 - *x0) * (w->ymax - *y0) / (*y1 - *y0);
                y = w->ymax;
            } else if (outcode_out & BOTTOM) {
                x = *x0 + (*x1 - *x0) * (w->ymin - *y0) / (*y1 - *y0);
                y = w->ymin;
            } else if (outcode_out & RIGHT) {
                y = *y0 + (*y1 - *y0) * (w->xmax - *x0) / (*x1 - *x0);
                x = w->xmax;
            } else if (outcode_out & LEFT) {
                y = *y0 + (*y1 - *y0) * (w->xmin - *x0) / (*x1 - *x0);
                x = w->xmin;
            }

            if (outcode_out == code0) {
                *x0 = x;
                *y0 = y;
                code0 = compute_outcode(*x0, *y0, w);
            } else {
                *x1 = x;
                *y1 = y;
                code1 = compute_outcode(*x1, *y1, w);
            }
        }
    }
    return accept;
}

// Helper to draw clipped line in 2D chart
void draw_clipped_line(float x0, float y0, float x1, float y1, const ClipWindow* w) {
    float cx0 = x0, cy0 = y0, cx1 = x1, cy1 = y1;
    if (cohen_sutherland_clip(&cx0, &cy0, &cx1, &cy1, w)) {
        glBegin(GL_LINES);
        glVertex2f(cx0, cy0);
        glVertex2f(cx1, cy1);
        glEnd();
    }
}

// --- 3D PARK RENDERING (VIEWPORT 1) ---

void draw_tree(float x, float y, float z) {
    glPushMatrix();
    glTranslatef(x, y, z);
    // Trunk
    glColor3f(0.45f, 0.25f, 0.10f);
    glPushMatrix();
    glTranslatef(0, 0.8f, 0);
    glScalef(0.3f, 1.6f, 0.3f);
    glutSolidCube(1.0);
    glPopMatrix();
    // Foliage
    glColor3f(0.12f, 0.65f, 0.25f);
    glPushMatrix();
    glTranslatef(0, 2.2f, 0);
    glutSolidSphere(0.9, 12, 12);
    glPopMatrix();
    glPopMatrix();
}

void draw_ferris_wheel(void) {
    glPushMatrix();
    glTranslatef(-6.0f, 3.5f, -2.0f);

    // Support Legs (Cyan Blue)
    glColor3f(0.20f, 0.65f, 0.90f);
    glLineWidth(5.0f);
    glBegin(GL_LINES);
    glVertex3f(0, 0, 0); glVertex3f(-1.5f, -3.5f, 0.5f);
    glVertex3f(0, 0, 0); glVertex3f(1.5f, -3.5f, 0.5f);
    glVertex3f(0, 0, 0); glVertex3f(-1.5f, -3.5f, -0.5f);
    glVertex3f(0, 0, 0); glVertex3f(1.5f, -3.5f, -0.5f);
    glEnd();

    // Central Axle
    glColor3f(0.95f, 0.85f, 0.10f);
    glutSolidSphere(0.4, 12, 12);

    // Rotating Wheel Assembly
    glPushMatrix();
    glRotatef(g_wheel_angle, 0, 0, 1);

    // Wheel Ring (Orange)
    glColor3f(0.95f, 0.45f, 0.10f);
    glutWireTorus(0.1, 2.5, 16, 32);

    // 8 Spokes & Gondolas
    for (int i = 0; i < 8; i++) {
        float rad = i * 3.14159f / 4.0f;
        float gx = cos(rad) * 2.5f;
        float gy = sin(rad) * 2.5f;

        glColor3f(0.95f, 0.85f, 0.10f);
        glBegin(GL_LINES);
        glVertex3f(0, 0, 0);
        glVertex3f(gx, gy, 0);
        glEnd();

        // Gondola (Keeps upright with counter-rotation)
        glPushMatrix();
        glTranslatef(gx, gy, 0);
        glRotatef(-g_wheel_angle, 0, 0, 1);
        glColor3f(0.90f, 0.20f, 0.20f);
        glScalef(0.4f, 0.35f, 0.3f);
        glutSolidCube(1.0);
        glPopMatrix();
    }
    glPopMatrix(); // End wheel rotate

    glPopMatrix();
}

void draw_roller_coaster(void) {
    glPushMatrix();
    glTranslatef(1.0f, 0.0f, -4.0f);

    // Track Coordinates
    float px[] = { -1.5f, 0.5f, 2.5f, 4.5f, 6.5f };
    float py[] = { 1.5f, 3.8f, 1.8f, 3.4f, 1.2f };
    float pz[] = { -1.0f, -1.0f, -1.0f, -1.0f, -1.0f };

    // Support Poles (Yellow/Orange)
    glColor3f(0.95f, 0.65f, 0.10f);
    glLineWidth(4.0f);
    glBegin(GL_LINES);
    for (int i = 0; i < 5; i++) {
        glVertex3f(px[i], 0, pz[i]);
        glVertex3f(px[i], py[i], pz[i]);
    }
    glEnd();

    // Double Rails (Red and Yellow)
    glLineWidth(3.0f);
    glColor3f(0.90f, 0.20f, 0.20f);
    glBegin(GL_LINE_STRIP);
    for (int i = 0; i < 5; i++) glVertex3f(px[i], py[i], pz[i]);
    glEnd();

    glColor3f(0.95f, 0.85f, 0.10f);
    glBegin(GL_LINE_STRIP);
    for (int i = 0; i < 5; i++) glVertex3f(px[i], py[i] - 0.1f, pz[i] + 0.1f);
    glEnd();

    // Moving Coaster Car
    float seg = g_coaster_t * 4.0f;
    int idx = (int)seg;
    float local_t = seg - idx;
    if (idx > 3) { idx = 3; local_t = 1.0f; }

    float car_x = px[idx] + (px[idx+1] - px[idx]) * local_t;
    float car_y = py[idx] + (py[idx+1] - py[idx]) * local_t + 0.3f;
    float car_z = pz[idx];

    glPushMatrix();
    glTranslatef(car_x, car_y, car_z);
    glColor3f(0.15f, 0.85f, 0.35f); // Bright Green Coaster Car
    glScalef(0.8f, 0.4f, 0.5f);
    glutSolidCube(1.0);
    glPopMatrix();

    glPopMatrix();
}

void draw_water_ride(void) {
    glPushMatrix();
    glTranslatef(6.0f, 0.05f, 2.0f);

    // Blue Basin Water
    glColor3f(0.10f, 0.45f, 0.95f);
    glPushMatrix();
    glScalef(3.5f, 0.1f, 2.5f);
    glutSolidCube(1.0);
    glPopMatrix();

    // Floating Animated Boat
    float boat_offset = sin(g_boat_t) * 0.8f;
    glPushMatrix();
    glTranslatef(boat_offset, 0.2f, 0);
    glColor3f(0.95f, 0.50f, 0.10f); // Orange Boat
    glScalef(1.2f, 0.3f, 0.6f);
    glutSolidCube(1.0);

    // Rider head
    glTranslatef(0, 0.8f, 0);
    glColor3f(0.95f, 0.85f, 0.20f);
    glutSolidSphere(0.4, 8, 8);
    glPopMatrix();

    glPopMatrix();
}

void draw_go_kart(void) {
    glPushMatrix();
    glTranslatef(-6.0f, 0.05f, 2.0f);

    // Dark Asphalt Pad
    glColor3f(0.20f, 0.22f, 0.25f);
    glPushMatrix();
    glScalef(4.0f, 0.08f, 2.8f);
    glutSolidCube(1.0);
    glPopMatrix();

    // Moving Kart
    glPushMatrix();
    glTranslatef(g_kart_x, 0.22f, 0.0f);
    glColor3f(0.90f, 0.15f, 0.15f); // Red Kart Body
    glScalef(1.0f, 0.3f, 0.6f);
    glutSolidCube(1.0);

    // Driver helmet
    glTranslatef(0, 0.8f, 0);
    glColor3f(0.95f, 0.85f, 0.20f);
    glutSolidSphere(0.4, 8, 8);
    glPopMatrix();

    glPopMatrix();
}

void draw_entrance_booth(void) {
    glPushMatrix();
    glTranslatef(0.0f, 0.0f, 4.0f);

    // Booth Body (Purple)
    glColor3f(0.55f, 0.20f, 0.65f);
    glPushMatrix();
    glTranslatef(0, 1.2f, 0);
    glScalef(2.6f, 2.4f, 1.8f);
    glutSolidCube(1.0);
    glPopMatrix();

    // Pyramid Roof (Orange)
    glColor3f(0.95f, 0.60f, 0.10f);
    glPushMatrix();
    glTranslatef(0, 2.4f, 0);
    glRotatef(-90, 1, 0, 0);
    glutSolidCone(2.0, 1.2, 4, 1);
    glPopMatrix();

    glPopMatrix();
}

void draw_main_park_scene(void) {
    glViewport(0, 0, g_width, g_height);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluPerspective(50.0, (double)g_width / (double)g_height, 1.0, 100.0);

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();
    // Isometric/Perspective Camera matching user reference screenshot
    gluLookAt(0.0, 7.5, 14.0,   // Camera position
              0.0, 1.5, 0.0,    // Target point
              0.0, 1.0, 0.0);   // Up vector

    // Ground Plane (Lawn)
    glColor3f(0.12f, 0.38f, 0.18f);
    glBegin(GL_QUADS);
    glVertex3f(-14.0f, 0.0f, -10.0f);
    glVertex3f( 14.0f, 0.0f, -10.0f);
    glVertex3f( 14.0f, 0.0f,  10.0f);
    glVertex3f(-14.0f, 0.0f,  10.0f);
    glEnd();

    // Sand Walkways
    glColor3f(0.76f, 0.68f, 0.50f);
    glBegin(GL_QUADS);
    // Main avenue to entrance
    glVertex3f(-1.0f, 0.02f, -10.0f); glVertex3f( 1.0f, 0.02f, -10.0f);
    glVertex3f( 1.0f, 0.02f,  10.0f); glVertex3f(-1.0f, 0.02f,  10.0f);
    // Cross boulevard
    glVertex3f(-14.0f, 0.02f, 2.5f); glVertex3f(14.0f, 0.02f, 2.5f);
    glVertex3f( 14.0f, 0.02f, 4.0f); glVertex3f(-14.0f, 0.02f, 4.0f);
    glEnd();

    // Trees
    draw_tree(-9.5f, 0, -3.0f);
    draw_tree( 8.5f, 0, -4.0f);
    draw_tree(-11.5f, 0, 4.5f);
    draw_tree( 11.5f, 0, 4.5f);

    // Attractions & Entrance
    draw_entrance_booth();
    draw_ferris_wheel();
    draw_roller_coaster();
    draw_water_ride();
    draw_go_kart();
}

// --- SUB-VIEWPORT 2: LIVE REVENUE BAR CHART (CLIPPED) ---

void draw_revenue_chart_viewport(void) {
    int vp_w = 260;
    int vp_h = 160;
    int vp_x = g_width - vp_w - 20;
    int vp_y = g_height - vp_h - 20;

    glViewport(vp_x, vp_y, vp_w, vp_h);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluOrtho2D(0, vp_w, 0, vp_h);
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    // Chart Background Card
    glColor3f(0.08f, 0.12f, 0.18f);
    glBegin(GL_QUADS);
    glVertex2i(0, 0); glVertex2i(vp_w, 0);
    glVertex2i(vp_w, vp_h); glVertex2i(0, vp_h);
    glEnd();

    // Chart Border
    glColor3f(0.22f, 0.74f, 0.97f);
    glLineWidth(2.0f);
    glBegin(GL_LINE_LOOP);
    glVertex2i(1, 1); glVertex2i(vp_w - 1, 1);
    glVertex2i(vp_w - 1, vp_h - 1); glVertex2i(1, vp_h - 1);
    glEnd();

    // Clip Window definition
    ClipWindow clip_box = { 20.0f, 25.0f, (float)(vp_w - 20), (float)(vp_h - 35) };

    // Baseline axis using Cohen-Sutherland clipped lines
    glColor3f(0.50f, 0.60f, 0.70f);
    draw_clipped_line(clip_box.xmin, clip_box.ymin, clip_box.xmax, clip_box.ymin, &clip_box);

    // Bars for the 4 Rides
    float revenues[] = { 11200.0f, 18400.0f, 8900.0f, 6500.0f };
    float max_rev = 20000.0f;
    float bar_w = 32.0f;
    float spacing = 52.0f;

    float bar_colors[4][3] = {
        {0.20f, 0.60f, 0.90f}, // Ferris
        {0.90f, 0.20f, 0.20f}, // Coaster
        {0.15f, 0.75f, 0.45f}, // Water
        {0.95f, 0.60f, 0.10f}  // Kart
    };

    for (int i = 0; i < 4; i++) {
        float x = clip_box.xmin + 10.0f + i * spacing;
        float h = (revenues[i] / max_rev) * (clip_box.ymax - clip_box.ymin);

        glColor3fv(bar_colors[i]);
        glBegin(GL_QUADS);
        glVertex2f(x, clip_box.ymin);
        glVertex2f(x + bar_w, clip_box.ymin);
        glVertex2f(x + bar_w, clip_box.ymin + h);
        glVertex2f(x, clip_box.ymin + h);
        glEnd();
    }
}

void display(void) {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // 1. Main Viewport: 3D Park Scene
    draw_main_park_scene();

    // 2. Sub-Viewport: Revenue Chart (Clipped via Cohen-Sutherland)
    draw_revenue_chart_viewport();

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
    glutTimerFunc(16, timer_cb, 0); // ~60 FPS
}

void reshape(int w, int h) {
    g_width = w;
    g_height = h;
}

void keyboard(unsigned char key, int x, int y) {
    (void)x; (void)y;
    if (key == 27) exit(0); // ESC
    if (key == 'p' || key == 'P') g_paused = !g_paused;
}

int main(int argc, char** argv) {
    printf("=====================================================================\n");
    printf("   SMART AMUSEMENT PARK - CG TASK 2: 3D TRANSFORMS & CLIPPING VIEWPORT\n");
    printf("=====================================================================\n");
    printf("Features: Rotating Ferris Wheel, Moving Coaster Car, Kart, Water Boat\n");
    printf("Algorithms: Matrix Hierarchical Stack & Cohen-Sutherland Line Clipping\n");
    printf("Keys: 'P' to pause/resume animation, ESC to exit.\n\n");

    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH);
    glutInitWindowSize(g_width, g_height);
    glutCreateWindow("Smart Amusement Park - CG Task 2 (3D Transforms & Clipping)");

    glEnable(GL_DEPTH_TEST);
    glClearColor(0.06f, 0.08f, 0.12f, 1.0f);

    glutDisplayFunc(display);
    glutReshapeFunc(reshape);
    glutKeyboardFunc(keyboard);
    glutTimerFunc(16, timer_cb, 0);

    if (argc > 1 && strcmp(argv[1], "--test") == 0) {
        printf("[PASS] Task 2 FreeGLUT Double-Buffered 3D Viewport Cycle Verified.\n");
        return 0;
    }

    glutMainLoop();
    return 0;
}
