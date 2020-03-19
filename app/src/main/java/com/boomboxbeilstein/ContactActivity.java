package com.boomboxbeilstein;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.view.View.OnClickListener;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


public class ContactActivity extends AppCompatActivity implements OnClickListener {

    private Button buttonContact;

    private EditText textinput;
    private EditText name;

    private Spinner dropdownSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        //setSupportActionBar(toolbar);

        buttonContact = (Button) findViewById(R.id.contact);
        buttonContact.setOnClickListener(ContactActivity.this);

        textinput = (EditText) findViewById(R.id.input);
        name = (EditText) findViewById(R.id.name);


        Spinner dropdown = findViewById(R.id.spinner1);
        String[] items = new String[]{"Liedwunsch", "Nachricht", "Problem"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        dropdownSpinner = (Spinner) findViewById(R.id.spinner1);
        dropdownSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                Object item = adapterView.getItemAtPosition(position);
                if (item != null) {
                    //Placeholder
                }
                //Placeholder

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // TODO Auto-generated method stub

            }
        });





    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contact:
                mailStudio();
                break;



        }
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.back:
            finish();
            return true;


            default:
                // Wenn wir hier ankommen, wurde eine unbekannt Aktion erfasst.
                // Daher erfolgt der Aufruf der Super-Klasse, die sich darum kümmert.
                return super.onOptionsItemSelected(item);



        }
    }


        public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        return true;
    }



    public void mailStudio(){
        EditText textinput = (EditText) findViewById(R.id.input);
        EditText name = (EditText) findViewById(R.id.name);
        String text = dropdownSpinner.getSelectedItem().toString();
        String theName = name.getText().toString();
        String message = textinput.getText().toString();
        //Creating SendMail object
        SendMail sm = new SendMail(this, "info@boombox-beilstein.de", text+" von "+theName, message);

        //Executing sendmail to send email
        sm.execute();
        textinput.getText().clear();
    }



}

