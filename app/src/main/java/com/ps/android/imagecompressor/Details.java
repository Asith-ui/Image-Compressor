package com.ps.android.imagecompressor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class Details extends AppCompatActivity {
    CardView fbPage,ytPage,ewris,examP,privacy,terms,share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        findViews();

        fbPage.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            gotoUrl("https://www.facebook.com/pixelfactory.lk");
        }
    });
        ytPage.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            gotoUrl("https://www.youtube.com/channel/UC3YDX0CvhubyUsnMUPd56sw");
        }
    });
        ewris.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            gotoUrl("https://play.google.com/store/apps/details?id=com.pixelstudio.android.cocofactory");
        }
    });
        examP.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            gotoUrl("https://play.google.com/store/apps/details?id=com.pixelstudio.android.updatecheck");
        }
    });
        privacy.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i2 = new Intent(Details.this, privacy.class);
            String policy = ("https://sites.google.com/view/privacy-policy-i-c/home");
            i2.putExtra("policy", policy);
            startActivity(i2);
        }
    });
        terms.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i3 = new Intent(Details.this,privacy.class);
            String terms = ("https://sites.google.com/view/terms-conditions-i-c/home");
            i3.putExtra("terms", terms);
            startActivity(i3);
        }
    });
//        share.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            Intent myIntent = new Intent(Intent.ACTION_SEND);
//            myIntent.setType("text/plain");
//            String body = "https://play.google.com/store/apps/details?id=com.pixelstudio.android.cocofactory";
//            String sub = "Share Ewris";
//            myIntent.putExtra(Intent.EXTRA_SUBJECT,sub);
//            myIntent.putExtra(Intent.EXTRA_TEXT,body);
//            startActivity(Intent.createChooser(myIntent, "Share Using"));
//        }
//    });

 }

    private void findViews(){
        fbPage = findViewById(R.id.facebook);
        ytPage = findViewById(R.id.youtube);
        ewris = findViewById(R.id.ewris);
        examP = findViewById(R.id.examP);
        privacy = findViewById(R.id.privacy);
        terms = findViewById(R.id.terms);
        share = findViewById(R.id.share);

    }

    private void gotoUrl(String s) {
        Uri uri = Uri.parse(s);
        startActivity(new Intent(Intent.ACTION_VIEW,uri));
    }

}