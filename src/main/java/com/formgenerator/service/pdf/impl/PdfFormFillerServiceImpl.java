package com.formgenerator.service.pdf.impl;

import com.formgenerator.domain.model.BankDetails;
import com.formgenerator.domain.model.InvestorDetails;
import com.formgenerator.domain.model.NomineeDetails;
import com.formgenerator.exception.PdfFormFillingException;
import com.formgenerator.mapping.AmcFieldMapping;
import com.formgenerator.mapping.FieldDefinition;
import com.formgenerator.mapping.FieldType;
import com.formgenerator.mapping.FormatterType;
import com.formgenerator.service.pdf.PdfFormFillerService;
import com.formgenerator.util.DateFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PdfFormFillerServiceImpl implements PdfFormFillerService {

    @Override
    public byte[] fillForm(byte[] pdfTemplateBytes, InvestorDetails investor, AmcFieldMapping mapping) {
        try (PDDocument doc = Loader.loadPDF(pdfTemplateBytes)) {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();

            if (acroForm == null) {
                throw new PdfFormFillingException("PDF has no AcroForm", null);
            }

            for (Map.Entry<String, FieldDefinition> entry : mapping.getFieldMappings().entrySet()) {
                FieldDefinition fieldDef = entry.getValue();
                try {
                    Object rawValue = resolveValue(investor, fieldDef.getInvestorFieldPath());
                    if (rawValue == null) continue;

                    PDField pdfField = acroForm.getField(fieldDef.getPdfFieldName());
                    if (pdfField == null) {
                        log.warn("PDF field not found: [{}] for mapping key [{}]",
                                fieldDef.getPdfFieldName(), entry.getKey());
                        continue;
                    }

                    writeField(pdfField, rawValue, fieldDef);

                } catch (Exception e) {
                    log.warn("Failed to fill field [{}]: {}", entry.getKey(), e.getMessage());
                }
            }

            // Signal viewers to regenerate checkbox/field appearances
            acroForm.setNeedAppearances(true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();

        } catch (PdfFormFillingException e) {
            throw e;
        } catch (IOException e) {
            throw new PdfFormFillingException("Failed to load or save PDF: " + e.getMessage(), e);
        }
    }

    // ── Field writing ─────────────────────────────────────────────────────────

    private void writeField(PDField field, Object rawValue, FieldDefinition def) throws IOException {
        String strValue = applyFormatter(rawValue, def);

        switch (def.getFieldType()) {
            case TEXT, AMOUNT -> {
                if (field instanceof PDTextField tf) {
                    tf.setValue(strValue);
                }
            }
            case CHECKBOX -> {
                if (field instanceof PDCheckBox cb) {
                    boolean shouldCheck = isTrue(rawValue, def.getCheckboxTrueValue());
                    if (shouldCheck) {
                        cb.check();
                    } else {
                        cb.unCheck();
                    }
                }
            }
            case RADIO_GROUP -> {
                if (field instanceof PDRadioButton rb) {
                    String exportValue = def.getRadioExportValue() != null
                            ? def.getRadioExportValue() : strValue;
                    rb.setValue(exportValue);
                }
            }
            case DATE_PART -> {
                if (field instanceof PDTextField tf && rawValue instanceof LocalDate date) {
                    tf.setValue(formatDatePart(date, def.getDatePartRole()));
                }
            }
        }
    }

    private String applyFormatter(Object value, FieldDefinition def) {
        if (value == null) return "";
        String str = value.toString();
        if (def.getFormatter() == null) return str;

        return switch (def.getFormatter()) {
            case UPPER_CASE -> str.toUpperCase();
            case TWO_DIGIT_PAD -> {
                try { yield DateFormatUtil.twoDigitPad(Integer.parseInt(str)); }
                catch (NumberFormatException e) { yield str; }
            }
            case AMOUNT_IN_FIGURES -> {
                if (value instanceof BigDecimal bd) yield bd.toPlainString();
                yield str;
            }
            case DATE_DD_MM_YYYY -> {
                if (value instanceof LocalDate d) yield DateFormatUtil.formatDdMmYyyy(d);
                yield str;
            }
            case TITLE_CASE -> toTitleCase(str);
            default -> str;
        };
    }

    private boolean isTrue(Object value, String checkboxTrueValue) {
        if (value instanceof Boolean b) return b;
        if (value instanceof String s) {
            return "true".equalsIgnoreCase(s) || s.equalsIgnoreCase(checkboxTrueValue);
        }
        if (value instanceof Enum<?> e) {
            return e.name().equalsIgnoreCase(checkboxTrueValue);
        }
        return false;
    }

    private String formatDatePart(LocalDate date, String role) {
        if (role == null) return "";
        return switch (role.toUpperCase()) {
            case "DAY"   -> DateFormatUtil.twoDigitPad(date.getDayOfMonth());
            case "MONTH" -> DateFormatUtil.twoDigitPad(date.getMonthValue());
            case "YEAR"  -> DateFormatUtil.fourDigit(date.getYear());
            default -> "";
        };
    }

    private String toTitleCase(String input) {
        if (input == null || input.isBlank()) return input;
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = true;
        for (char c : input.toCharArray()) {
            sb.append(nextUpper ? Character.toUpperCase(c) : Character.toLowerCase(c));
            nextUpper = Character.isWhitespace(c);
        }
        return sb.toString();
    }

    // ── Reflection-based value resolver ──────────────────────────────────────

    /**
     * Resolves a dot-notation path on InvestorDetails.
     * Supports:
     *   - Simple: "firstName"
     *   - Nested: "bankDetails.accountNumber"
     *   - List index: "nominees[0].nomineeName"
     */
    Object resolveValue(InvestorDetails investor, String path) {
        if (path == null || path.isBlank()) return null;
        try {
            Object current = investor;
            String[] parts = path.split("\\.");
            for (String part : parts) {
                if (current == null) return null;
                // Handle list indexing: nominees[0]
                if (part.contains("[")) {
                    int bracketIdx = part.indexOf('[');
                    String fieldName = part.substring(0, bracketIdx);
                    int index = Integer.parseInt(part.substring(bracketIdx + 1, part.indexOf(']')));
                    Field f = findField(current.getClass(), fieldName);
                    f.setAccessible(true);
                    Object listObj = f.get(current);
                    if (listObj instanceof List<?> list) {
                        if (index >= list.size()) return null;
                        current = list.get(index);
                    } else {
                        return null;
                    }
                } else {
                    Field f = findField(current.getClass(), part);
                    f.setAccessible(true);
                    current = f.get(current);
                }
            }
            return current;
        } catch (Exception e) {
            log.trace("Could not resolve path [{}]: {}", path, e.getMessage());
            return null;
        }
    }

    private Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field not found: " + name + " in " + clazz.getName());
    }
}
