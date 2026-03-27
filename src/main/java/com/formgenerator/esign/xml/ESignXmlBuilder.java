package com.formgenerator.esign.xml;

import com.formgenerator.config.AadhaarESignProperties;
import com.formgenerator.esign.model.ESignRequest;
import com.formgenerator.exception.ESignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ESignXmlBuilder {

    private final AadhaarESignProperties eSignProperties;

    /**
     * Builds the CDAC eSign 2.1 XML request, signs it with the ASP private key,
     * and returns the signed XML as a UTF-8 string.
     */
    public String buildSignedXml(ESignRequest request) {
        try {
            Document doc = buildXmlDocument(request);
            signDocument(doc);
            return documentToString(doc);
        } catch (ESignException e) {
            throw e;
        } catch (Exception e) {
            throw new ESignException("Failed to build eSign XML request: " + e.getMessage(), e);
        }
    }

    private Document buildXmlDocument(ESignRequest request) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().newDocument();

        // Root <Esign> element
        Element esign = doc.createElement("Esign");
        esign.setAttribute("ver", request.getVersion());
        esign.setAttribute("ts", request.getTimestamp());
        esign.setAttribute("txn", request.getTransactionId());
        esign.setAttribute("aspId", request.getAspId());
        esign.setAttribute("AuthMode", request.getAuthMode());
        esign.setAttribute("responseUrl", request.getResponseUrl());
        esign.setAttribute("ekycIdType", "A");
        esign.setAttribute("ekycId", "");
        doc.appendChild(esign);

        // <Docs> section
        Element docs = doc.createElement("Docs");
        esign.appendChild(docs);

        Element inputHash = doc.createElement("InputHash");
        inputHash.setAttribute("id", "1");
        inputHash.setAttribute("hashAlgorithm", "SHA256");
        inputHash.setAttribute("docInfo", request.getDocInfo());
        inputHash.setTextContent(request.getDocumentHash());
        docs.appendChild(inputHash);

        return doc;
    }

    private void signDocument(Document doc) throws Exception {
        AadhaarESignProperties.Asp asp = eSignProperties.getAsp();

        // Load ASP keystore
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(asp.getKeystorePath())) {
            ks.load(fis, asp.getKeystorePassword().toCharArray());
        }

        PrivateKey privateKey = (PrivateKey) ks.getKey(
                asp.getKeyAlias(), asp.getKeystorePassword().toCharArray());
        X509Certificate cert = (X509Certificate) ks.getCertificate(asp.getKeyAlias());

        if (privateKey == null || cert == null) {
            throw new ESignException("ASP private key or certificate not found in keystore for alias: " + asp.getKeyAlias());
        }

        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

        // Digest and transforms
        DigestMethod digestMethod = fac.newDigestMethod(DigestMethod.SHA256, null);
        Transform envelopedTransform = fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
        Transform c14nTransform = fac.newTransform(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null);
        Reference reference = fac.newReference("", digestMethod,
                List.of(envelopedTransform, c14nTransform), null, null);

        // SignedInfo
        CanonicalizationMethod c14n = fac.newCanonicalizationMethod(
                CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null);
        SignatureMethod signatureMethod = fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null);
        SignedInfo signedInfo = fac.newSignedInfo(c14n, signatureMethod, List.of(reference));

        // KeyInfo with X.509 certificate
        KeyInfoFactory kif = fac.getKeyInfoFactory();
        List<Object> x509Content = new ArrayList<>();
        x509Content.add(cert);
        X509Data x509Data = kif.newX509Data(x509Content);
        KeyInfo keyInfo = kif.newKeyInfo(Collections.singletonList(x509Data));

        // Sign
        XMLSignature signature = fac.newXMLSignature(signedInfo, keyInfo);
        DOMSignContext signContext = new DOMSignContext(privateKey, doc.getDocumentElement());
        signature.sign(signContext);

        log.debug("eSign XML document signed successfully for txn: {}",
                doc.getDocumentElement().getAttribute("txn"));
    }

    private String documentToString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc), new StreamResult(baos));
        return baos.toString("UTF-8");
    }
}
