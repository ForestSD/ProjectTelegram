package com.example.projecttelegram;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.github.library.bubbleview.BubbleTextView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class MainActivity extends AppCompatActivity {

    public static int SING_IN_CODE = 1;
    public RelativeLayout activity_main;
    public FirebaseListAdapter<Messages> adapter;
    public EmojiconEditText emojiconEditText;
    public ImageView emojiBotton, submitButton;
    public EmojIconActions emojIconActions;
    public String CHANNEL_ID = "personal_non";
    public int NOTIFICATION_ID = 001;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        if(requestCode == SING_IN_CODE){
            if(resultCode == RESULT_OK){
                Snackbar.make(activity_main,"Вы авторизованы", Snackbar.LENGTH_SHORT).show();
                displayAllMessages();
            }else {
                Snackbar.make(activity_main,"Вы не авторизованы", Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getBundleExtra("msg");
            String message = bundle.getString("msgBody");
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main = findViewById(R.id.activity_main);
        submitButton = findViewById(R.id.submit_button);
        emojiBotton = findViewById(R.id.emoji_button);
        emojiconEditText = findViewById(R.id.text_field);

        emojIconActions = new EmojIconActions(getApplicationContext(), activity_main, emojiconEditText, emojiBotton);
        emojIconActions.ShowEmojIcon();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().push().setValue(new Messages(
                        FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                        emojiconEditText.getText().toString()
                ));
                if (activityReceiver != null) {
                    IntentFilter intentFilter = new  IntentFilter("ACTION_STRING_ACTIVITY");
                    registerReceiver(activityReceiver, intentFilter);
                }
                emojiconEditText.setText("");
            }
        });

        if(FirebaseAuth.getInstance().getCurrentUser() == null)
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(),SING_IN_CODE);
        else {
            Snackbar.make(activity_main,"Вы авторизованы", Snackbar.LENGTH_SHORT).show();
            displayAllMessages();
        }
    }


    //"Оп Оп, сообщение!"
    public void notificationDisplay(){
        createNotificationChanel();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID);
        builder.setSmallIcon(R.drawable.smiley);
        builder.setContentTitle("TelegramMESS");
        builder.setContentText("Оп Оп, сообщение!");
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    public void createNotificationChanel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            CharSequence charSequence = "Personal Notification";
            int importanceDefault = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, charSequence, importanceDefault);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void displayAllMessages() {
        ListView listOfMessages = findViewById(R.id.list_of_messages);
        adapter = new FirebaseListAdapter<Messages>(this, Messages.class, R.layout.list_item,
                FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, Messages model, int position) {
                TextView mess_user, mess_time;
                BubbleTextView mess_text;
                mess_user = v.findViewById(R.id.message_user);
                mess_text = v.findViewById(R.id.message_text);
                mess_time = v.findViewById(R.id.message_time);

                mess_user.setText(model.getUserName());
                mess_text.setText(model.getTextMessage());
                mess_time.setText(DateFormat.format("dd-mm-yyyy HH:mm:ss", model.getMessageTime()));
            }
        };

        listOfMessages.setAdapter(adapter);
    }

}
