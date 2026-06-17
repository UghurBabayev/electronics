package az.electronika.demo.service;

import az.electronika.demo.dto.InstallmentPaymentResponse;
import az.electronika.demo.dto.InstallmentPlanResponse;
import az.electronika.demo.dto.ReportResponse;
import az.electronika.demo.entity.enums.InstallmentStatus;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    public byte[] exportInstallments(List<InstallmentPlanResponse> plans) throws IOException {
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = headerStyle(wb);
            CellStyle overdueStyle = overdueStyle(wb);
            CellStyle numberStyle = numberStyle(wb);

            Sheet planSheet = wb.createSheet("Nisiyə Planları");
            String[] planHeaders = {"Müştəri", "Məhsul", "Ümumi məbləğ", "Aylıq ödəniş",
                    "Ödənilib", "Qalıq", "Status", "Başlanğıc", "Müddət (ay)"};
            writeRow(planSheet.createRow(0), planHeaders, headerStyle);

            Sheet paySheet = wb.createSheet("Ödəniş Cədvəli");
            String[] payHeaders = {"#", "Müştəri", "Məhsul", "Ödəniş tarixi", "Məbləğ", "Status", "Ödənildi tarixi"};
            writeRow(paySheet.createRow(0), payHeaders, headerStyle);

            int planRow = 1, payRow = 1;
            for (InstallmentPlanResponse plan : plans) {
                Row r = planSheet.createRow(planRow++);
                setCell(r, 0, plan.customerName() != null ? plan.customerName() : "");
                setCell(r, 1, plan.productName() != null ? plan.productName() : "");
                setNumCell(r, 2, plan.totalAmount().doubleValue(), numberStyle);
                setNumCell(r, 3, plan.monthlyPayment().doubleValue(), numberStyle);
                setNumCell(r, 4, plan.paidAmount().doubleValue(), numberStyle);
                setNumCell(r, 5, plan.remainingAmount().doubleValue(), numberStyle);
                setCell(r, 6, translateStatus(plan.status()));
                setCell(r, 7, plan.startDate().toString());
                setCell(r, 8, String.valueOf(plan.durationMonths()));

                for (InstallmentPaymentResponse pay : plan.payments()) {
                    boolean overdue = !pay.isPaid() && pay.dueDate().isBefore(LocalDate.now());
                    Row pr = paySheet.createRow(payRow++);
                    CellStyle rowStyle = overdue ? overdueStyle : null;
                    setCell(pr, 0, String.valueOf(payRow - 1), rowStyle);
                    setCell(pr, 1, plan.customerName() != null ? plan.customerName() : "", rowStyle);
                    setCell(pr, 2, plan.productName() != null ? plan.productName() : "", rowStyle);
                    setCell(pr, 3, pay.dueDate().toString(), rowStyle);
                    setNumCell(pr, 4, pay.amount().doubleValue(), overdue ? overdueStyle : numberStyle);
                    setCell(pr, 5, pay.isPaid() ? "Ödənilib" : (overdue ? "Gecikmiş" : "Gözlənilir"), rowStyle);
                    setCell(pr, 6, pay.paidDate() != null ? pay.paidDate().toString() : "", rowStyle);
                }
            }

            for (int i = 0; i < planHeaders.length; i++) planSheet.autoSizeColumn(i);
            for (int i = 0; i < payHeaders.length; i++) paySheet.autoSizeColumn(i);

            wb.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportReport(ReportResponse r) throws IOException {
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = headerStyle(wb);
            CellStyle numberStyle = numberStyle(wb);

            Sheet sheet = wb.createSheet("Hesabat");
            Object[][] rows = {
                    {"Tarix aralığı",   r.from() + " — " + r.to()},
                    {"Ümumi gəlir",     r.totalRevenue().doubleValue()},
                    {"Ümumi xərc",      r.totalCost().doubleValue()},
                    {"Mənfəət",         r.profit().doubleValue()},
                    {"Nisiyə borcu",    r.totalDebt().doubleValue()},
                    {"Anbar dəyəri",    r.inventoryValue().doubleValue()},
                    {"Başlanğıc məbləğ", r.initialBalance().doubleValue()},
                    {"Kassada nağd",    r.cashOnHand().doubleValue()},
            };

            for (int i = 0; i < rows.length; i++) {
                Row row = sheet.createRow(i);
                Cell label = row.createCell(0);
                label.setCellValue((String) rows[i][0]);
                label.setCellStyle(headerStyle);
                if (rows[i][1] instanceof Double d) {
                    setNumCell(row, 1, d, numberStyle);
                } else {
                    row.createCell(1).setCellValue((String) rows[i][1]);
                }
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            wb.write(out);
            return out.toByteArray();
        }
    }

    private CellStyle headerStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setBorderBottom(BorderStyle.THIN);
        return s;
    }

    private CellStyle overdueStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private CellStyle numberStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        DataFormat fmt = wb.createDataFormat();
        s.setDataFormat(fmt.getFormat("#,##0.00"));
        return s;
    }

    private void writeRow(Row row, String[] values, CellStyle style) {
        for (int i = 0; i < values.length; i++) {
            Cell c = row.createCell(i);
            c.setCellValue(values[i]);
            c.setCellStyle(style);
        }
    }

    private void setCell(Row row, int col, String value) {
        row.createCell(col).setCellValue(value);
    }

    private void setCell(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        if (style != null) c.setCellStyle(style);
    }

    private void setNumCell(Row row, int col, double value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        if (style != null) c.setCellStyle(style);
    }

    private String translateStatus(InstallmentStatus status) {
        return switch (status) {
            case ACTIVE -> "Aktiv";
            case COMPLETED -> "Tamamlandı";
            case OVERDUE -> "Gecikmiş";
        };
    }
}