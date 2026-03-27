package com.formgenerator.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

import java.io.File;
import java.util.List;

/**
 * Diagnostic utility: fills every text field with its own field name so you
 * can open the PDF and see which visual box corresponds to which field name.
 *
 * Usage (after mvn package):
 *   java -cp ... com.formgenerator.util.PdfDiagnosticFillUtil \
 *        src/main/resources/forms/BLACKROCK/fresh_purchase.pdf \
 *        /tmp/diagnostic_fill.pdf
 */
public class PdfDiagnosticFillUtil {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: PdfDiagnosticFillUtil <input-pdf> <output-pdf>");
            System.exit(1);
        }

        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);

        try (PDDocument doc = Loader.loadPDF(inputFile)) {
            var acroForm = doc.getDocumentCatalog().getAcroForm();
            if (acroForm == null) {
                System.out.println("No AcroForm found.");
                return;
            }
            fillFields(acroForm.getFields());
            doc.save(outputFile);
            System.out.println("Saved diagnostic PDF to: " + outputFile.getAbsolutePath());
        }
    }

    private static void fillFields(List<PDField> fields) {
        for (PDField field : fields) {
            try {
                if (field instanceof PDTextField tf) {
                    // Truncate to 12 chars so it fits in small boxes
                    String name = field.getFullyQualifiedName();
                    tf.setValue(name.length() > 12 ? name.substring(0, 12) : name);
                } else if (field instanceof PDCheckBox cb) {
                    cb.check();
                } else if (field instanceof PDNonTerminalField ntf) {
                    fillFields(ntf.getChildren());
                }
            } catch (Exception e) {
                System.err.println("Could not fill field: " + field.getFullyQualifiedName() + " — " + e.getMessage());
            }
        }
    }
}
