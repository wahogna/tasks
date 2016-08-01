package org.tasks.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.common.base.Function;

import org.tasks.R;
import org.tasks.calendars.AndroidCalendar;
import org.tasks.calendars.CalendarProvider;
import org.tasks.dialogs.AlertDialogBuilder;
import org.tasks.dialogs.DialogBuilder;
import org.tasks.injection.DialogFragmentComponent;
import org.tasks.injection.ForActivity;
import org.tasks.injection.InjectingDialogFragment;

import java.util.List;

import javax.inject.Inject;

import static com.google.common.collect.Lists.transform;

public class CalendarSelectionDialog extends InjectingDialogFragment {

    public void enableNone() {
        enableNone = true;
    }

    @Override
    protected void inject(DialogFragmentComponent component) {
        component.inject(this);
    }

    public interface CalendarSelectionHandler {
        void selectedCalendar(AndroidCalendar calendar);
        void dismiss();
    }

    @Inject DialogBuilder dialogBuilder;
    @Inject CalendarProvider calendarProvider;
    @Inject @ForActivity Context context;
    private CalendarSelectionHandler handler;
    private boolean enableNone;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final List<AndroidCalendar> calendars = calendarProvider.getCalendars();
        List<String> calendarNames = transform(calendars, new Function<AndroidCalendar, String>() {
            @Override
            public String apply(AndroidCalendar androidCalendar) {
                return androidCalendar.getName();
            }
        });

        if (calendarNames.isEmpty()) {
            Toast.makeText(context, R.string.no_calendars_found, Toast.LENGTH_LONG).show();
            handler.dismiss();
        }

        AlertDialogBuilder builder = dialogBuilder.newDialog()
                .setItems(calendarNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.selectedCalendar(calendars.get(which));
                        dialog.dismiss();
                    }
                });
        if (enableNone) {
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).setNeutralButton(R.string.none, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.selectedCalendar(AndroidCalendar.NONE);
                    dialog.dismiss();
                }
            });
        }

        return builder.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        handler.dismiss();
    }

    public void setCalendarSelectionHandler(CalendarSelectionHandler handler) {
        this.handler = handler;
    }
}
