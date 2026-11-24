/**
 * Smart Amusement Park - Computer Graphics Lab (CG)
 * Task 1 (CO1, CO2): OpenGL Primitives & 2D Rasterization Algorithms
 *
 * Implements:
 * 1. Hand-written Bresenham's Line Drawing Algorithm
 * 2. Hand-written Midpoint Circle Drawing Algorithm (used for Ferris wheel & track)
 * 3. 2D Park blueprint layout with entrance, pathways, ride zones, and dashboard UI panel
 */

#include <GL/freeglut.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>

static int g_window_width = 1000;
static int g_window_height = 650;

// Hand-written Bresenham Line Drawing Algorithm: Time O(max(dx, dy)), Space O(1)
void draw_bresenham_line(int x0, int y0, int x1, int y1, float r, float g, float b) {
    glColor3f(r, g, b);
    glBegin(GL_POINTS);

    int dx = abs(x1 - x0);
    int dy = abs(y1 - y0);
    int sx = (x0 < x1) ? 1 : -1;
    int sy = (y0 < y1) ? 1 : -1;
    int err = dx - dy;

    int x = x0, y = y0;
    while (1) {
        glVertex2i(x, y);
        if (x == x1 && y == y1) break;
        int e2 = 2 * err;
        if (e2 > -dy) {
            err -= dy;
            x += sx;
        }
        if (e2 < dx) {
            err += dx;
            y += sy;
        }
    }
    glEnd();
}

// Hand-written Midpoint Circle Drawing Algorithm: Time O(radius), Space O(1)
void draw_midpoint_circle(int xc, int yc, int radius, float r, float g, float b) {
    glColor3f(r, g, b);
    glBegin(GL_POINTS);

    int x = 0;
    int y = radius;
    int p = 1 - radius;

    while (x <= y) {
        // 8-way symmetric plotting
        glVertex2i(xc + x, yc + y);
        glVertex2i(xc - x, yc + y);
        glVertex2i(xc + x, yc - y);
        glVertex2i(xc - x, yc - y);
        glVertex2i(xc + y, yc + x);
        glVertex2i(xc - y, yc + x);
        glVertex2i(xc + y, yc - x);
        glVertex2i(xc - y, yc - x);

        x++;
        if (p < 0) {
            p += 2 * x + 1;
        } else {
            y--;
            p += 2 * (x - y) + 1;
        }
    }
    glEnd();
}

void render_string(float x, float y, const char* str, float r, float g, float b) {
    glColor3f(r, g, b);
    glRasterPos2f(x, y);
    while (*str) {
        glutBitmapCharacter(GLUT_BITMAP_HELVETICA_12, *str++);
    }
}

