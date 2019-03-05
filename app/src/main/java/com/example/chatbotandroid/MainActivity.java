package com.example.chatbotandroid;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatbotandroid.Adapter.ChatAdapter;
import com.example.chatbotandroid.Model.Chat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity implements AIListener {

    private static final int RECORD_REQUEST_CODE = 101;
    private static final int REQUEST_CODE_SPEECH_INPUT = 505 ;
    Button listen;
    TextView textView,userText,responseText;
    private ChatAdapter mChatAdapter;
    private  List<Chat> mChat = new ArrayList<>();
    RecyclerView recyclerView;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listen = findViewById(R.id.listen);
        userText = findViewById(R.id.userText);
        responseText = findViewById(R.id.responseText);

        textView = findViewById(R.id.textView);
        recyclerView = findViewById(R.id.recycler_view);
        mChatAdapter = new ChatAdapter(this,mChat);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mChatAdapter);

        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if(permission != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "onCreate: "+ "permission to record denied ");
            makeRequest();
        }
        final AIConfiguration config = new AIConfiguration("f8738d70fec54e4a9bc169bf88e91f7c",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        final AIService aiService = AIService.getService(this, config);
        aiService.setListener(this);

        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
                aiService.startListening();
            }
        });


    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say Something");

        try{
            startActivityForResult(intent,REQUEST_CODE_SPEECH_INPUT);
        }
        catch (ActivityNotFoundException e ){
            Toast.makeText(getApplicationContext(),"Your device doesn't supports speech input",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQUEST_CODE_SPEECH_INPUT : {
                if (resultCode == RESULT_OK && null != data){
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String userQuery = result.get(0);
                    userText.setText(userQuery);
                    sendMessage("11","00",userQuery);
                    RetrieveFeedTask task = new RetrieveFeedTask();
                    task.execute(userQuery);

                }
                break;
            }
        }
    }

    private void sendMessage(String s, String s1, String userQuery)
    {
        Chat newChat = new Chat();
        newChat.setSender(s);
        newChat.setReciever(s1);
        newChat.setMessage(userQuery);
        mChat.add(newChat);
        mChatAdapter.notifyDataSetChanged();

    }


    private void makeRequest() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},
                RECORD_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case RECORD_REQUEST_CODE : {
                if(grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "Permission denied by User");
                }
                else
                {
                    Log.d(TAG, "Permission granted by user");
                }
                return;
            }
        }
    }

    public String getText(String query)  throws UnsupportedEncodingException {
        String text = "";
        BufferedReader reader  = null;

        try {
            URL url = new URL("https://api.dialogflow.com/v1/query?v=20150910");

            //send POST Data request

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);

            conn.setRequestProperty("Authorization", "Bearer f8738d70fec54e4a9bc169bf88e91f7c");
            conn.setRequestProperty("Content-Type", "application/json");

            //create JSON Object here

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("query",query);
            jsonParam.put("lang","en");
            jsonParam.put("sessionId","1234567890");

            //use Output Stream to send data with this post request
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            Log.d(TAG, "getText: after conversation is "+ jsonParam.toString());
            wr.write(jsonParam.toString());
            wr.flush();
            Log.d(TAG, "karma "+ " json is "+ jsonParam);

            //Get the server response
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder ab = new StringBuilder();
            String line = null;

            //Read server response
            while ((line = reader.readLine()) != null){
                ab.append(line + "\n");
            }

            text = ab.toString();


            //access
            JSONObject jsonObject = new JSONObject(text);
            JSONObject object  = jsonObject.getJSONObject("result");
            JSONObject fullfillment  = null;
            String speech = null;

            fullfillment = object.getJSONObject("fulfillment");

            speech = fullfillment.optString("speech");

            Log.d(TAG, "karma "+ "response is : "+ text);
           return  speech;


        }

         catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try{
                reader.close();
            }
            catch (Exception ex){

            }
        }
        return  null;
    }

    class RetrieveFeedTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... strings) {
            String s = null;
            try
            {
                Log.d(TAG, "doInBackground: called");
                s = getText(strings[0]);
                Log.d(TAG, "doInBackground: after called");
            }
            catch (UnsupportedEncodingException e){
                e.printStackTrace();
                Log.d(TAG, "doInBackground: "+ e);
            }
            return s;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: "+ s);
            responseText.setText(s);
            Chat newChat = new Chat();
            newChat.setSender("00");
            newChat.setReciever("11");
            newChat.setMessage(s);
            mChat.add(newChat);
        //    readMessages("00","11",s);
            mChatAdapter.notifyDataSetChanged();
        }
    }

    private void readMessages(String sender,String receiver,String message){

             Chat myChat = new Chat(sender,receiver,message);
             mChat.add(myChat);
             mChatAdapter.notifyDataSetChanged();



    }

    @Override
    public void onResult(AIResponse result) {
        Log.d(TAG, "onResult: "+ result.toString());
        Result result1 = result.getResult();

        textView.setText("Query "+ result1.getResolvedQuery()+" Action: "+ result1.getAction());


    }

    @Override
    public void onError(AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

}
