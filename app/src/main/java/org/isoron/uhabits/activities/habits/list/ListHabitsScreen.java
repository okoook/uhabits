/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.activities.habits.list;

import android.content.*;
import android.os.*;
import android.support.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.intents.*;
import org.isoron.uhabits.io.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.activities.*;
import org.isoron.uhabits.activities.common.dialogs.*;
import org.isoron.uhabits.activities.common.dialogs.ColorPickerDialog.*;
import org.isoron.uhabits.activities.habits.edit.*;
import org.isoron.uhabits.utils.*;

import java.io.*;

import javax.inject.*;

@ActivityScope
public class ListHabitsScreen extends BaseScreen
    implements CommandRunner.Listener
{
    public static final int RESULT_BUG_REPORT = 4;

    public static final int RESULT_EXPORT_CSV = 2;

    public static final int RESULT_EXPORT_DB = 3;

    public static final int RESULT_IMPORT_DATA = 1;

    @Nullable
    private ListHabitsController controller;

    @NonNull
    private final DialogFactory dialogFactory;

    @NonNull
    private final IntentFactory intentFactory;

    @NonNull
    private final DirFinder dirFinder;

    @NonNull
    private final CommandRunner commandRunner;

    @NonNull
    private final ConfirmDeleteDialogFactory confirmDeleteDialogFactory;

    @NonNull
    private final CreateHabitDialogFactory createHabitDialogFactory;

    @NonNull
    private final FilePickerDialogFactory filePickerDialogFactory;

    @Inject
    public ListHabitsScreen(@NonNull BaseActivity activity,
                            @NonNull CommandRunner commandRunner,
                            @NonNull DirFinder dirFinder,
                            @NonNull DialogFactory dialogFactory,
                            @NonNull ListHabitsRootView rootView,
                            @NonNull IntentFactory intentFactory,
                            @NonNull ConfirmDeleteDialogFactory confirmDeleteDialogFactory,
                            @NonNull CreateHabitDialogFactory createHabitDialogFactory,
                            @NonNull FilePickerDialogFactory filePickerDialogFactory)
    {
        super(activity);
        setRootView(rootView);
        this.commandRunner = commandRunner;
        this.confirmDeleteDialogFactory = confirmDeleteDialogFactory;
        this.createHabitDialogFactory = createHabitDialogFactory;
        this.dialogFactory = dialogFactory;
        this.dirFinder = dirFinder;
        this.filePickerDialogFactory = filePickerDialogFactory;
        this.intentFactory = intentFactory;
    }

    public void onAttached()
    {
        commandRunner.addListener(this);
    }

    @Override
    public void onCommandExecuted(@NonNull Command command,
                                  @Nullable Long refreshKey)
    {
        showMessage(command.getExecuteStringId());
    }

    public void onDettached()
    {
        commandRunner.removeListener(this);
    }

    @Override
    public void onResult(int requestCode, int resultCode, Intent data)
    {
        if (controller == null) return;

        switch (resultCode)
        {
            case RESULT_IMPORT_DATA:
                showImportScreen();
                break;

            case RESULT_EXPORT_CSV:
                controller.onExportCSV();
                break;

            case RESULT_EXPORT_DB:
                controller.onExportDB();
                break;

            case RESULT_BUG_REPORT:
                controller.onSendBugReport();
                break;
        }
    }

    public void setController(@Nullable ListHabitsController controller)
    {
        this.controller = controller;
    }

    public void showAboutScreen()
    {
        Intent intent = intentFactory.startAboutActivity(activity);
        activity.startActivity(intent);
    }

    /**
     * Displays a {@link ColorPickerDialog} to the user.
     * <p>
     * The selected color on the dialog is the color of the given habit.
     *
     * @param habit    the habit
     * @param callback
     */
    public void showColorPicker(@NonNull Habit habit,
                                @NonNull OnColorSelectedListener callback)
    {
        ColorPickerDialog picker =
            dialogFactory.buildColorPicker(habit.getColor());
        picker.setListener(callback);
        activity.showDialog(picker, "picker");
    }

    public void showCreateHabitScreen()
    {
        activity.showDialog(createHabitDialogFactory.create(), "editHabit");
    }

    public void showDeleteConfirmationScreen(ConfirmDeleteDialog.Callback callback)
    {
        activity.showDialog(confirmDeleteDialogFactory.create(callback));
    }

    public void showEditHabitScreen(Habit habit)
    {
        EditHabitDialog dialog = dialogFactory.buildEditHabitDialog(habit);
        activity.showDialog(dialog, "editHabit");
    }

    public void showFAQScreen()
    {
        Intent intent = intentFactory.viewFAQ(activity);
        activity.startActivity(intent);
    }

    public void showHabitScreen(@NonNull Habit habit)
    {
        Intent intent = intentFactory.startShowHabitActivity(activity, habit);
        activity.startActivity(intent);
    }

    public void showImportScreen()
    {
        File dir = dirFinder.findStorageDir(null);

        if (dir == null)
        {
            showMessage(R.string.could_not_import);
            return;
        }

        FilePickerDialog picker = filePickerDialogFactory.create(dir);

        if (controller != null)
            picker.setListener(file -> controller.onImportData(file));
        activity.showDialog(picker.getDialog());
    }

    public void showIntroScreen()
    {
        Intent intent = intentFactory.startIntroActivity(activity);
        activity.startActivity(intent);
    }

    public void showSettingsScreen()
    {
        Intent intent = intentFactory.startSettingsActivity(activity);
        activity.startActivityForResult(intent, 0);
    }

    public void toggleNightMode()
    {
        if (InterfaceUtils.isNightMode())
            InterfaceUtils.setCurrentTheme(InterfaceUtils.THEME_LIGHT);
        else InterfaceUtils.setCurrentTheme(InterfaceUtils.THEME_DARK);

        refreshTheme();
    }

    private void refreshTheme()
    {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(activity, MainActivity.class);

            activity.finish();
            activity.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
            activity.startActivity(intent);

        }, 500); // HACK: Let the menu disappear first
    }
}
