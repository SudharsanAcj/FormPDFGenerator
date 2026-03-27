# Add New AMC Form

Guide me through adding a new mutual fund AMC fresh-purchase PDF form to this application.

The AMC to add is: $ARGUMENTS

Follow these steps in order, pausing to ask for confirmation or input where noted.

---

## Step 1 — Gather inputs

Ask the user for:
1. **AMC name / code** (e.g., `HDFC`, `ICICI`, `SBI`) — will become the enum value
2. **PDF template file path** on their machine (e.g., `/Downloads/hdfc_fresh_purchase.pdf`)
3. **RTI endpoint URL** for this AMC
4. **RTI auth type**: `API_KEY`, `OAUTH2`, or `NONE`

---

## Step 2 — Copy the PDF template

Copy (or instruct the user to copy) the PDF to:
```
src/main/resources/forms/{AMC_CODE}/fresh_purchase.pdf
```
Create the directory if it doesn't exist.

---

## Step 3 — Add AMC to the enum

Open `src/main/java/com/formgenerator/domain/model/AmcName.java` and add the new value to the enum:
```java
{AMC_CODE}("{amc_code_lowercase}")
```
Follow the existing pattern exactly.

---

## Step 4 — Add RTI properties

Add to `src/main/resources/application.properties`:
```properties
rti.endpoints.{amc_code_lowercase}.url={RTI_ENDPOINT_URL}
rti.endpoints.{amc_code_lowercase}.auth-type={AUTH_TYPE}
# If API_KEY:
# rti.endpoints.{amc_code_lowercase}.api-key=${RTI_{AMC_CODE}_API_KEY}
# If OAUTH2:
# rti.endpoints.{amc_code_lowercase}.client-id=${RTI_{AMC_CODE}_CLIENT_ID}
# rti.endpoints.{amc_code_lowercase}.client-secret=${RTI_{AMC_CODE}_CLIENT_SECRET}
```

---

## Step 5 — Build the JAR and run the diagnostic fill

Run these commands to identify every AcroForm field name in the PDF:

```bash
export JAVA_HOME="/Users/sudharsan.acj/Library/Java/JavaVirtualMachines/graalvm-jdk-21.0.7/Contents/Home"
mvn clean package -DskipTests

# Extract all field names with types and on-values
mvn exec:java -Dexec.mainClass=com.formgenerator.util.PdfFieldExtractorUtil \
              -Dexec.args="\"src/main/resources/forms/{AMC_CODE}/fresh_purchase.pdf\""

# Fill every field with its own name so you can see positions visually
java -cp "$(find target -name 'form-pdf-generator-*.jar' | head -1)" \
     com.formgenerator.util.PdfDiagnosticFillUtil \
     "src/main/resources/forms/{AMC_CODE}/fresh_purchase.pdf" \
     "/tmp/diagnostic_{amc_code_lowercase}.pdf"
```

Open `/tmp/diagnostic_{amc_code_lowercase}.pdf` and screenshot each section so we can map field names to visual positions.

---

## Step 6 — Create the YAML field mapping

Create `src/main/resources/amc-mappings/{amc_code_lowercase}-field-mapping.yaml`.

Use `blackrock-field-mapping.yaml` as the template. The structure for each field is:

```yaml
fieldMappings:

  # TEXT field example
  firstName:
    investorFieldPath: firstName
    pdfFieldName: "Text7"          # exact AcroForm name from Step 5
    fieldType: TEXT
    formatter: UPPER_CASE          # optional: UPPER_CASE | DATE_DD_MM_YYYY | AMOUNT_IN_FIGURES | TITLE_CASE

  # CHECKBOX example (ticked when investorFieldPath value equals checkboxTrueValue)
  statusIndividual:
    investorFieldPath: status
    pdfFieldName: "Check Box78"
    fieldType: CHECKBOX
    checkboxTrueValue: "INDIVIDUAL"

  # List indexing for joint applicants / nominees
  nominee1Name:
    investorFieldPath: "nominees[0].nomineeName"
    pdfFieldName: "Text57"
    fieldType: TEXT
    formatter: UPPER_CASE

  # Amount field
  amount:
    investorFieldPath: paymentDetails.amount
    pdfFieldName: "Text34"
    fieldType: AMOUNT
    formatter: AMOUNT_IN_FIGURES
```

