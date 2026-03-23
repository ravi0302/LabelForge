# 🏷️ LabelForge

> Open-source EAN-13 barcode generator for small manufacturers and food businesses.  
> Built with Spring Boot 3 · React 18 · PostgreSQL

---

## 💡 Story

Our family runs a small home food business. My dad asked me to find a paid online tool to generate barcodes for our product labels. I thought — *why pay for something I can build over a weekend?*

Built in ~2 days with the help of **Claude AI** as a pair programming assistant. This is a real-world project actively used for our home products.

---

## ✨ Features

- **EAN-13 barcode generation** — auto-generated using a DB sequence, or manually enter your own
- **Product catalog** — full CRUD with name, weight, price, category
- **Category management** — filter products by category (Flour, Masala, Farsan etc.)
- **Barcode download** — single PNG per product or bulk ZIP for selected products
- **Excel export** — download product catalog as `.xlsx`, filtered by category
- **Excel import** — upload a spreadsheet, preview rows, resolve duplicate EAN conflicts before committing
- **GS1 prefix settings** — configure your company prefix (internal `2xxxxx` range for MVP, real GS1 prefix for retail)
- **Print-ready label preview** — preview and print labels directly from the browser
- **Bulk operations** — select multiple products, bulk delete or bulk ZIP download

---

## 🖥️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.2, Java 17 |
| Database | PostgreSQL 15 |
| ORM | Spring Data JPA, Hibernate 6 |
| Barcode generation | ZXing 3.5 (server-side PNG), JsBarcode (client-side SVG) |
| Excel processing | Apache POI 5.2 |
| Frontend | React 18, Vite |
| HTTP client | Axios |
| ZIP export | JSZip |
| Excel (frontend) | SheetJS (xlsx) |

---

## 📁 Project Structure

```
LabelForge/
├── labelforge-api/          # Spring Boot backend
│   ├── src/main/java/com/labelforge/
│   │   ├── controller/      # REST endpoints
│   │   ├── service/         # Business logic
│   │   ├── repository/      # JPA repositories
│   │   ├── model/           # JPA entities
│   │   ├── dto/             # Request / Response DTOs
│   │   ├── exception/       # Global error handling
│   │   └── config/          # CORS config
│   └── src/main/resources/
│       ├── application.properties
│       ├── schema.sql        # v1 — initial schema
│       └── schema_v2.sql     # v2 — categories + settings
│
└── labelforge-ui/           # React frontend
    └── src/
        ├── App.jsx
        ├── App.css
        └── api/
            └── productApi.js
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Node.js 18+
- npm 9+

---

### 1. Database Setup

Connect to PostgreSQL and run the schema files in order:

```bash
psql -U postgres -f labelforge-api/src/main/resources/schema.sql
psql -U postgres -d labelforge -f labelforge-api/src/main/resources/schema_v2.sql
```

This creates the `labelforge` database, `products`, `categories`, and `settings` tables, and seeds default categories.

---

### 2. Backend Setup

```bash
cd labelforge-api
```

Copy the example config and fill in your values:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Edit `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/labelforge
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
labelforge.ean.company-prefix=20001
labelforge.cors.allowed-origins=http://localhost:5173
spring.jpa.open-in-view=false
```

Run the backend:

```bash
mvn clean install
mvn spring-boot:run
```

Backend starts at `http://localhost:8080`

Health check:
```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

---

### 3. Frontend Setup

```bash
cd labelforge-ui
npm install
npm run dev
```

Frontend starts at `http://localhost:5173`

The Vite dev server proxies all `/api/*` requests to `http://localhost:8080` — no CORS issues during development.

---

## 📡 API Reference

Base URL: `http://localhost:8080/api/v1`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/products` | Create product + generate EAN-13 |
| `GET` | `/products` | List all products (supports `?search=` and `?categoryId=`) |
| `GET` | `/products/{id}` | Get single product |
| `PUT` | `/products/{id}` | Update product |
| `DELETE` | `/products/{id}` | Delete product |
| `DELETE` | `/products/bulk` | Bulk delete by IDs |
| `GET` | `/products/{id}/barcode` | Download EAN-13 barcode as PNG |
| `POST` | `/products/import/preview` | Parse Excel file, return preview with conflict info |
| `POST` | `/products/import/commit` | Commit import after conflict resolution |
| `GET` | `/products/export/excel` | Export products as `.xlsx` |
| `GET` | `/categories` | List all categories |
| `POST` | `/categories` | Create category |
| `DELETE` | `/categories/{id}` | Delete category |
| `GET` | `/settings` | Get app settings (GS1 prefix) |
| `PUT` | `/settings` | Update settings |

---

## 🔐 EAN-13 Structure

```
[ company prefix (5–7 digits) ] [ item reference ] [ check digit (1) ]
        20001                       00001                  X
```

- The `2xxxxx` number system prefix is **reserved by GS1 for internal/in-store use** — no registration needed for internal labelling.
- When expanding to retail supply chain, purchase a GS1 Company Prefix at [gs1.org](https://gs1.org) and update it in Settings.
- The item reference comes from a **PostgreSQL sequence** — guaranteed unique even under concurrent requests.

---

## 📸 Screenshots
<img width="1886" height="943" alt="image" src="https://github.com/user-attachments/assets/c4175470-93ec-483c-8301-9a6f658e8a7b" />



---

## 🛣️ Roadmap

- [ ] PDF label export (print-ready, multi-label per page)
- [ ] QR code support alongside EAN-13
- [ ] Direct thermal printer integration (Zebra, Dymo)
- [ ] User authentication (Spring Security + JWT)
- [ ] Multi-tenant SaaS with per-tenant GS1 prefix

---

## 🤝 Built With

- [Spring Boot](https://spring.io/projects/spring-boot)
- [React](https://react.dev)
- [PostgreSQL](https://www.postgresql.org)
- [ZXing](https://github.com/zxing/zxing)
- [Apache POI](https://poi.apache.org)
- [JsBarcode](https://github.com/lindell/JsBarcode)
- [Claude AI](https://claude.ai) — AI pair programming assistant

---

## 👨‍💻 Developer

**Ravi Gawas**  
[LinkedIn](https://linkedin.com/in/ravi-gawas) · [GitHub](https://github.com/your-username)

---

## 📄 License

MIT License — free to use, modify and distribute.
