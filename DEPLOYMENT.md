# Deploy Lumiere Ecommerce

Recommended free-friendly setup:

- App hosting: Koyeb Web Service from GitHub
- Database: Neon PostgreSQL free database
- Images: Cloudinary, because app server disk storage is temporary on free app hosts

## 1. Create The Database

1. Create a free Neon project.
2. Copy the pooled PostgreSQL connection string.
3. Keep the database username and password available.

Use the pooled connection string if Neon gives both pooled and direct URLs. It is better for small hosted apps.

## 2. Deploy The App On Koyeb

1. Push this repository to GitHub.
2. Open Koyeb and create a new Web Service.
3. Choose GitHub as the source.
4. Select this repository and the `main` branch.
5. Choose Dockerfile deployment.
6. Expose port `8080`.
7. Add the environment variables below.
8. Deploy and open the generated `.koyeb.app` URL.

## 3. Environment Variables

Required:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_NEON_HOST/YOUR_DB?sslmode=require
SPRING_DATASOURCE_USERNAME=YOUR_DB_USER
SPRING_DATASOURCE_PASSWORD=YOUR_DB_PASSWORD
RAZORPAY_KEY_ID=YOUR_RAZORPAY_KEY_ID
RAZORPAY_KEY_SECRET=YOUR_RAZORPAY_KEY_SECRET
ADMIN_EMAILS=your-email@example.com
```

Recommended if product images are uploaded from the admin page:

```text
CLOUDINARY_CLOUD_NAME=YOUR_CLOUD_NAME
CLOUDINARY_API_KEY=YOUR_API_KEY
CLOUDINARY_API_SECRET=YOUR_API_SECRET
```

Optional for Google login:

```text
GOOGLE_CLIENT_ID=YOUR_GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET=YOUR_GOOGLE_CLIENT_SECRET
```

## 4. Notes

- The app already reads the hosting platform `PORT` variable through `server.port=${PORT:8080}`.
- Do not commit secrets to GitHub. Add them only in the hosting dashboard.
- Free hosts can sleep after inactivity, so the first request may be slower.
- If image upload is used, Cloudinary credentials are important because local `uploads/` files will not persist reliably on free app services.
