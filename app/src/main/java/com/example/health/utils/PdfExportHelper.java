package com.example.health.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.example.health.model.Appointment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfExportHelper {

    private static final String TAG = "PdfExportHelper";
    private static final int PAGE_WIDTH = 595; // A4 width in points
    private static final int PAGE_HEIGHT = 842; // A4 height in points
    private static final int MARGIN = 40;

    public static boolean exportAppointmentsToPdf(Context context, List<Appointment> appointments, String patientName) {
        PdfDocument document = new PdfDocument();

        try {
            // Create page info
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            // Setup paints
            Paint titlePaint = new Paint();
            titlePaint.setColor(Color.parseColor("#435E91"));
            titlePaint.setTextSize(24);
            titlePaint.setFakeBoldText(true);

            Paint headerPaint = new Paint();
            headerPaint.setColor(Color.parseColor("#333333"));
            headerPaint.setTextSize(14);
            headerPaint.setFakeBoldText(true);

            Paint textPaint = new Paint();
            textPaint.setColor(Color.parseColor("#666666"));
            textPaint.setTextSize(12);

            Paint linePaint = new Paint();
            linePaint.setColor(Color.parseColor("#CCCCCC"));
            linePaint.setStrokeWidth(1);

            Paint statusPendingPaint = new Paint();
            statusPendingPaint.setColor(Color.parseColor("#FF9800"));
            statusPendingPaint.setTextSize(11);

            Paint statusCompletedPaint = new Paint();
            statusCompletedPaint.setColor(Color.parseColor("#4CAF50"));
            statusCompletedPaint.setTextSize(11);

            Paint statusCancelledPaint = new Paint();
            statusCancelledPaint.setColor(Color.parseColor("#F44336"));
            statusCancelledPaint.setTextSize(11);

            int yPosition = MARGIN;

            // Draw title
            canvas.drawText("Historique des Rendez-vous", MARGIN, yPosition + 24, titlePaint);
            yPosition += 50;

            // Draw patient info
            canvas.drawText("Patient: " + patientName, MARGIN, yPosition, headerPaint);
            yPosition += 20;

            // Draw export date
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            canvas.drawText("Date d'export: " + sdf.format(new Date()), MARGIN, yPosition, textPaint);
            yPosition += 15;

            // Draw total count
            canvas.drawText("Total: " + appointments.size() + " rendez-vous", MARGIN, yPosition, textPaint);
            yPosition += 30;

            // Draw separator line
            canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, linePaint);
            yPosition += 20;

            // Draw appointments
            int pageNumber = 1;
            for (int i = 0; i < appointments.size(); i++) {
                Appointment apt = appointments.get(i);

                // Check if we need a new page
                if (yPosition > PAGE_HEIGHT - 100) {
                    document.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    yPosition = MARGIN;
                }

                // Draw appointment card
                canvas.drawText((i + 1) + ". " + apt.getDoctorName(), MARGIN, yPosition, headerPaint);
                yPosition += 18;

                canvas.drawText("    Specialite: " + apt.getSpecialty(), MARGIN, yPosition, textPaint);
                yPosition += 15;

                canvas.drawText("    Date: " + apt.getDate() + " a " + apt.getTime(), MARGIN, yPosition, textPaint);
                yPosition += 15;

                // Draw status with appropriate color
                String status = apt.getStatus();
                String statusText = getStatusText(status);
                Paint statusPaint = getStatusPaint(status, statusPendingPaint, statusCompletedPaint, statusCancelledPaint);
                canvas.drawText("    Statut: " + statusText, MARGIN, yPosition, statusPaint);
                yPosition += 15;

                if (apt.getReason() != null && !apt.getReason().isEmpty()) {
                    canvas.drawText("    Motif: " + apt.getReason(), MARGIN, yPosition, textPaint);
                    yPosition += 15;
                }

                canvas.drawText("    Tarif: " + String.format(Locale.getDefault(), "%.0f", apt.getConsultationFee()) + " EUR", MARGIN, yPosition, textPaint);
                yPosition += 20;

                // Draw separator
                canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, linePaint);
                yPosition += 15;
            }

            document.finishPage(page);

            // Save the document
            String fileName = "rendez_vous_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            fos.close();
            document.close();

            Log.d(TAG, "PDF exported to: " + file.getAbsolutePath());

            // Open the PDF file
            openPdfFile(context, file);

            return true;

        } catch (IOException e) {
            Log.e(TAG, "Error exporting PDF: " + e.getMessage());
            document.close();
            return false;
        }
    }

    private static String getStatusText(String status) {
        if (status == null) return "Inconnu";
        switch (status) {
            case "pending":
                return "En attente";
            case "confirmed":
                return "Confirme";
            case "completed":
                return "Termine";
            case "cancelled":
                return "Annule";
            default:
                return status;
        }
    }

    private static Paint getStatusPaint(String status, Paint pending, Paint completed, Paint cancelled) {
        if (status == null) return pending;
        switch (status) {
            case "completed":
                return completed;
            case "cancelled":
                return cancelled;
            default:
                return pending;
        }
    }

    private static void openPdfFile(Context context, File file) {
        try {
            Uri uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening PDF: " + e.getMessage());
        }
    }
}
