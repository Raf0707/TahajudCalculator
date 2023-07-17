package raf.tabiin.tahajudcalculator.ui.calculator;

import static android.accessibilityservice.AccessibilityService.*;
import static android.content.Context.MODE_PRIVATE;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import raf.tabiin.tahajudcalculator.R;
import raf.tabiin.tahajudcalculator.databinding.FragmentCalculatorBinding;
import raf.tabiin.tahajudcalculator.util.Timer;

public class CalculatorFragment extends Fragment {
    FragmentCalculatorBinding b;
    private SharedPreferences sPreff;

    private Map<String, String> lastValues = new HashMap<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        b = FragmentCalculatorBinding.inflate(getLayoutInflater());

        b.dukhaBtn.setOnClickListener(v -> {
            try {
                calculateDukha(v);
                b.hoursFajr.setCursorVisible(false);
                b.minuteFajr.setCursorVisible(false);
            } catch (NumberFormatException e) {
                //validateInputText(b.hoursShuruk, b.minuteShuruk);
                b.hoursShuruk.setText(b.hoursShuruk.getText().toString().replaceAll("[\\.\\-,\\s]+", ""));
                b.minuteShuruk.setText(b.minuteShuruk.getText().toString().replaceAll("[\\.\\-,\\s]+", ""));
                Snackbar.make(v, "Заполните все поля", Snackbar.LENGTH_SHORT)
                        .setAction("А что не так?", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onMaterialAlert();
                            }
                        })
                        .show();
                e.printStackTrace();
            }