**Standard InvestorDetails paths to map** (check which fields exist in this AMC's form):

| Section | investorFieldPath | Notes |
|---|---|---|
| Distributor | `distributorArn`, `subBrokerCode`, `euin` | |
| Transaction type | `transactionChargeType` | CHECKBOX, values: `EXISTING_INVESTOR` / `FIRST_TIME_INVESTOR` |
| Applicant | `firstName`, `title`, `dateOfBirth`, `pan`, `gender`, `status` | |
| Address | `correspondenceAddress.line1`, `.line2`, `.city`, `.pinCode`, `.state` | |
| Contact | `mobile`, `email`, `stdCode`, `officePhone` | |
| Joint applicants | `jointApplicants[0].firstName`, `[0].pan`, `[0].gender` | Repeat for [1] |
| Mode of holding | `modeOfHolding` | Values: `SINGLE`, `JOINT`, `ANYONE_OR_SURVIVOR` |
| Bank | `bankDetails.bankName`, `.accountNumber`, `.ifscCode`, `.micrCode`, `.branchName`, `.accountType` | accountType values: `SAVINGS`, `CURRENT`, `NRO`, `NRE`, `FCNR` |
| Payment | `paymentDetails.paymentMode`, `.chequeOrDdNumber`, `.chequeOrDdDate`, `.amount` | paymentMode values: `CHEQUE`, `DD`, `NEFT`, `RTGS`, `UPI` |
| Unit holding | `unitHoldingOption` | Values: `PHYSICAL`, `DEMAT` |
| Nominees | `nominees[0].nomineeName`, `[0].allocationPercentage`, `[0].guardianName` | Repeat for [1], [2] |
| KYC | `kycNumber` | |

---

## Step 7 — Rebuild and test

```bash
mvn clean package -DskipTests
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Send a test request to `POST /api/v1/dev/preview-filled-form` with `"amcName": "{AMC_CODE}"` and representative investor data. Download and visually verify the filled PDF.

---

## Step 8 — Custom strategy (only if needed)

If the AMC's PDF has quirks that the generic filler can't handle (e.g., calculated fields, special multi-field logic, conditional sections), create:

```
src/main/java/com/formgenerator/strategy/{AmcCode}FormFillerStrategy.java
```

```java
@Component
public class {AmcCode}FormFillerStrategy implements FormFillerStrategy {

    private final PdfFormFillerService pdfFormFillerService;

    public {AmcCode}FormFillerStrategy(PdfFormFillerService pdfFormFillerService) {
        this.pdfFormFillerService = pdfFormFillerService;
    }

    @Override
    public AmcName getSupportedAmc() { return AmcName.{AMC_CODE}; }

    @Override
    public byte[] fill(byte[] template, InvestorDetails investor, AmcFieldMapping mapping) {
        // pre-processing if needed
        byte[] filled = pdfFormFillerService.fillForm(template, investor, mapping);
        // post-processing if needed
        return filled;
    }
}
```

`FormFillerStrategyFactory` auto-discovers `@Component` implementations — no registration needed.

---

## Checklist

- [ ] PDF template at `src/main/resources/forms/{AMC_CODE}/fresh_purchase.pdf`
- [ ] `AmcName` enum updated
- [ ] RTI properties added to `application.properties`
- [ ] YAML mapping at `src/main/resources/amc-mappings/{amc_code_lowercase}-field-mapping.yaml`
- [ ] `amcName:` header in YAML matches enum value exactly
- [ ] `pdfTemplatePath: classpath:forms/{AMC_CODE}/fresh_purchase.pdf` in YAML
- [ ] Built with `mvn clean package -DskipTests` after all edits
- [ ] Visual test via `/api/v1/dev/preview-filled-form` passes
