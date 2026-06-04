# ElektronikaDemo — CLAUDE.md

## Layihə haqqında
Kiçik elektronika mağazası üçün veb əsaslı idarəetmə sistemi. 2-3 nəfər istifadəçi üçün nəzərdə tutulub, hamıya açıq deyil.

## Stack
- **Backend:** Java 17 + Spring Boot 3 + Spring Security (JWT)
- **DB:** PostgreSQL + Liquibase (migration-lar `src/main/resources/db/changelog/`)
- **Frontend:** Vanilla HTML/CSS/JS — Spring Boot-un `static/` qovluğundan serve edilir (ayrı SPA deyil)
- **Deploy:** Railway.app

## Paket strukturu
```
az.electronika.demo
├── controller/     — REST endpoint-lər
├── service/        — biznes məntiqi
├── repository/     — Spring Data JPA
├── entity/         — JPA entity-lər
│   └── enums/      — Role, PaymentType, InstallmentStatus
├── dto/            — Request/Response DTO-lar
├── security/       — JwtUtil, JwtFilter, UserDetailsServiceImpl
└── config/         — SecurityConfig, GlobalExceptionHandler
```

## Əsas entity-lər
| Entity | Cədvəl | Qeyd |
|---|---|---|
| `User` | `users` | login, Role enum (ADMIN/USER) |
| `Product` | `products` | alış qiyməti, alış tarixi, kateqoriya, marka, quantity |
| `Customer` | `customers` | müştəri bazası |
| `Sale` | `sales` | satış qiyməti, tarix, PaymentType (CASH/INSTALLMENT), quantity |
| `InstallmentPlan` | `installment_plans` | nisiyə planı |
| `InstallmentPayment` | `installment_payments` | aylıq ödənişlər |
| `InitialBalance` | `initial_balances` | başlanğıc məbləğ |
| `Category` / `Brand` | — | məhsul kateqoriyası/markası |

## Profillər
- **dev** (`application-dev.yml`): `localhost:5432/elektronika_db`, user=postgres, pass=postgres
- **prod** (`application-prod.yml`): `DATABASE_URL` env var

## Environment variable-lar (Railway-də set edilməli)
```
DATABASE_URL=<Railway PostgreSQL connection string>
JWT_SECRET=<minimum 64 simvol>
JWT_EXPIRATION_MS=86400000   # optional, default 24 saat
APP_PROFILE=prod
PORT=8080                    # Railway özü set edir
```

## DB migration
Yeni cədvəl/dəyişiklik üçün `src/main/resources/db/changelog/changes/` altında növbəti nömrəli XML fayl yarat və `db.changelog-master.xml`-ə əlavə et.

## Frontend
- `src/main/resources/static/` — bütün HTML/CSS/JS
- `index.html` — əsas tətbiq (login sonrası)
- `login.html` — giriş səhifəsi
- `js/api.js` — mərkəzi API helper
- Hər modul üçün ayrı JS: `products.js`, `sales.js`, `installments.js`, `customers.js`, `reports.js`, `settings.js`

## Əsas funksiyalar
1. **Məhsul idarəsi** — alış qiyməti, tarix, kateqoriya, marka, stok
2. **Satış** — nağd (`CASH`) / nisiyə (`INSTALLMENT`), müştəriyə bağlı
3. **Nisiyə** — aylıq ödəniş cədvəli, `InstallmentStatus` izlənir
4. **Müştəri bazası** — satış tarixçəsi ilə əlaqəli
5. **Başlanğıc məbləğ** — kassa başlanğıcı
6. **Hesabatlar** — mənfəət, borc, inventar (`ReportService`)

## Tərtibat qaydaları
- Lombok istifadə olunur (`@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`)
- Bütün pul dəyərləri `BigDecimal` (precision=12, scale=2)
- Tarix sahələri `LocalDate` (tarix) və `LocalDateTime` (timestamp)
- `@CreationTimestamp` — `created_at` avtomatik doldurulur
- `FetchType.LAZY` — bütün `@ManyToOne` əlaqələrdə
- `ddl-auto: validate` — hər iki profildə (dev və prod), schema dəyişikliyi yalnız Liquibase ilə