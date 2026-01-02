# 🏦 ATM Backend

Backend REST API per il simulatore ATM, sviluppato con **Spring Boot 4** e **Java 21**.

## 🛠️ Tecnologie

- **Java 21** - JDK
- **Spring Boot 4.0.1** - Framework
- **Spring Security** - Autenticazione JWT
- **Spring Data JPA** - ORM
- **PostgreSQL 16** - Database
- **Maven** - Build tool
- **Docker** - Containerizzazione

## 📋 Prerequisiti

- Java 21+
- PostgreSQL 16+
- Maven 3.9+ (oppure usa il wrapper `./mvnw`)

## ⚙️ Configurazione

### 1. Database PostgreSQL

Crea il database:

```sql
CREATE DATABASE atm;
```

### 2. File .env

Crea un file `.env` nella root del progetto (non verrà committato su Git):

```env
# Database PostgreSQL
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/atm
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=tua_password_postgres

# JWT Secret (minimo 32 caratteri)
ATM_JWT_SECRET=una-chiave-segreta-lunga-almeno-32-caratteri

# API Key per comunicazione con servizio Chat
ATM_INTERNAL_API_KEY=una-api-key-sicura

# CORS - origini permesse (comma-separated)
ATM_CORS_ORIGINS=http://localhost:5173,http://localhost:3000

# Brute force protection - tentativi PIN prima del blocco
ATM_MAX_PIN_ATTEMPTS=3

# Demo data - crea utente/carta demo al primo avvio
ATM_SEED_ENABLED=true

# Ambiente
ATM_PRODUCTION=false
```

> ⚠️ **IMPORTANTE**: Il file `.env` contiene credenziali sensibili e **NON deve essere committato** su Git. È già incluso nel `.gitignore`.

### 3. Variabili d'Ambiente (Alternativa)

In alternativa al file `.env`, puoi settare le variabili d'ambiente del sistema operativo.

## 🚀 Avvio

### Sviluppo locale

```bash
# Con Maven wrapper (consigliato)
./mvnw spring-boot:run

# Oppure con Maven installato
mvn spring-boot:run
```

L'applicazione sarà disponibile su: `http://localhost:8080`

### Build JAR

```bash
./mvnw clean package -DskipTests
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### Docker

```bash
# Build immagine
docker build -t atm-backend .

# Run container
docker run -p 8080:8080 --env-file .env atm-backend
```

## 🧪 Test

```bash
# Esegui tutti i test
./mvnw test

# Test specifico
./mvnw test -Dtest=AuthServiceTest
```

I test usano **H2 in-memory database**, non richiedono PostgreSQL.

## 📡 API Endpoints

### Autenticazione (pubblici)

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `POST` | `/api/auth/validate-card` | Valida numero carta |
| `POST` | `/api/auth/card-login` | Login con carta e PIN |

### Operazioni Conto (autenticati)

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/accounts/{id}` | Dettagli conto |
| `POST` | `/api/accounts/{id}/deposit` | Deposito |
| `POST` | `/api/accounts/{id}/withdraw` | Prelievo |

### Interni (protetti da API Key)

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `POST` | `/api/internal/unlock-card` | Sblocca carta bloccata |

## 🔐 Sicurezza

- **JWT HS256** - Token per autenticazione utenti
- **BCrypt** - Hash delle password/PIN
- **Brute Force Protection** - Blocco carta dopo 3 tentativi PIN errati
- **API Key** - Protezione endpoint interni
- **CORS** - Configurabile via environment

## 📁 Struttura Progetto

```
BE/
├── src/main/java/com/azienda/demo/
│   ├── api/                    # Controller REST
│   │   ├── dto/                # Data Transfer Objects
│   │   ├── AccountController.java
│   │   ├── AuthController.java
│   │   └── InternalController.java
│   ├── config/                 # Configurazione Spring
│   │   ├── ApiKeyFilter.java   # Filtro API Key
│   │   ├── JwtService.java     # Gestione JWT
│   │   └── SecurityConfig.java # Spring Security
│   ├── data/                   # Seed dati demo
│   ├── domain/                 # Entità JPA
│   │   ├── Account.java
│   │   ├── Card.java
│   │   └── User.java
│   ├── repo/                   # Repository JPA
│   └── service/                # Business logic
│       ├── exception/          # Eccezioni custom
│       ├── AccountService.java
│       └── AuthService.java
├── src/main/resources/
│   └── application.properties  # Configurazione
├── src/test/                   # Test
├── .env.example                # Template environment
├── .gitignore
├── Dockerfile
└── pom.xml
```

## 🎭 Dati Demo

Con `ATM_SEED_ENABLED=true`, al primo avvio vengono creati:

| Campo | Valore |
|-------|--------|
| **Numero Carta** | `1111222233334444` |
| **PIN** | `1234` |
| **Saldo iniziale** | €1000.00 |
| **Titolare** | Mario Rossi |

## 🌐 Deploy

### Render / Railway / Fly.io

1. Collega il repository GitHub
2. Configura le variabili d'ambiente nella dashboard del servizio
3. Il Dockerfile verrà usato automaticamente per il build

### Variabili richieste in produzione

```
SPRING_DATASOURCE_URL=jdbc:postgresql://tuo-db-host:5432/db_bancomat
SPRING_DATASOURCE_USERNAME=db_user
SPRING_DATASOURCE_PASSWORD=password_sicura_prod
ATM_JWT_SECRET=chiave-jwt-produzione-molto-lunga
ATM_INTERNAL_API_KEY=api-key-produzione
ATM_CORS_ORIGINS=https://tuodominio.github.io
ATM_PRODUCTION=true
ATM_SEED_ENABLED=false
```

## 📄 Licenza

MIT License

## 👤 Autore

Sviluppato come progetto portfolio - Simulatore ATM Full Stack
