# ---- Stage 1: build -------------------------------------------------------
# The frontend is static today, so this stage only copies. It exists so that
# adding a build step later (Vite/React) is a change to one RUN line instead
# of a restructure.
FROM nginx:1.30-alpine AS build
WORKDIR /src
COPY . /src/
# Sanity check: fail the build early if the app shell is missing.
RUN test -f "Login Page/LoginPage.html" || (echo "ERROR: Login Page/LoginPage.html not found" && exit 1)

# ---- Stage 2: runtime -----------------------------------------------------
FROM nginx:1.30-alpine AS runtime

# Our site config replaces the stock one (it also lives inside the http{}
# block, so limit_req_zone etc. are valid here).
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build "/src/Login Page/" /usr/share/nginx/html/

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -q --spider http://localhost/healthz || exit 1

CMD ["nginx", "-g", "daemon off;"]
