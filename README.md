# Transco API

Moteur de **transcodification** — résolution d'une valeur de sortie à partir d'un ou plusieurs critères d'entrée, avec gestion des priorités et des règles de fallback.

---

## Principe

Une règle de transcodification associe un **contexte**, des **critères d'entrée** (clé/valeur) et une **valeur de sortie**.

Lors d'une résolution, le moteur cherche la règle dont les critères sont contenus dans les inputs fournis, et retourne celle qui a la priorité la plus haute. Si aucune règle exacte n'est trouvée et que le fallback est activé, il remonte vers des règles plus génériques jusqu'à une éventuelle règle *catch-all* (sans critères).

**Exemple :**

| context | inputs | output | priorité |
|---|---|---|---|
| tarif | `{pays: FR, canal: web, client: premium}` | TARIF_A | 30 |
| tarif | `{pays: FR, canal: web}` | TARIF_B | 20 |
| tarif | `{pays: FR}` | TARIF_C | 10 |
| tarif | `{}` | TARIF_DEFAULT | 0 |

Requête avec `{pays: FR, canal: web, client: standard}` → retourne **TARIF_B** (fallback sur 2 critères).

---

## Stack technique

| Couche | Technologie |
|---|---|
| Backend | Java 21 · Spring Boot 3 · Spring Data JPA |
| Base de données | PostgreSQL (champ `JSONB` + opérateur `@>`) |
| Mapping | MapStruct |
| Documentation API | SpringDoc OpenAPI (Swagger UI) |
| Frontend | React 18 · Vite |
| Import Excel | Apache POI |

---

## Structure du projet

```
transco-api/                        ← Backend Spring Boot
└── src/main/java/com/transco/api/
    ├── controller/v1/              ← Endpoints REST
    ├── dto/v1/                     ← Records Java 21
    ├── entity/                     ← Entité JPA
    ├── mapper/v1/                  ← MapStruct
    ├── repository/                 ← Requêtes JSONB natives
    └── service/v1/ + impl/v1/     ← Logique métier

transco-admin/                      ← Frontend React / Vite
└── src/
    └── App.jsx                     ← Interface d'administration
```

---

## Schéma de la base de données

```sql
CREATE TABLE transco_rule (
    id           BIGSERIAL PRIMARY KEY,
    context      VARCHAR(100) NOT NULL,
    inputs       JSONB NOT NULL,
    output_value TEXT NOT NULL,
    priority     INT DEFAULT 0,
    created_at   TIMESTAMP DEFAULT NOW(),
    updated_at   TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_transco_inputs ON transco_rule USING GIN (inputs);
CREATE UNIQUE INDEX idx_transco_unique ON transco_rule (context, inputs);
```

Le trigger `trg_transco_rule_updated_at` met à jour `updated_at` automatiquement à chaque modification.

---

## Lancer le projet

### Prérequis

- Java 21
- Maven 3.8+
- PostgreSQL 14+
- Node.js 20+ (frontend)

### Backend

1. Créer la base et exécuter `src/main/resources/init.sql`
2. Configurer `src/main/resources/application.properties` :
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/transco
spring.datasource.username=...
spring.datasource.password=...
```
3. Lancer :
```bash
mvn spring-boot:run
```

API disponible sur `http://localhost:8080`  
Swagger UI sur `http://localhost:8080/swagger-ui.html`

### Frontend

```bash
cd transco-admin
npm install
npm run dev
```

Interface disponible sur `http://localhost:5173`

> Le proxy Vite redirige automatiquement `/api` vers `http://localhost:8080` — aucune configuration CORS nécessaire.

---

## API REST

| Méthode | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/transco-rules` | Liste toutes les règles |
| `GET` | `/api/v1/transco-rules/{id}` | Récupère une règle |
| `POST` | `/api/v1/transco-rules` | Crée une règle |
| `PUT` | `/api/v1/transco-rules/{id}` | Met à jour une règle |
| `DELETE` | `/api/v1/transco-rules/{id}` | Supprime une règle |
| `POST` | `/api/v1/transco-rules/resolve` | Résout une valeur |
| `POST` | `/api/v1/transco-rules/import` | Importe depuis un fichier `.xlsx` |

### Exemple de résolution

```bash
curl -X POST http://localhost:8080/api/v1/transco-rules/resolve \
  -H "Content-Type: application/json" \
  -d '{
    "context": "tarif",
    "inputs": { "pays": "FR", "canal": "web" },
    "withFallback": true
  }'
```

---

## Import Excel

Le endpoint `POST /api/v1/transco-rules/import` accepte un fichier `.xlsx` en multipart.

**Format attendu** — colonnes fixes + colonnes dynamiques (critères d'entrée) :

| context | output_value | priority | pays | canal | client |
|---|---|---|---|---|---|
| tarif | TARIF_A | 30 | FR | web | premium |
| tarif | TARIF_B | 20 | FR | web | |
| tarif | TARIF_DEFAULT | 0 | | | |

- Colonnes `context` et `output_value` obligatoires
- `priority` optionnelle (défaut : 0)
- Toutes les autres colonnes sont des critères d'entrée — une cellule vide signifie que ce critère est absent de la règle
- Les doublons sont ignorés silencieusement (contrainte unique en base)

**Réponse :**
```json
{
  "inserted": 8,
  "skipped": 2,
  "rejected": 0,
  "errors": []
}
```

---

## Versioning

Toutes les couches applicatives (controller, dto, service, mapper) sont organisées par version (`v1`). L'ajout d'une `v2` ne perturbe pas l'existant.

---

## Licence

MIT
