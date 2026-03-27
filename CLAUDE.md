# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

The system has JDK 25 (Homebrew) and GraalVM JDK 21. **Lombok requires Java 21** — always set `JAVA_HOME` before building:

```bash
export JAVA_HOME="/Users/sudharsan.acj/Library/Java/JavaVirtualMachines/graalvm-jdk-21.0.7/Contents/Home"

# Build (skip tests)
mvn clean package -DskipTests

# Run (dev profile)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=PdfFormFillerServiceTest

# Extract actual AcroForm field names from a PDF (run after build)
mvn exec:java -Dexec.mainClass=com.formgenerator.util.PdfFieldExtractorUtil \
              -Dexec.args="\"src/main/resources/forms/BLACKROCK/fresh_purchase.pdf\""
```

**Required environment variables for full functionality:**
- `ASP_KEYSTORE_PASSWORD` — PKCS#12 keystore password for the ASP signing key
- `RTI_BLACKROCK_API_KEY`, `RTI_HDFC_CLIENT_ID`, `RTI_HDFC_CLIENT_SECRET`, etc.

For local dev, `application-dev.properties` uses fallback values so the app starts without these set.

## Architecture Overview

The application fills mutual fund fresh-purchase PDF forms from a JSON payload, applies Aadhaar eSign, and POSTs the signed PDF to the AMC's RTA endpoint.

### Request lifecycle (10 steps)

```
POST /api/v1/investments/fresh-purchase
  1.  @Valid + InvestmentRequest DTO
  2.  MapStruct maps DTO → InvestorDetails (domain model)
  3.  TransactionRecord saved (in-memory store, replace with Redis in prod)
  4.  AmcMappingRegistry loads YAML field mapping for the AMC
  5.  classpath:forms/{AMC}/fresh_purchase.pdf loaded
  6.  FormFillerStrategyFactory dispatches to the AMC's strategy
  7.  PdfFormFillerServiceImpl fills AcroForm fields via PDFBox + reflection path resolver
  8.  SHA-256 hash of filled PDF sent to CDAC eSign gateway (async redirect flow)
  → Returns {transactionId, eSignRedirectUrl} — user authenticates on gateway

POST /api/v1/esign/callback  (called by CDAC gateway after user OTP)
  9.  PKCS#7 signature bytes embedded in PDF via PDFBox incremental save
  10. Signed PDF POSTed as multipart to RTI endpoint (CAMS / KFintech)
```

### Package map

| Package | Responsibility |
|---|---|
| `api/v1/` | REST controllers + `GlobalExceptionHandler` (`@RestControllerAdvice`) |
| `domain/model/` | Canonical domain model (`InvestorDetails` and nested objects) |
| `domain/dto/` | Request/response DTOs, kept separate from domain |
| `domain/mapper/` | MapStruct `InvestorDetailsMapper` — only translation boundary |
| `service/` | `InvestmentOrchestrationService` orchestrates all sub-services |
| `strategy/` | `FormFillerStrategy` per AMC; `FormFillerStrategyFactory` self-registers via `List<FormFillerStrategy>` injection |
| `service/pdf/` | `PdfFormFillerServiceImpl` — PDFBox AcroForm writer with reflection-based path resolver |
| `esign/xml/` | `ESignXmlBuilder` (builds + XMLDSig-signs CDAC request), `ESignXmlParser` |
| `esign/pkcs7/` | `Pkcs7SignatureEmbedder` — PDFBox incremental save with pre-computed PKCS#7 bytes |
| `service/esign/` | `AadhaarESignServiceImpl` — calls gateway via WebClient; Resilience4j circuit breaker |
| `service/rti/` | `RtiSubmissionServiceImpl` — multipart POST; supports API_KEY and OAUTH2 auth |
| `mapping/` | `AmcFieldMapping` / `FieldDefinition` POJOs; `AmcMappingRegistry` loads all `classpath:amc-mappings/*.yaml` on startup |
| `config/` | `AadhaarESignProperties`, `RtiProperties` (`@ConfigurationProperties`); `HttpClientConfig` (two named `WebClient` beans) |
| `store/` | `TransactionStore` — in-memory `ConcurrentHashMap`; replace with Redis for prod |
| `audit/` | `AuditService` — logs audit events; extend to persist to DB or message bus |
| `exception/` | Typed domain exceptions mapped to HTTP codes in `GlobalExceptionHandler` |
| `util/` | `PdfHashUtil`, `DateFormatUtil`, `PdfFieldExtractorUtil` (CLI tool) |

### Adding a new AMC

1. Drop `src/main/resources/forms/{AMC}/fresh_purchase.pdf`
2. Drop `src/main/resources/amc-mappings/{amc}-field-mapping.yaml` (use `blackrock-field-mapping.yaml` as template)
3. Add the code to `AmcName` enum
4. Add `rti.endpoints.{AMC}.*` to `application.properties`
5. Only if the AMC has unique PDF quirks: create `{Amc}FormFillerStrategy implements FormFillerStrategy` annotated `@Component` — it auto-registers

### PDF field mapping YAML

`amc-mappings/*.yaml` keys map `investorFieldPath` (dot-notation path on `InvestorDetails`, supports list indexing like `nominees[0].nomineeName`) to a `pdfFieldName` (exact AcroForm field name in the PDF).

**Getting real field names:** Run `PdfFieldExtractorUtil` against the PDF. The names in `blackrock-field-mapping.yaml` are representative placeholders that must be verified against the actual PDF output.

### WebClient qualifier injection

`HttpClientConfig` produces two named beans: `eSignWebClient` and `rtiWebClient`. Services inject them by matching field name to bean name — this works because `<parameters>true</parameters>` is set in the Maven compiler plugin, preserving constructor parameter names for Spring's qualifier resolution.

### eSign notes

- Spec: CDAC eSign 2.1 (CCA India). ASP must be onboarded with CDAC/NeSL.
- The ASP PKCS#12 keystore is loaded from `esign.asp.keystore-path` at request time.
- The CDAC gateway response XML is XXE-hardened (doctype/external entities disabled in `ESignXmlParser`).
- `Pkcs7SignatureEmbedder` embeds the gateway-returned PKCS#7 bytes directly — it does NOT re-compute a signature.
