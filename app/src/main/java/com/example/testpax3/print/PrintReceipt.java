package com.example.testpax3.print;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.example.testpax3.PrintDemo;
import com.example.testpax3.R;
import com.pax.gl.page.IPage;
import com.pax.gl.page.PaxGLPage;

import org.json.JSONArray;
import org.json.JSONException;

public class PrintReceipt {
    private Bitmap bitmap;
    private Handler handler = new Handler();
    private Context context;
    private JSONArray multiInvoices;
    private Bitmap logoBMp;
    private int index;

    public PrintReceipt(Context context) {
        this.context = context;
    }

    public void printReceipt(String invoiceData) {
        logoBMp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        if (invoiceData == null || invoiceData.isEmpty()) {
            return;
        }

        try {
            Log.i("Data", invoiceData);
            multiInvoices = new JSONArray(invoiceData);
            index = 0;
            generateInvoice();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void generateInvoice() {
        if (index >= multiInvoices.length()) {
            return;
        }
        PaxGLPage paxGLPage = PaxGLPage.getInstance(context);
        IPage page = paxGLPage.createPage();
        page.adjustLineSpace(-9);


        page.addLine().addUnit(logoBMp, IPage.EAlign.CENTER);
        page.addLine().addUnit("\n", 7);
        try {

            JSONArray invoice = multiInvoices.getJSONArray(index);
            Log.i("Data", invoice.toString());
            for (int j = 0; j < invoice.length(); j++) {
                Log.i("Data", invoice.optJSONObject(j).toString());
                String text = invoice.optJSONObject(j).getString("text");
                IPage.EAlign align;
                switch (invoice.optJSONObject(j).optString("dir")) {
                    case "right":
                        align = IPage.EAlign.RIGHT;
                        break;
                    case "left":
                        align = IPage.EAlign.LEFT;
                        break;
                    default:
                        align = IPage.EAlign.CENTER;

                }
                int textSize = Integer.parseInt(invoice.optJSONObject(j).getString("size"));
                int style;
                switch (invoice.optJSONObject(j).optString("style")) {
                    case "bold":
                        style = IPage.ILine.IUnit.TEXT_STYLE_BOLD;
                        break;
                    case "italic":
                        style = IPage.ILine.IUnit.TEXT_STYLE_ITALIC;
                        break;
                    case "underline":
                        style = IPage.ILine.IUnit.TEXT_STYLE_UNDERLINE;
                        break;
                    default:
                        style = IPage.ILine.IUnit.TEXT_STYLE_NORMAL;

                }
                page.addLine().addUnit(text, textSize, align, style);
                page.addLine().addUnit("\n", 5);

            }
            page.addLine().addUnit("\n", 30);

            bitmap = paxGLPage.pageToBitmap(page, 384);
            printReceipt();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void printReceipt() {
        Log.i("printReceipt", Build.MANUFACTURER.toUpperCase()+'\n'+
                        Build.BRAND.toUpperCase()+'\n'+
                        Build.DEVICE.toUpperCase()+'\n'+
                        Build.MODEL.toUpperCase()+'\n'+
                        Build.PRODUCT.toUpperCase()+'\n'
                );

        if ("PAX".equalsIgnoreCase(Build.BRAND) || "PAX".equalsIgnoreCase(Build.MANUFACTURER)) { //case of pax device
            new Thread(() -> {
                PaxPrinter.getInstance().init();
                PaxPrinter.getInstance().printBitmap(bitmap);

                onShowMessage(PaxPrinter.getInstance().start());

            }).start();

        } else { //for other devices use bluetooth print
            Log.i("printReceipt", "not pax\n");
        }
    }

    private void onShowMessage(final String message) {
        handler.post(() -> {
            if (!message.equals("Success")) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setTitle(PrintDemo.getInstance().getString(R.string.app_name));
                alertDialog.setMessage(message);
                alertDialog.setPositiveButton(PrintDemo.getInstance().getResources().getString(R.string.app_name),
                        (dialog, which) -> {
                            printReceipt();
                            dialog.cancel();
                        });
                alertDialog.setNegativeButton(PrintDemo.getInstance().getResources().getString(R.string.app_name),
                        (dialog, which) -> {
                            dialog.cancel();
                            index++;
                            generateInvoice();
                        });
                alertDialog.show();
            } else {
                index++;
                generateInvoice();
            }

        });

    }
}
