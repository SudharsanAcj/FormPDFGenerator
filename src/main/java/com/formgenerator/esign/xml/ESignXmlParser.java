package com.formgenerator.esign.xml;

import com.formgenerator.esign.model.ESignResponse;
import com.formgenerator.esign.model.ESignStatus;
import com.formgenerator.exception.ESignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class ESignXmlParser {

    /**
     * Parses the eSign gateway callback XML response.
     * Response status="1" means success; anything else is failure.
     */
    public ESignResponse parse(String responseXml) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            // Disable external entity processing (XXE prevention)
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            Document doc = dbf.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(responseXml.getBytes(StandardCharsets.UTF_8)));

            Element root = doc.getDocumentElement();

            String status = root.getAttribute("status");
            String errCode = root.getAttribute("errCode");
            String errMsg = root.getAttribute("errMsg");
            String txn = root.getAttribute("txn");
            String ts = root.getAttribute("ts");

            if (!"1".equals(status)) {
                log.warn("eSign failed — txn={}, errCode={}, errMsg={}", txn, errCode, errMsg);
                return ESignResponse.builder()
                        .status(ESignStatus.FAILED)
                        .transactionId(txn)
                        .errorCode(errCode)
                        .errorMessage(errMsg)
                        .responseTimestamp(ts)
                        .build();
            }

            // Extract UserX509Certificate
            String userCert = extractTextContent(doc, "UserX509Certificate");

            // Extract DocSignature for doc id="1"
            String pkcs7Signature = extractDocSignature(doc, "1");

            return ESignResponse.builder()
                    .status(ESignStatus.SUCCESS)
                    .transactionId(txn)
                    .pkcs7SignatureBase64(pkcs7Signature)
                    .userCertificateBase64(userCert)
                    .responseTimestamp(ts)
                    .build();

        } catch (ESignException e) {
            throw e;
        } catch (Exception e) {
            throw new ESignException("Failed to parse eSign response XML: " + e.getMessage(), e);
        }
    }

    private String extractTextContent(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) return null;
        return nodes.item(0).getTextContent().trim();
    }

    private String extractDocSignature(Document doc, String id) {
        NodeList nodes = doc.getElementsByTagName("DocSignature");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (id.equals(el.getAttribute("id"))) {
                return el.getTextContent().trim();
            }
        }
        return null;
    }
}
