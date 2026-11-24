# Supabase Cloud Database Setup Guide

## Overview
The Smart Amusement Park system integrates with Supabase (PostgreSQL) to store a permanent, tamper-resistant history of ticket bookings and real-time revenue summaries.

## Setup Steps
1. Create a free Supabase project at [https://supabase.com](https://supabase.com).
2. Navigate to the **SQL Editor** in the Supabase Dashboard.
3. Open `supabase/schema.sql`, copy all SQL content, and run it.
4. Verify that:
   - `rides` table is created and populated with 4 rides (Ferris Wheel, Roller Coaster, Water Ride, Go-Kart).
   - `bookings` table, views `ride_availability`, `revenue_summary`, and functions `book_ticket` and `cancel_booking` are compiled.
5. In your Supabase Project Settings, navigate to **API**:
   - Copy **Project URL**.
   - Copy **anon public API key**.
6. In `java/config/`, copy `supabase.properties.example` to `supabase.properties`:
   ```properties
   supabase.url=https://YOUR_PROJECT_ID.supabase.co
   supabase.key=eyJhbGciOi...
   ```

## Security & Ethics Note
- **Row Level Security (RLS)** is enabled on all tables.
- **Permanent History:** No `DELETE` policy is granted to the anonymous role. Cancellations are modeled strictly as status changes (`CANCELLED`), preserving the audit trail.
- Prototype disclaimer: The anon key is used here for lab demonstration. Production deployments should enforce authenticated staff sessions via JWT.
