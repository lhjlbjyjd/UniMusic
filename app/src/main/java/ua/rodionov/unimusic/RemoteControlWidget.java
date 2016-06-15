package ua.rodionov.unimusic;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Created by Дмитрий on 13.06.2016.
 */
@SuppressLint("ParcelCreator")
public class RemoteControlWidget extends RemoteViews {
    private final Context mContext;

    public static final String ACTION_PLAY = "ua.rodionov.unimusic.ACTION_PLAY";

    public static final String ACTION_PREVIOUS = "ua.rodionov.unimusic.ACTION_PREVIOUS";

    public static final String ACTION_NEXT = "ua.rodionov.unimusic.ACTION_NEXT";

    public RemoteControlWidget(Context context, int layoutId)
    {
        super("ua.rodionov.unimusic", layoutId);
        mContext = context;
        Intent intent = new Intent(ACTION_PLAY);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,100,
                intent,PendingIntent.FLAG_UPDATE_CURRENT);
        setOnClickPendingIntent(R.id.play_control,pendingIntent);
        intent = new Intent(ACTION_PREVIOUS);
        pendingIntent = PendingIntent.getBroadcast(mContext,101,
                intent,PendingIntent.FLAG_UPDATE_CURRENT);
        setOnClickPendingIntent(R.id.previous_control,pendingIntent);
        intent = new Intent(ACTION_NEXT);
        pendingIntent = PendingIntent.getBroadcast(mContext,102,
                intent,PendingIntent.FLAG_UPDATE_CURRENT);
        setOnClickPendingIntent(R.id.next_control,pendingIntent);
    }
}
