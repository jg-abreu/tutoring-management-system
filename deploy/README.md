# Live demo: https://tutoring.joaoabreu.dev

How the public demo is served, reset and rebuilt.

```
visitor ──HTTPS──▶ Cloudflare (proxy, edge certificate, DNS A tutoring → static IP)
                       │  HTTPS, SSL mode Full (strict)
                       ▼
            Lightsail portfolio-v2-2gb (the portfolio's VM) · nginx site "tutoring"
              ├─ /            static React client in /var/www/tutoring (SPA fallback to index.html)
              └─ /api/...  ──▶ 127.0.0.1:8090 · Docker container tutoring-production-api-1
                                  │  network portfolio-production_default
                                  ▼
                               portfolio's PostgreSQL 18 · database "tutoring", role "tutoring"
```

| Piece | Where | Notes |
|---|---|---|
| Server | AWS Lightsail `portfolio-v2-2gb`, shared with joaoabreu.dev | 2 GB RAM plus a 1 GB swap file (`/swapfile`, `vm.swappiness=10`) |
| API | `/opt/tutoring-api`: `compose.yaml` (this folder's `compose.production.yaml`), `tutoring-api.jar`, `.env` (mode 600) | `SPRING_PROFILES_ACTIVE=prod`, heap 192 MB, container limit 384 MB, `127.0.0.1:8090` only |
| Database | `tutoring` inside the portfolio's PostgreSQL container | Owned by the `tutoring` role; `CONNECT` revoked from `PUBLIC` on both databases, so neither role reaches the other's data |
| Client | `/var/www/tutoring` | Built with `VITE_API_URL=/api`, so the browser only talks to its own origin |
| nginx | [`nginx/tutoring.conf`](nginx/tutoring.conf) → `/etc/nginx/sites-available/tutoring`; [`nginx/tutoring-proxy.conf`](nginx/tutoring-proxy.conf) → `/etc/nginx/snippets/` | Per-visitor limits on `CF-Connecting-IP`: login 10/min, API 10/s with a burst, `429` beyond; same security headers as the portfolio |
| TLS | the portfolio's Cloudflare Origin certificate | It covers `*.joaoabreu.dev` |
| Reset | [`reset/`](reset): `/opt/tutoring-api/reset-demo.sh`, `tutoring-demo-reset.service` and `.timer` | Every day at 06:00 UTC: empties the tables and reloads `02-seed.sql` |

The `.env` holds `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` and `JWT_SECRET` (see [`.env.production.example`](.env.production.example)). The password and the key were generated on the VM with `openssl rand` and never left it.

## Deploy a new version

On your machine:

```bash
./mvnw -B package                                   # tests run against the Docker database
sha256sum target/tutoring-management-system-0.0.1-SNAPSHOT.jar
scp target/tutoring-management-system-0.0.1-SNAPSHOT.jar ubuntu@<ip>:/tmp/tutoring-api.jar
```

On the server, after comparing the SHA-256:

```bash
sudo install -m 644 /tmp/tutoring-api.jar /opt/tutoring-api/tutoring-api.jar
cd /opt/tutoring-api && sudo docker compose up -d --force-recreate
sudo docker logs -f tutoring-production-api-1      # wait for "Started", then check a login
```

The client, from `frontend/` (in Git Bash, `MSYS_NO_PATHCONV=1` keeps `/api` from being rewritten into a Windows path):

```bash
cd frontend
MSYS_NO_PATHCONV=1 VITE_API_URL=/api npm run build
tar -czf client.tgz -C dist . && scp client.tgz ubuntu@<ip>:/tmp/
# on the server: extract into /var/www/tutoring.new, then swap it with /var/www/tutoring
```

A schema change needs a script in `docs/migrations/` run against the `tutoring` database, and the same change in `docker/postgres/init/`, since the reset reloads only the seed.

## Rebuild from scratch

1. **Swap:** `fallocate -l 1G /swapfile`, `chmod 600`, `mkswap`, `swapon`, a line in `/etc/fstab` and `vm.swappiness=10` in `/etc/sysctl.d/99-swappiness.conf`.
2. **Database**, in the portfolio's PostgreSQL container as the `portfolio` superuser: `CREATE ROLE tutoring LOGIN PASSWORD ...`, `CREATE DATABASE tutoring OWNER tutoring`, `REVOKE CONNECT ON DATABASE tutoring FROM PUBLIC`, `GRANT CONNECT ON DATABASE tutoring TO tutoring`, `REVOKE CONNECT ON DATABASE portfolio FROM PUBLIC`. Write the same password into `.env` in one step, so it is never shown.
3. **Schema and sample data:** pipe `docker/postgres/init/01-schema.sql` and `02-seed.sql` into `psql -U tutoring -d tutoring` inside the container.
4. **API:** `/opt/tutoring-api` with `compose.yaml`, `.env` (600, root) and the JAR; `docker compose up -d`.
5. **Client:** extract the build into `/var/www/tutoring`.
6. **nginx:** install both files, `nginx -t`, `systemctl reload nginx`. Check through the origin before DNS exists: `curl -k --resolve tutoring.joaoabreu.dev:443:127.0.0.1 https://tutoring.joaoabreu.dev/`.
7. **Reset:** copy `02-seed.sql` to `/opt/tutoring-api/seed/`, install the script and the two systemd units, `systemctl enable --now tutoring-demo-reset.timer`, and run the service once.
8. **DNS:** in Cloudflare, `A tutoring` → the static IP, proxied.

## Roll back

```bash
cd /opt/tutoring-api && sudo docker compose down
sudo rm /etc/nginx/sites-enabled/tutoring && sudo systemctl reload nginx
sudo systemctl disable --now tutoring-demo-reset.timer
```

Then remove the DNS record. To drop the demo for good, also `DROP DATABASE tutoring` and `DROP ROLE tutoring` in the PostgreSQL container. None of these steps touch the portfolio.

## First deploy (2026-11-20)

Before: 1905 MB total, 673 MB available, no swap; portfolio API instances 329 and 336 MB, PostgreSQL 68 MB.
After: 389 MB available, swap unused; tutoring API 256 MB of its 384 MB, PostgreSQL 102 MB with both databases; the portfolio's pages and both API instances answered `200`.