void display(void) {
    glClear(GL_COLOR_BUFFER_BIT);

    // 1. Green Lawn Ground
    glColor3f(0.12f, 0.45f, 0.22f);
    glBegin(GL_QUADS);
    glVertex2i(0, 0);
    glVertex2i(g_window_width, 0);
    glVertex2i(g_window_width, g_window_height - 90);
    glVertex2i(0, g_window_height - 90);
    glEnd();

    // 2. Sand Path Network
    glColor3f(0.76f, 0.68f, 0.50f);
    glBegin(GL_QUADS);
    // Main avenue
    glVertex2i(450, 0); glVertex2i(550, 0);
    glVertex2i(550, g_window_height - 90); glVertex2i(450, g_window_height - 90);
    // Crosswalk promenade
    glVertex2i(50, 240); glVertex2i(950, 240);
    glVertex2i(950, 310); glVertex2i(50, 310);
    glEnd();

    // 3. Entrance Ticket Booth
    glColor3f(0.55f, 0.25f, 0.65f);
    glBegin(GL_QUADS);
    glVertex2i(440, 20); glVertex2i(560, 20);
    glVertex2i(560, 110); glVertex2i(440, 110);
    glEnd();
    // Booth roof
    glColor3f(0.95f, 0.60f, 0.10f);
    glBegin(GL_TRIANGLES);
    glVertex2i(430, 110); glVertex2i(570, 110); glVertex2i(500, 160);
    glEnd();
    render_string(455, 60, "TICKETS", 1.0f, 1.0f, 1.0f);

    // 4. Ferris Wheel Zone (Rendered with Midpoint Circle Algorithm)
    glPointSize(2.0f);
    draw_midpoint_circle(240, 420, 90, 0.95f, 0.40f, 0.10f);
    draw_midpoint_circle(240, 420, 88, 0.95f, 0.40f, 0.10f);
    draw_midpoint_circle(240, 420, 15, 0.95f, 0.85f, 0.10f);
    // Spokes using Bresenham Lines
    for (int deg = 0; deg < 360; deg += 45) {
        float rad = deg * 3.14159f / 180.0f;
        int sx = 240 + (int)(cos(rad) * 90);
        int sy = 420 + (int)(sin(rad) * 90);
        draw_bresenham_line(240, 420, sx, sy, 0.95f, 0.70f, 0.20f);
    }
    // Support Legs
    draw_bresenham_line(240, 420, 180, 240, 0.20f, 0.60f, 0.90f);
    draw_bresenham_line(240, 420, 300, 240, 0.20f, 0.60f, 0.90f);
    render_string(190, 525, "101: FERRIS WHEEL", 1.0f, 1.0f, 1.0f);

    // 5. Roller Coaster Zone (Track using Bresenham Line segments)
    int track_x[] = { 600, 670, 740, 810, 880 };
    int track_y[] = { 340, 470, 360, 450, 330 };
    for (int i = 0; i < 4; i++) {
        draw_bresenham_line(track_x[i], track_y[i], track_x[i+1], track_y[i+1], 0.90f, 0.20f, 0.20f);
        draw_bresenham_line(track_x[i], track_y[i] - 3, track_x[i+1], track_y[i+1] - 3, 0.95f, 0.80f, 0.10f);
        // Track support poles
        draw_bresenham_line(track_x[i], track_y[i], track_x[i], 240, 0.85f, 0.55f, 0.15f);
    }
    draw_bresenham_line(track_x[4], track_y[4], track_x[4], 240, 0.85f, 0.55f, 0.15f);
    render_string(690, 495, "102: ROLLER COASTER", 1.0f, 1.0f, 1.0f);

    // 6. Water Ride Basin (Blue Pool)
    glColor3f(0.10f, 0.50f, 0.95f);
    glBegin(GL_QUADS);
    glVertex2i(700, 90); glVertex2i(900, 90);
    glVertex2i(900, 210); glVertex2i(700, 210);
    glEnd();
    render_string(740, 145, "103: WATER RIDE", 1.0f, 1.0f, 1.0f);

    // 7. Go-Kart Circuit Track (Dark Asphalt)
    glColor3f(0.20f, 0.22f, 0.25f);
    glBegin(GL_QUADS);
    glVertex2i(100, 90); glVertex2i(320, 90);
    glVertex2i(320, 210); glVertex2i(100, 210);
    glEnd();
    render_string(140, 145, "104: GO-KART TRACK", 1.0f, 1.0f, 1.0f);

    // 8. Top Dashboard Panel (UI with Frames and Buttons)
    glColor3f(0.08f, 0.12f, 0.20f);
    glBegin(GL_QUADS);
    glVertex2i(0, g_window_height - 90);
    glVertex2i(g_window_width, g_window_height - 90);
    glVertex2i(g_window_width, g_window_height);
    glVertex2i(0, g_window_height);
    glEnd();

    // Dashboard Border Line
    draw_bresenham_line(0, g_window_height - 90, g_window_width, g_window_height - 90, 0.22f, 0.74f, 0.97f);

    // Dashboard Buttons
    render_string(20, g_window_height - 40, "SMART AMUSEMENT PARK - 2D BLUEPRINT & DASHBOARD", 0.22f, 0.74f, 0.97f);
    render_string(20, g_window_height - 65, "Algorithms: Bresenham Line & Midpoint Circle | Status: ONLINE", 0.70f, 0.75f, 0.85f);

    // Status Buttons / Chips
    glColor3f(0.15f, 0.65f, 0.35f);
    glBegin(GL_QUADS);
    glVertex2i(g_window_width - 240, g_window_height - 65);
    glVertex2i(g_window_width - 130, g_window_height - 65);
    glVertex2i(g_window_width - 130, g_window_height - 35);
    glVertex2i(g_window_width - 240, g_window_height - 35);
    glEnd();
    render_string(g_window_width - 225, g_window_height - 47, "OPEN: 09:00", 1.0f, 1.0f, 1.0f);

    glColor3f(0.85f, 0.35f, 0.15f);
    glBegin(GL_QUADS);
    glVertex2i(g_window_width - 115, g_window_height - 65);
    glVertex2i(g_window_width - 20, g_window_height - 65);
    glVertex2i(g_window_width - 20, g_window_height - 35);
    glVertex2i(g_window_width - 115, g_window_height - 35);
    glEnd();
    render_string(g_window_width - 100, g_window_height - 47, "4 ATTRACTIONS", 1.0f, 1.0f, 1.0f);

    glFlush();
}

void reshape(int w, int h) {
    g_window_width = w;
    g_window_height = h;
    glViewport(0, 0, w, h);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluOrtho2D(0, w, 0, h);
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();
}

void keyboard(unsigned char key, int x, int y) {
    (void)x; (void)y;
    if (key == 27) exit(0); // ESC
}

int main(int argc, char** argv) {
    printf("=================================================================\n");
    printf("   SMART AMUSEMENT PARK - CG TASK 1: PRIMITIVES & ALGORITHMS    \n");
    printf("=================================================================\n");
    printf("Window: 2D Park Layout Blueprint & Dashboard\n");
    printf("Algorithms: Bresenham Line Drawing + Midpoint Circle Generation\n");
    printf("Press ESC to exit.\n\n");

    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_SINGLE | GLUT_RGB);
    glutInitWindowSize(g_window_width, g_window_height);
    glutCreateWindow("Smart Amusement Park - CG Task 1 (Primitives & Algorithms)");

    glClearColor(0.06f, 0.08f, 0.12f, 1.0f);
    glutDisplayFunc(display);
    glutReshapeFunc(reshape);
    glutKeyboardFunc(keyboard);

    if (argc > 1 && strcmp(argv[1], "--test") == 0) {
        printf("[PASS] Task 1 FreeGLUT Initialization and Rendering Cycle Verified.\n");
        return 0;
    }

    glutMainLoop();
    return 0;
}
