package com.formgenerator.esign.pkcs7;

import com.formgenerator.exception.ESignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Calendar;

@Slf4j
@Component
public class Pkcs7SignatureEmbedder {

    /**
     * Embeds the PKCS#7 / CMS signature bytes (received from the CDAC eSign gateway)
     * into the PDF using PDFBox's incremental save / external signing mechanism.
     *
     * @param filledPdfBytes        the filled (unsigned) PDF bytes
     * @param pkcs7SignatureBase64  Base64-encoded PKCS#7 signature bytes from the gateway
     * @param signerName            investor's name for the /Name field of the signature dict
     * @return signed PDF bytes with embedded PKCS#7 signature
     */
    public byte[] embedSignature(byte[] filledPdfBytes, String pkcs7SignatureBase64, String signerName) {
        byte[] pkcs7Bytes = Base64.getDecoder().decode(pkcs7SignatureBase64);

        try (PDDocument doc = Loader.loadPDF(filledPdfBytes)) {
            ByteArrayOutputStream signedOutput = new ByteArrayOutputStream();

            PDSignature pdSignature = new PDSignature();
            pdSignature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            pdSignature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            pdSignature.setName(signerName);
            pdSignature.setReason("Aadhaar eSign - Fresh Purchase Application");
            pdSignature.setLocation("India");
            pdSignature.setSignDate(Calendar.getInstance());

            // Reserve space for the PKCS#7 bytes — must be at least as large as pkcs7Bytes
            SignatureOptions options = new SignatureOptions();
            // Reserve 2x the actual signature size to be safe
            options.setPreferredSignatureSize(pkcs7Bytes.length * 2 + 1024);

            // ExternalSigningSupport allows injecting pre-computed signature bytes
            doc.addSignature(pdSignature, externalSigning -> pkcs7Bytes, options);
            doc.saveIncremental(signedOutput);

            log.debug("PKCS#7 signature embedded successfully for signer: {}", signerName);
            return signedOutput.toByteArray();

        } catch (IOException e) {
            throw new ESignException("Failed to embed PKCS#7 signature into PDF: " + e.getMessage(), e);
        }
    }
}
