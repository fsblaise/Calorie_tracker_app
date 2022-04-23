package hu.fsblaise.kcal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String msg = bundle.get("msg").toString();
        new NotificationHandler(context).send(msg);
    }
}