package vitamio.vitamiolibrary.videos.utils;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.example.ijkplay.IjkPlayerActivity;
import com.example.ijkplay.IjkPlayerActivity2;

/**
 * Created by aoe on 2015/12/25.
 */
public class PlayUtils {

    public static final String TAG="**PlayUtils**";



    public static void startIjkPlayVideo(Context context,String url,String LiverName,String UserName,boolean flag){
        Intent intent=new Intent(context, IjkPlayerActivity.class);
        intent.putExtra("url",url);
        intent.putExtra("LiverName",LiverName);
        intent.putExtra("UserName",UserName);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startIjkPlayVideo2(Context context,String url,boolean flag){
        Intent intent=new Intent(context, IjkPlayerActivity2.class);
        intent.putExtra("url",url);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean isEmptyView(View view){
        if(view==null){return false;}return true;
    }

    public static String getResString(Context context,int resId){
        return context.getResources().getString(resId);
    }

}
