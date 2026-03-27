#!/bin/bash
# DSP BlackRock FTP Fresh Purchase Form — Preview Filled PDF
# Fills all fields: applicant, address, bank, payment, nominees, joint applicants
# Output: /tmp/filled_blackrock_all_fields.pdf

BASE_URL="http://localhost:8080/form-pdf-generator"

curl -s -X POST "${BASE_URL}/api/v1/dev/preview-filled-form" \
  -H "Content-Type: application/json" \
  -o /tmp/filled_blackrock_all_fields.pdf \
  -w "\nHTTP_STATUS:%{http_code}\nSIZE:%{size_download}\n" \
  -d '{
  "amcName": "BLACKROCK",
  "transactionChargeType": "EXISTING_INVESTOR",

  "distributorArn": "ARN-12345",
  "subBrokerCode": "SB-999",
  "euin": "E123456",

  "title": "MR",
  "firstName": "RAJESH KUMAR",
  "lastName": "SHARMA",
  "dateOfBirth": "15/06/1980",
  "gender": "MALE",
  "status": "INDIVIDUAL",
  "pan": "ABCPS1234Z",
  "kycNumber": "KYC9876543210",

  "correspondenceAddress": {
    "line1": "42 SAKET NAGAR, BHOPAL",
    "line2": "NEAR KALI MANDIR",
    "city": "BHOPAL",
    "state": "MADHYA PRADESH",
    "pinCode": "462024",
    "country": "INDIA"
  },

  "mobile": "9876543210",
  "email": "rajesh.sharma@email.com",
  "stdCode": "0755",
  "officePhone": "07552345678",

  "modeOfHolding": "JOINT",
  "jointApplicants": [
    {
      "firstName": "SUNITA",
      "lastName": "SHARMA",
      "gender": "FEMALE",
      "pan": "BCDQS5678Y",
      "dateOfBirth": "20/09/1983"
    },
    {
      "firstName": "ARVIND",
      "lastName": "SHARMA",
      "gender": "MALE",
      "pan": "CDERS6789Z",
      "dateOfBirth": "05/03/1985"
    }
  ],

  "bankDetails": {
    "bankName": "STATE BANK OF INDIA",
    "accountNumber": "40123456789",
    "accountType": "SAVINGS",
    "ifscCode": "SBIN0001234",
    "micrCode": "462002001",
    "branchName": "BHOPAL MAIN BRANCH"
  },

  "paymentDetails": {
    "paymentMode": "CHEQUE",
    "chequeOrDdNumber": "001234",
    "chequeOrDdDate": "27/03/2026",
    "amount": 50000.00
  },

  "investmentDetails": {
    "schemeName": "DSP BlackRock FTP Series 13 - 15M",
    "schemeCode": "DSP-FTP-13",
    "planType": "GROWTH",
    "dividendOption": "PAYOUT",
    "investmentAmount": 50000.00
  },

  "unitHoldingOption": "PHYSICAL",

  "nominees": [
    {
      "nomineeName": "PRIYA SHARMA",
      "allocationPercentage": 50,
      "relationship": "DAUGHTER",
      "dateOfBirth": "10/02/2005",
      "guardianName": "SUNITA SHARMA"
    },
    {
      "nomineeName": "ROHIT SHARMA",
      "allocationPercentage": 30,
      "relationship": "SON",
      "dateOfBirth": "22/07/2008",
      "guardianName": "SUNITA SHARMA"
    },
    {
      "nomineeName": "KAVITA SHARMA",
      "allocationPercentage": 20,
      "relationship": "MOTHER"
    }
  ],

  "aadhaarOrVid": "123456789012"
}'

echo "Saved to: /tmp/filled_blackrock_all_fields.pdf"
open /tmp/filled_blackrock_all_fields.pdf
