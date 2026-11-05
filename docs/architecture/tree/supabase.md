# `supabase/`

> **Kind:** Project area  
> **Measured:** 1 tracked files — 1× `.sql`

## Shape

| Path | Files |
|---|---:|
| `supabase/migrations/` | 1 |

<!-- tree-scaffold:end -->
## Significance

SQL migrations for the Supabase project behind the `supabase` access point in `app-profile/app.yaml`. The app
talks to Supabase with the **publishable** key only, and that key ships inside every build — so whatever the
`anon` role may do, anyone may do. These migrations are where that is decided: grants and RLS policies, not app
code, are the security boundary.

`20261031120000_app_server_config_lockdown.sql` makes `app_server_config` (the server-instance list the app reads)
read-only for app clients: it revokes the write grants Supabase gives `anon`/`authenticated` by default, forces
RLS, and keeps one policy — read enabled rows. Without it the table is protected by RLS alone, one
`disable row level security` away from being writable with the public key. It is a no-op where the table does not
exist, so it is safe in every fork.

A fork with its **own** Supabase project adds its migrations here and applies them with `/supabase-push` (or
`supabase db push`). Writes that clients genuinely need go through an Edge Function or RPC that checks the caller;
the secret key (`sb_secret_…`, bypasses RLS) stays server-side, never in `app-profile` or a build.