            //saveText();
        });

        b.tahajudBtn.setOnClickListener(v -> {
            try {
                calculateTahajud(v);
                b.hoursFajr.setCursorVisible(false);
                b.minuteFajr.setCursorVisible(false);
                b.hoursIsha.setCursorVisible(false);
                b.minuteIsha.setCursorVisible(false);
            } catch (NumberFormatException e){
                //validateInputText(b.hoursIsha, b.minuteIsha);
                //validateInputText(b.hoursFajr, b.minuteFajr);
                b.hoursIsha.setText(b.hoursIsha.getText().toString().replaceAll("[\\.\\-,\\s]+", ""));
                b.minuteIsha.setText(b.minuteIsha.getText().toString().replaceAll("[\\.\\-,\\s]+", ""));
                b.hoursFajr.setText(b.hoursFajr.getText().toString().replaceAll("[\\.\\-,\\s]+", ""));
                b.minuteFajr.setText(b.minuteFajr.getText().toString().replaceAll("[\\.\\-,\\s]+", ""));
                Snackbar.make(v, "Заполните все поля", Snackbar.LENGTH_SHORT)
                        .setAction("А что не так?", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onMaterialAlert();
                            }
                        })
                        .show();
                e.printStackTrace();
            }

            //saveText();
        });

        b.copyAllBtn.setOnClickListener(v -> {
            copyAll();
        });

        b.copyTextBtn.setOnClickListener(v -> {
            copyText();
        });

        b.resetBtn.setOnClickListener(v -> {
            resetDialog();
        });

        b.sharedAllBtn.setOnClickListener(v -> {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Время намазов Духа и Тахаджуд" + "\n" + copyAllTxt();
            String shareSub = copyAllTxt();
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share using"));
        });

        b.infotimerBtn.setOnClickListener(v -> {
            b.hoursShuruk.setText(lastValues.get("sh"));
            b.minuteShuruk.setText(lastValues.get("sm"));
            b.hoursIsha.setText(lastValues.get("ih"));
            b.minuteIsha.setText(lastValues.get("im"));
            b.hoursFajr.setText(lastValues.get("fh"));
            b.minuteFajr.setText(lastValues.get("fm"));

            b.dukhaTimeOutput.setText(lastValues.get("dukha"));
            b.tahajudTimeOutput.setText(lastValues.get("tahajud"));
        });

        b.hoursFajr.setOnClickListener(v -> { b.hoursFajr.setCursorVisible(true); });
        b.minuteFajr.setOnClickListener(v -> { b.minuteFajr.setCursorVisible(true); });
        b.hoursIsha.setOnClickListener(v -> { b.hoursIsha.setCursorVisible(true); });
        b.minuteIsha.setOnClickListener(v -> { b.minuteIsha.setCursorVisible(true); });
        b.hoursShuruk.setOnClickListener(v -> { b.hoursShuruk.setCursorVisible(true); });
        b.minuteShuruk.setOnClickListener(v -> { b.minuteShuruk.setCursorVisible(true); });


        return b.getRoot();
    }

    public void pass() {}

    public void calculateDukha(View v) {
        validateInputText(b.hoursShuruk, b.minuteShuruk, v);
        b.dukhaTimeOutput.setText(convertTime(getTime(b.hoursShuruk, b.minuteShuruk) + 45));
    }

    public void calculateTahajud(View v) {
        validateInputText(b.hoursIsha, b.minuteIsha, v);
        validateInputText(b.hoursFajr, b.minuteFajr, v);
        int totalIsha = Math.abs(getTime(b.hoursIsha, b.minuteIsha) - 24 * 60);
        int totalFajr = getTime(b.hoursFajr, b.minuteFajr);
        int res = (Math.abs(totalIsha + totalFajr) / 3);
        b.tahajudTimeOutput.setText(convertTime(totalFajr - res));
    }

    public int getTime(EditText hrs, EditText mins) {
        int hours = Integer.parseInt(hrs.getText().toString());
        int minutes = Integer.parseInt(mins.getText().toString());;
        int total = hours * 60 + minutes;
        return total;
    }

    public String convertTime(int time) {
        String h = String.valueOf(((time / 60) % 24));
        String m = String.valueOf((time % 60));
        if (m.length() == 1) {
            m = "0" + m;
        }
        String res = h + ":" + m;
        if (((time / 60) % 24) < 10) {
            res = "0" + res;
        }

        return res;

    }

    public void validateInputText(EditText hrs, EditText mins, View v) {
        if ((Integer.parseInt(hrs.getText().toString()) < 0)
            || (Integer.parseInt(hrs.getText().toString()) > 24)) {
            Snackbar.make(v, "Введите часы от 0 до 24", Snackbar.LENGTH_LONG).show();
        }

        if ((Integer.parseInt(mins.getText().toString()) < 0)
                || (Integer.parseInt(mins.getText().toString()) > 60)) {
            Snackbar.make(v, "Введите минуты от 0 до 60", Snackbar.LENGTH_LONG).show();
        }

        if (hrs.getText().toString().length() > 2) {
            Snackbar.make(v, "Введите время в формате чч:мм, часы от 0 до 24, минуты от 0 до 60", Snackbar.LENGTH_LONG).show();
            hrs.getText().clear();
        }

        if (mins.getText().toString().length() > 2) {
            Snackbar.make(v, "Введите время в формате чч:мм, часы от 0 до 24, минуты от 0 до 60", Snackbar.LENGTH_LONG).show();
            mins.getText().clear();
        }
    }

    public void onMaterialAlert() {
        MaterialAlertDialogBuilder alert =
                new MaterialAlertDialogBuilder(getContext());

        View dialogView = getLayoutInflater()
                .inflate(R.layout.create_pamyatka_dialog, null);

        alert.setTitle("А что не так? Хмм... хороший вопрос)");
        alert.setMessage(R.string.pamyatka);
        alert.setCancelable(true);

        alert.setNegativeButton("не понял", (dialogInterface, i) -> {
            Snackbar.make(b.getRoot(), "Прочти памятку еще раз", Snackbar.LENGTH_LONG)
                    .setAction("ну ок, давай еще раз", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onMaterialAlert();
                        }
                    })
                    .show();
        });


        alert.setPositiveButton("Понял", (dialogInterface, i) -> {
            Snackbar.make(b.getRoot(), "Молодец", Snackbar.LENGTH_LONG)
                    .setAction("Спасибо", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(getContext(), "Да не за что, наслаждайся", Toast.LENGTH_LONG).show();
                        }
                    })
                    .show();
        });

        alert.setView(dialogView);
        alert.show();
    }


    private void saveText() {
        sPreff = requireActivity().getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPreff.edit();
        ed.clear();

        ed.putString("sH", b.hoursShuruk.getText().toString());
        ed.putString("sM", b.minuteShuruk.getText().toString());

        ed.putString("iH", b.hoursIsha.getText().toString());
        ed.putString("iM", b.minuteIsha.getText().toString());

        ed.putString("fH", b.hoursFajr.getText().toString());
        ed.putString("fM", b.minuteFajr.getText().toString());

        ed.putString("dukha", b.dukhaTimeOutput.getText().toString());
        ed.putString("tahajud", b.tahajudTimeOutput.getText().toString());

        ed.apply();
    }

    public void loadText() {
        String sH = sPreff.getString("sH", b.hoursShuruk.getText().toString());
        String sM = sPreff.getString("sM", b.minuteShuruk.getText().toString());

        String iH = sPreff.getString("iH", b.hoursIsha.getText().toString());
        String iM = sPreff.getString("iM", b.minuteIsha.getText().toString());

        String fH = sPreff.getString("fH", b.hoursFajr.getText().toString());
        String fM = sPreff.getString("fM", b.minuteFajr.getText().toString());

        String dukha = sPreff.getString("sH", b.dukhaTimeOutput.getText().toString());
        String tahajud = sPreff.getString("sH", b.tahajudTimeOutput.getText().toString());

        b.hoursShuruk.setText(sH);
        b.minuteShuruk.setText(sM);

        b.hoursIsha.setText(iH);
        b.minuteIsha.setText(iM);

        b.hoursFajr.setText(fH);
        b.minuteFajr.setText(fM);

        b.dukhaTimeOutput.setText(dukha);
        b.tahajudTimeOutput.setText(tahajud);
    }

    public void screenShot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            String mPath = Environment.getExternalStorageState().toString() + "/" + now + ".jpg";
            View v1 = requireActivity().getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
            int q = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, q, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            MediaStore.Images.Media.insertImage(requireContext().getContentResolver(), imageFile.getAbsolutePath(), imageFile.getName(), imageFile.getName());
            //openScreen(imageFile);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void openScreen(File file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

    private void takeScreen() {
        View v = getActivity().getWindow().getDecorView().getRootView();
        v.setDrawingCacheEnabled(true);
        Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);

        try {
            File file = new File(Environment.getExternalStorageDirectory().toString(), "SCREEN" + System.currentTimeMillis() + ".png");
            FileOutputStream f = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.PNG, 100, f);
            f.flush();
            f.close();
            MediaStore.Images.Media.insertImage(requireContext().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveScr(View v) {
        String iPath = null;
        Bitmap bm = screen(v);
    }

    private Bitmap screen(View v) {
        if (v != null) {
            Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            v.draw(c);
            return b;
        }
        return null;
    }

    public void copyText() {
        Date date = new Date();
        String c = (String) android.text.format.DateFormat.format("Дата: dd.MM.yyyy, Время: hh:mm:ss", date);
        ClipboardManager clipboard = (ClipboardManager)
                b.getRoot().getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("",
                c + "\n" +
                        "Духа " + b.dukhaTimeOutput.getText().toString() + "\n" +
                "Тахаджуд " + b.tahajudTimeOutput.getText().toString());
        clipboard.setPrimaryClip(clip);

        Snackbar.make(b.getRoot()," скопировано в буфер обмена",
                Snackbar.LENGTH_SHORT).show();
    }

    public void copyAll() {
        Date date = new Date();
        String c = (String) android.text.format.DateFormat.format("Дата: dd.MM.yyyy, Время: hh:mm:ss", date);
        ClipboardManager clipboard = (ClipboardManager)
                b.getRoot().getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("",
                c + "\n" +
                        "Время восхода " + b.hoursShuruk.getText().toString() + ":" + b.minuteShuruk.getText().toString() + "\n" +
                        "Утренняя молитва Духа " + b.dukhaTimeOutput.getText().toString() + "\n" +
                        "Время намаза 'Иша " + b.hoursIsha.getText().toString() + ":" + b.minuteIsha.getText().toString() + "\n" +
                        "Время намаза Фаджр " + b.hoursFajr.getText().toString() + ":" + b.minuteFajr.getText().toString() + "\n" +
                        "Тахаджуд " + b.tahajudTimeOutput.getText().toString());
        clipboard.setPrimaryClip(clip);

        Snackbar.make(b.getRoot()," скопировано в буфер обмена",
                Snackbar.LENGTH_SHORT).show();
    }

    public String copyAllTxt() {
        Date date = new Date();
        String c = (String) android.text.format.DateFormat.format("Дата: dd.MM.yyyy, Время: hh:mm:ss", date);

        String res =
                c + "\n" +
                        "Время восхода " + b.hoursShuruk.getText().toString() + ":" + b.minuteShuruk.getText().toString() + "\n" +
                        "Утренняя молитва Духа " + b.dukhaTimeOutput.getText().toString() + "\n" +
                        "Время намаза 'Иша " + b.hoursIsha.getText().toString() + ":" + b.minuteIsha.getText().toString() + "\n" +
                        "Время намаза Фаджр " + b.hoursFajr.getText().toString() + ":" + b.minuteFajr.getText().toString() + "\n" +
                        "Тахаджуд " + b.tahajudTimeOutput.getText().toString();


        return res;
    }

    public void resetDialog() {
        MaterialAlertDialogBuilder alert =
                new MaterialAlertDialogBuilder(requireContext());

        alert.setTitle("Reset");
        alert.setMessage("Очистить данные?");
        alert.setCancelable(true);

        alert.setNegativeButton("отмена", (dialogInterface, i) -> {

        });

        alert.setPositiveButton("ок", (dialogInterface, i) -> {

            lastValues.put("sh", b.hoursShuruk.getText().toString());
            lastValues.put("sm", b.minuteShuruk.getText().toString());
            lastValues.put("ih", b.hoursIsha.getText().toString());
            lastValues.put("im", b.minuteIsha.getText().toString());
            lastValues.put("fh", b.hoursFajr.getText().toString());
            lastValues.put("fm", b.minuteFajr.getText().toString());

            lastValues.put("dukha", b.dukhaTimeOutput.getText().toString());
            lastValues.put("tahajud", b.tahajudTimeOutput.getText().toString());

            b.hoursFajr.getText().clear();
            b.minuteFajr.getText().clear();
            b.hoursIsha.getText().clear();
            b.minuteIsha.getText().clear();
            b.hoursShuruk.getText().clear();
            b.minuteShuruk.getText().clear();

            b.tahajudTimeOutput.setText("");
            b.dukhaTimeOutput.setText("");

            b.hoursFajr.setCursorVisible(true);
            b.minuteFajr.setCursorVisible(true);
            b.hoursIsha.setCursorVisible(true);
            b.minuteIsha.setCursorVisible(true);
            b.hoursShuruk.setCursorVisible(true);
            b.minuteShuruk.setCursorVisible(true);
        });

        alert.show();
    }

}