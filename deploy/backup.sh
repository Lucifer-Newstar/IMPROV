#!/usr/bin/env bash
# IMPROV — database backup.
#
# Dumps the MySQL `db` service (single transaction, routines + triggers),
# gzips it into BACKUP_DIR, prunes anything older than RETENTION_DAYS, and —
# if the aws CLI is present and S3_BUCKET is set — copies it off-site.
#
# Run from the repository root on the host where the stack runs, e.g. cron:
#
#   0 3 * * *  cd /opt/improv && ./deploy/backup.sh >> /var/log/improv-backup.log 2>&1
#
# Restore (into a fresh MySQL with the same database name):
#
#   gunzip < backups/improv-YYYYMMDD-HHMMSS.sql.gz \
#     | docker compose exec -T db mysql -u root -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"
#
# The restore path is proven in CI by .github/workflows/backup.yml.
set -euo pipefail

RETENTION_DAYS="${RETENTION_DAYS:-30}"
BACKUP_DIR="${BACKUP_DIR:-./backups}"

mkdir -p "$BACKUP_DIR"
STAMP="$(date -u +%Y%m%d-%H%M%S)"
FILE="$BACKUP_DIR/improv-$STAMP.sql.gz"

# MYSQL_PWD keeps the password off the command line (and out of `ps`).
docker compose exec -T db sh -c \
  'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqldump -u root --single-transaction --routines --triggers "$MYSQL_DATABASE"' \
  | gzip > "$FILE"

# Prune old local backups.
find "$BACKUP_DIR" -name 'improv-*.sql.gz' -mtime +"$RETENTION_DAYS" -delete

echo "backup written: $FILE ($(du -h "$FILE" | cut -f1))"

# Optional off-site copy.
if [ -n "${S3_BUCKET:-}" ] && command -v aws >/dev/null 2>&1; then
  aws s3 cp "$FILE" "s3://$S3_BUCKET/db/$(basename "$FILE")"
  echo "uploaded to s3://$S3_BUCKET/db/$(basename "$FILE")"
fi
