## Use Nginx to serve pre-built frontend
FROM nginx:stable-alpine

COPY dist/front/browser /usr/share/nginx/html

COPY nginx-custom.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
