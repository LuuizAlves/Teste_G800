package com.example.testeg800;

import br.com.gertec.gedi.GEDI;
import br.com.gertec.gedi.enums.GEDI_PRNTR_e_Alignment;
import br.com.gertec.gedi.enums.GEDI_PRNTR_e_BarCodeType;
import br.com.gertec.gedi.enums.GEDI_PRNTR_e_Status;
import br.com.gertec.gedi.exceptions.GediException;
import br.com.gertec.gedi.impl.Gedi;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.fonts.Font;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.security.PrivateKey;
import java.util.ArrayList;

import br.com.gertec.gedi.GEDI;
import br.com.gertec.gedi.interfaces.IGEDI;
import br.com.gertec.gedi.interfaces.IPRNTR;
import br.com.gertec.gedi.structs.GEDI_PRNTR_st_BarCodeConfig;
import br.com.gertec.gedi.structs.GEDI_PRNTR_st_PictureConfig;
import br.com.gertec.gedi.structs.GEDI_PRNTR_st_StringConfig;
import me.dm7.barcodescanner.core.ViewFinderView;

import static android.hardware.Camera.Parameters.FLASH_MODE_ON;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //LIB GEDI DE CONFIGURAÇÃO DE IMAGENS
    private GEDI_PRNTR_st_PictureConfig pictureConfig;

    private IGEDI iGedi = null;
    private IPRNTR iPrint = null;

    private GEDI_PRNTR_e_Status status;
    private GEDI_PRNTR_st_StringConfig stringConfig;

    private static boolean isInitPrint = false; //Não deixa imprimir outra coisa


    private IntentIntegrator qrScan;
    private ArrayList<String> arrayListTipo;

    //VARIAVÉIS DA MENSAGEM DE TEXTO DECLARADA PELO USUÁRIO
    private String textMessage;
    private EditText textInput;

    //BOTÕES DE ACESSO AO USUÁRIO
    private Button buttonMessage;
    private Button buttonPrintText;
    private Button buttonPrintImage;
    private Button buttonPrintBarCode;
    private Button buttonReadBarCodeEAN8;
    private Button buttonReadBarCodeEAN13;
    private Button buttonReadBarCodeQRCODE;

    //RADIO BUTTONS SOBRE ALINHAMENTO DA MESSAGEM
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private String alignMessage;

    //CHECK BOX DA MENSAGEM DE ENTRADA
    private CheckBox checkNegrito;
    private CheckBox checkItalic;
    private CheckBox checkSublinhado;

    //SPINNERS SOBRE CONFIGS DO CÓDIGO DE BARRAS
    private Spinner spinnerFontType;
    private Spinner spinnerFontSize;

    //SPINNERS SOBRE CONFIGS DO CÓDIGO DE BARRAS
    private Spinner spinnerWidthBarCode;
    private Spinner spinnerHeightBarCode;
    private Spinner spinnerTypeBarCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //CONFIGS RADIO BUTTONS
        radioGroup = findViewById(R.id.groupRadios);

        //CONFIGS DOS SPINNERS SOBRE FONT TYPE E SIZE
        spinnerFontType = findViewById(R.id.spinnerFontText);
        spinnerFontType.setOnItemSelectedListener(this);
        spinnerFontSize = findViewById(R.id.spinnerFontSize);
        spinnerFontSize.setOnItemSelectedListener(this);

        //CONFIGS CHECKBOX BUTTONS
        checkItalic = findViewById(R.id.italico);
        checkNegrito = findViewById(R.id.negrito);
        checkSublinhado = findViewById(R.id.sublinhado);

        //CONFIGS PARA IMPRIMIR MENSAGEM DO USUÁRIO
        textInput = findViewById(R.id.inputText);
        buttonMessage = findViewById(R.id.buttonPrintMessage);
        buttonMessage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                textMessage = textInput.getText().toString();

                if(textMessage.length() == 0){
                    Toast.makeText(getApplicationContext(), "Digíte uma mensagem", Toast.LENGTH_SHORT).show();
                }else {
                    try {
                        //Toast.makeText(getApplicationContext(), textMessage, Toast.LENGTH_SHORT).show();

                        int idRadio = radioGroup.getCheckedRadioButtonId();
                        radioButton = findViewById(idRadio);
                        alignMessage = radioButton.getText().toString();
                        //Toast.makeText(getApplicationContext(), "ID RADIO BUTTON: " + alignMessage, Toast.LENGTH_SHORT).show();

                        printText(textMessage, alignMessage);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        buttonPrintImage = findViewById(R.id.buttonPrintImage);
        buttonPrintImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    imprimeImagem("gertec", 150, 320);
                } catch (GediException e) {
                    e.printStackTrace();
                }
            }
        });

        //CONFIGS ID SPINNERS BAR CODE
        spinnerWidthBarCode = findViewById(R.id.spinnerBarCodeWidth);
        spinnerWidthBarCode.setOnItemSelectedListener(this);
        spinnerHeightBarCode = findViewById(R.id.spinnerBarCodeHeight);
        spinnerHeightBarCode.setOnItemSelectedListener(this);
        spinnerTypeBarCode = findViewById(R.id.spinnerBarCodeType);
        spinnerTypeBarCode.setOnItemSelectedListener(this);

        //DECLARAÇÃO DO ID E ONCLICK DOS SPINNERS SOBRE O BAR CODE
        buttonPrintBarCode = findViewById(R.id.buttonBarCode);
        buttonPrintBarCode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int hp = Integer.valueOf(spinnerHeightBarCode.getSelectedItem().toString());
                int wp = Integer.valueOf(spinnerWidthBarCode.getSelectedItem().toString());
                String type = spinnerTypeBarCode.getSelectedItem().toString();
                String text = "12345";

                if (type.equals("QR_CODE")) text = "12345";
                if (type.equals("EAN_8")) text = "59012341";
                if (type.equals("EAN_13")) text = "5901234123457";

                try {
                    imprimeBarCode(text, hp, wp, type);
                } catch (GediException e) {
                    e.printStackTrace();
                }
            }
        });

        //DECLARAÇÃO DO ID E ONCLICK DOS SPINNERS SOBRE O BAR CODE DO TIPO EAN_8
        buttonReadBarCodeEAN8 = findViewById(R.id.buttonReadEAN8);
        buttonReadBarCodeEAN8.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startCamera("EAN_8");
            }
        });

        //DECLARAÇÃO DO ID E ONCLICK DOS SPINNERS SOBRE O BAR CODE DO TIPO EAN_13
        buttonReadBarCodeEAN13 = findViewById(R.id.buttonReadEAN13);
        buttonReadBarCodeEAN13.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startCamera("EAN_13");
            }
        });

        //DECLARAÇÃO DO ID E ONCLICK DOS SPINNERS SOBRE O BAR CODE DO TIPO QR_CODE
        buttonReadBarCodeQRCODE = findViewById(R.id.buttonReadQRCODE);
        buttonReadBarCodeQRCODE.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startCamera("QR_CODE");
            }
        });

        //DECLARAÇÃO DO ID E ONCLICK DO BUTTON DE PRINTAR TUDO
        buttonPrintText = findViewById(R.id.buttonPrint);
        buttonPrintText.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                try {
                    imprimeImagem("gertec", 150, 320);
                    printText("GERTEC DEVELOPER", "Centralizado");
                    imprimeBarCode("5901234123457", 150, 350, "EAN_13");
                    imprimeBarCode("59012341", 150, 250, "EAN_8");
                    imprimeBarCode("1234", 300, 300, "QR_CODE");
                    //startCamera();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //INICIALIZA TREAHD PARA RODAR GEDI
        new Thread(() -> {
            iGedi = new Gedi(this);
            this.iGedi = GEDI.getInstance(this);
            this.iPrint = this.iGedi.getPRNTR();
            try {
                new Thread().sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public boolean isImpressoraOK(){
        if( status.getValue() == 0 ){
            return true;
        }
        return false;
    }

    public void getStatusImpressora() throws GediException {
        try {
            ImpressoraInit();
            this.status = this.iPrint.Status();
        } catch ( GediException e) {
            throw new GediException(e.getErrorCode());
        }
    }

    public void ImpressoraInit() throws GediException{
        try{
            if(this.iPrint != null && !isInitPrint){
                isInitPrint = true;
                this.iPrint.Init();
            }
        }catch (GediException e){
            e.printStackTrace();
            throw new GediException(e.getErrorCode());
        }
    }

    @SuppressLint("WrongConstant")
    private boolean printText(String text, String align) throws Exception{
        try{
            this.getStatusImpressora();

            if(isImpressoraOK()){
                this.stringConfig = new GEDI_PRNTR_st_StringConfig(new Paint());

                int size = Integer.valueOf(spinnerFontSize.getSelectedItem().toString());
                this.stringConfig.paint.setTextSize(size);

                if(align.equals("Centralizado")) this.stringConfig.paint.setTextAlign(Paint.Align.CENTER);
                if(align.equals("Esquerda")) this.stringConfig.paint.setTextAlign(Paint.Align.LEFT);
                if(align.equals("Direita")) this.stringConfig.paint.setTextAlign(Paint.Align.RIGHT);

                if(checkNegrito.isChecked() && !checkItalic.isChecked()){
                    this.stringConfig.paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                }
                if(checkItalic.isChecked() && !checkNegrito.isChecked()){
                    this.stringConfig.paint.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                }
                if(checkItalic.isChecked() && checkNegrito.isChecked()){
                    this.stringConfig.paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
                }

                String type = spinnerFontType.getSelectedItem().toString();
                if(type.equals("DEFAULT")) this.stringConfig.paint.setTypeface(Typeface.DEFAULT);
                if(type.equals("MONOSPACE")) this.stringConfig.paint.setTypeface(Typeface.MONOSPACE);
                if(type.equals("SANS_SERIF")) this.stringConfig.paint.setTypeface(Typeface.SANS_SERIF);
                if(type.equals("SERIF")) this.stringConfig.paint.setTypeface(Typeface.SERIF);

                this.iPrint.DrawBlankLine(200);
                this.iPrint.DrawStringExt(this.stringConfig, text);
                ImpressoraOutput();
                return true;
            }
        }catch (GediException e){
            throw new GediException(e.getErrorCode());
        }

        return false;
    }

    public void ImpressoraOutput() throws GediException {
        try {
            if( this.iPrint != null  ){
                this.iPrint.Output();
                isInitPrint = false;
            }
        } catch (GediException e) {
            e.printStackTrace();
            throw new GediException(e.getErrorCode());
        }
    }

    public boolean imprimeBarCode( String texto, int height, int width,  String barCodeType ) throws GediException {
        try {

            GEDI_PRNTR_st_BarCodeConfig barCodeConfig = new GEDI_PRNTR_st_BarCodeConfig();
            //Bar Code Type
            barCodeConfig.barCodeType = GEDI_PRNTR_e_BarCodeType.valueOf(barCodeType);

            //Height
            barCodeConfig.height = height;
            //Width
            barCodeConfig.width = width;

            ImpressoraInit();
            this.iPrint.DrawBarCode(barCodeConfig,texto);
            this.iPrint.DrawBlankLine(100);
            this.ImpressoraOutput();
            return true;
        }catch (IllegalArgumentException e){
            throw new IllegalArgumentException(e);
        } catch (GediException e) {
            throw new GediException(e.getErrorCode());
        }

    }

    public boolean imprimeImagem(String imagem, int height, int width) throws GediException {

        int id = 0;
        Bitmap bmp;
        try {

            pictureConfig = new GEDI_PRNTR_st_PictureConfig();

            //Align
            pictureConfig.alignment = GEDI_PRNTR_e_Alignment.valueOf("CENTER");

            //Height
            pictureConfig.height = height;
            //Width
            pictureConfig.width = width;


            id = this.getApplicationContext().getResources().getIdentifier(
                    imagem, "drawable",
                    this.getApplicationContext().getPackageName());
            bmp = BitmapFactory.decodeResource(this.getApplicationContext().getResources(), id);


            ImpressoraInit();
            this.iPrint.DrawPictureExt(pictureConfig, bmp);
            this.iPrint.DrawBlankLine(30);

            this.ImpressoraOutput();

            return true;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        } catch (GediException e) {
            throw new GediException(e.getErrorCode());
        }

    }

    private void startCamera( String type){
        this.arrayListTipo = new ArrayList<String>();
        arrayListTipo.add(type);

        qrScan = new IntentIntegrator(this);
        qrScan.setPrompt("Digitalizar o código ");
        qrScan.setBeepEnabled(true);
        qrScan.setBarcodeImageEnabled(true);
        qrScan.setTimeout(30000); // 30 * 1000 => 3 minuto
        qrScan.addExtra("FLASH_MODE_ON", FLASH_MODE_ON);
        qrScan.initiateScan(this.arrayListTipo); //LISTA DE TIPOS DE CÓDIGOS DE BARRA
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (intentResult != null) {
            //if qrcode has nothing in it
            if (intentResult.getContents() == null) {
                //System.out.println("Não encontrado QR CODE!");
                Toast.makeText(getApplicationContext(), "Não encontrado QR CODE!", Toast.LENGTH_SHORT).show();
            } else {
                //if qr contains data
                try {
                    System.out.println(intentResult.getContents());
                    String result = intentResult.getContents().toString();
                    printText(result, "Centralizado");
                    this.iPrint.DrawBlankLine(20);

                    //Toast.makeText(getApplicationContext(), "RESULT: " + result, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            System.out.println(": Não foi possível fazer a leitura.\n");
            Toast.makeText(getApplicationContext(), "Não foi possível fazer a leitura!", Toast.LENGTH_SHORT).show();
        }
    }

    //FUNÇÕES QUE FORAM AUTOMATICAMENTES IMPLEMENTADAS PARA UTILIZAÇÃO DOS SPINNERS
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        //Toast.makeText(this, adapterView.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}