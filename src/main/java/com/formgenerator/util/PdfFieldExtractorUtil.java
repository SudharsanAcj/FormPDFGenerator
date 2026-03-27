package com.formgenerator.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;

import java.io.File;
import java.util.List;

/**
 * Standalone utility to extract and print all AcroForm field names from a PDF.
 *
 * Usage:
 *   mvn exec:java -Dexec.mainClass=com.formgenerator.util.PdfFieldExtractorUtil \
 *                 -Dexec.args="\"PDF sample/Form__DSP_BlackRock_FTP_-_Series_13_-_15M_form_Filler.pdf\""
 *
 * Or after building the jar:
 *   java -cp target/form-pdf-generator-1.0.0.jar \
 *        com.formgenerator.util.PdfFieldExtractorUtil \
 *        "PDF sample/Form__DSP_BlackRock_FTP_-_Series_13_-_15M_form_Filler.pdf"
 */
public class PdfFieldExtractorUtil {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: PdfFieldExtractorUtil <path-to-pdf>");
            System.exit(1);
        }

        File pdfFile = new File(args[0]);
        if (!pdfFile.exists()) {
            System.err.println("File not found: " + pdfFile.getAbsolutePath());
            System.exit(1);
        }

        try (PDDocument doc = Loader.loadPDF(pdfFile)) {
            var acroForm = doc.getDocumentCatalog().getAcroForm();
            if (acroForm == null) {
                System.out.println("This PDF has no AcroForm (no fillable fields).");
                return;
            }

            List<PDField> fields = acroForm.getFields();
            System.out.printf("%-55s %-20s %-15s %s%n", "FIELD NAME", "TYPE", "ON_VALUE", "CURRENT_VALUE");
            System.out.println("-".repeat(120));

            printFields(fields, "");
        }
    }

    private static void printFields(List<PDField> fields, String indent) {
        for (PDField field : fields) {
            String type = field.getClass().getSimpleName();
            String value = field.getValueAsString();
            String onValue = (field instanceof PDCheckBox cb) ? cb.getOnValue() : "-";
            System.out.printf("%-55s %-20s %-15s %s%n",
                    indent + field.getFullyQualifiedName(), type, onValue, value);

            if (field instanceof PDNonTerminalField ntf) {
                printFields(ntf.getChildren(), indent + "  ");
            }
        }
    }
}
