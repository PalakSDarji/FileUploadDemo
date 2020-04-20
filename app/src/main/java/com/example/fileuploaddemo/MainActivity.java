package com.example.fileuploaddemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.NormalFilePickActivity;
import com.vincent.filepicker.filter.entity.NormalFile;

import java.io.File;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.pickFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent4 = new Intent(MainActivity.this, NormalFilePickActivity.class);
                intent4.putExtra(Constant.MAX_NUMBER, 1);
                intent4.putExtra(NormalFilePickActivity.SUFFIX, new String[] {"xlsx", "xls", "doc", "docx", "ppt", "pptx", "pdf"});
                startActivityForResult(intent4, Constant.REQUEST_CODE_PICK_FILE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case  Constant.REQUEST_CODE_PICK_FILE:
                if (resultCode == RESULT_OK) {
                    ArrayList<NormalFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_FILE);
                    Log.v("FileUploadDemo","Picked file: "+ list.get(0).getPath());
                    uploadToServer(list.get(0).getPath());
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadToServer(String filePath) {

        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        FileUploadService uploadAPIs = retrofit.create(FileUploadService.class);
        //Create a file object using file path
        File file = new File(filePath);
        // Create a request body with file and image media type
        RequestBody fileReqBody = RequestBody.create(MediaType.parse(getMimeType(file)),
                file
        );
        // Create MultipartBody.Part using file request-body,file name and part name
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileReqBody);

        Call call = uploadAPIs.upload(part);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Log.v("FileUploadDemo","Call success");
                Toast.makeText(MainActivity.this.getApplicationContext(),response.code()+" Uploaded!",Toast.LENGTH_LONG).show();
            }
            @Override
            public void onFailure(Call call, Throwable t) {
                Log.v("FileUploadDemo","Call failure");
                Toast.makeText(MainActivity.this.getApplicationContext(),"Upload failed! " + t.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    public static String getMimeType(File file) {
        String extension = null;
        //Check uri format to avoid null
        if (file != null)
            extension = MimeTypeMap.getFileExtensionFromUrl(file.getPath());

        return extension;
    }
}
