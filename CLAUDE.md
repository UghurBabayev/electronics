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
| `User` | `users` | login, Role enum (ADMIN/USER), `active` flag |
| `Model` | `models` | məhsul modeli; `name`, `brand` (FK), `category` (FK) |
| `Product` | `products` | hər unit **ayrı sıra** (quantity=1); `purchasePrice`, `salePrice`, `purchaseDate`, `model` (FK), `createdBy` (FK→User) |
| `Customer` | `customers` | müştəri bazası; `createdBy` (FK→User) |
| `Sale` | `sales` | satış qiyməti, tarix, PaymentType (CASH/INSTALLMENT), quantity, `customer` (FK), `createdBy` (FK→User) |
| `InstallmentPlan` | `installment_plans` | nisiyə planı |
| `InstallmentPayment` | `installment_payments` | aylıq ödənişlər |
| `InitialBalance` | `initial_balances` | başlanğıc məbləğ; `createdBy` (FK→User) |
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
- Hər modul üçün ayrı JS: `products.js`, `sales.js`, `installments.js`, `customers.js`, `reports.js`, `settings.js`, `models.js`

### UI naviqasiya
| Səhifə | Nə var |
|---|---|
| Məhsullar | Hər unit ayrı sıra; satış qiyməti, status (Stokda/Satılıb) |
| Satışlar | Müştəriyə bağlı; məhsul seçiləndə satış qiyməti avtomatik doldurulur |
| Modellər | Ayrıca CRUD səhifəsi (`models.js`) |
| Ayarlar | Kateqoriyalar, Markalar, Modellər, İstifadəçilər (ADMIN), Başlanğıc məbləğ |

## Əsas funksiyalar
1. **Məhsul idarəsi** — hər unit ayrı sıra (quantity=1); alış + satış qiyməti; model → brand/category zənciri
2. **Satış** — nağd (`CASH`) / nisiyə (`INSTALLMENT`), müştəriyə bağlı
3. **Nisiyə** — aylıq ödəniş cədvəli, `InstallmentStatus` izlənir
4. **Müştəri bazası** — satış tarixçəsi ilə əlaqəli
5. **Başlanğıc məbləğ** — kassa başlanğıcı
6. **Hesabatlar** — mənfəət, borc, inventar (`ReportService`)
7. **İstifadəçi idarəsi** — ADMIN yaradır; hər USER yalnız öz məhsul/satış/müştəri/borcunu görür

## Per-user data izolyasiyası
- `Product`, `Customer`, `Sale`, `InitialBalance` cədvəllərində `created_by` (FK→`users`) var
- `SecurityHelper.isAdmin()` → ADMIN bütün dataları görür; USER yalnız özününküləri
- `SecurityHelper` hər service-ə inject edilib; bütün `getAll/search` metodları bu şərtə görə branch edir

## LazyInitializationException — həll yolu
- `open-in-view: false` olduğundan session `save()` sonrası bağlanır
- **`@EntityGraph`** bütün repository `find*` metodlarına əlavə edilib (eager load)
- **`save()` sonrası həmişə `findById()` ilə yenidən oxu** — `save()` `@EntityGraph` tətbiq etmir:
  ```java
  productRepo.save(p);
  return ProductResponse.from(findOrThrow(id)); // findOrThrow -> findById + @EntityGraph
  ```
- Migration-larda təhlükəsizlik üçün `preConditions onFail="MARK_RAN"` + `columnExists` istifadə et

## Deploy
```bash
railway up --detach
```
- Railway URL: `https://electronics-production.up.railway.app`
- Build logs Railway dashboard-da izlənir

## Tərtibat qaydaları
- Lombok istifadə olunur (`@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`)
- Bütün pul dəyərləri `BigDecimal` (precision=12, scale=2)
- Tarix sahələri `LocalDate` (tarix) və `LocalDateTime` (timestamp)
- `@CreationTimestamp` — `created_at` avtomatik doldurulur
- `FetchType.LAZY` — bütün `@ManyToOne` əlaqələrdə
- `ddl-auto: validate` — hər iki profildə (dev və prod), schema dəyişikliyi yalnız Liquibase ilə
- Migration faylları: `src/main/resources/db/changelog/changes/` → `db.changelog-master.xml`-ə əlavə et