#!/bin/bash
# Jio BlackRock Common Application Form — Preview Filled PDF
# Fills all fields: applicant, address, bank, payment, nominees, joint applicants
# Output: /tmp/filled_jio_blackrock.pdf

BASE_URL="http://localhost:8080/form-pdf-generator"

curl -s -X POST "${BASE_URL}/api/v1/dev/preview-filled-form" \
  -H "Content-Type: application/json" \
  -o /tmp/filled_jio_blackrock.pdf \
  -w "\nHTTP_STATUS:%{http_code}\nSIZE:%{size_download}\n" \
  -d '{
  "amcName": "JIO_BLACKROCK",

  "distributorArn": "ARN-12345",

  "firstName": "RAJESH KUMAR",
  "lastName": "SHARMA",
  "dateOfBirth": "15/06/1980",
  "status": "INDIVIDUAL",
  "pan": "ABCPS1234Z",
  "kycNumber": "KYC9876543210",

  "mobile": "9876543210",
  "email": "rajesh.sharma@email.com",

  "correspondenceAddress": {
    "line1": "42 SAKET NAGAR, BHOPAL",
    "line2": "NEAR KALI MANDIR",
    "city": "BHOPAL",
    "state": "MADHYA PRADESH",
    "country": "INDIA",
    "pinCode": "462024"
  },

  "modeOfHolding": "JOINT",
  "jointApplicants": [
    {
      "firstName": "SUNITA",
      "lastName": "SHARMA",
      "pan": "BCDQS5678Y",
      "dateOfBirth": "20/09/1983"
    },
    {
      "firstName": "ARVIND",
      "lastName": "SHARMA",
      "pan": "CDERS6789Z",
      "dateOfBirth": "05/03/1985"
    }
  ],

  "bankDetails": {
    "bankName": "STATE BANK OF INDIA",
    "accountNumber": "40123456789",
    "accountType": "SAVINGS",
    "ifscCode": "SBIN0001234",
    "branchName": "BHOPAL MAIN BRANCH"
  },

  "paymentDetails": {
    "paymentMode": "CHEQUE",
    "chequeOrDdNumber": "001234",
    "chequeOrDdDate": "27/03/2026",
    "amount": 50000.00
  },

  "investmentDetails": {
    "schemeName": "JioBlackRock Liquid Fund - Growth",
    "planType": "GROWTH",
    "investmentAmount": 50000.00
  },

  "unitHoldingOption": "PHYSICAL",

  "nominees": [
    {
      "nomineeName": "PRIYA SHARMA",
      "allocationPercentage": 100,
      "dateOfBirth": "10/02/2005",
      "guardianName": "SUNITA SHARMA"
    }
  ],

  "aadhaarOrVid": "123456789012"
}'

echo "Saved to: /tmp/filled_jio_blackrock.pdf"
open /tmp/filled_jio_blackrock.pdf
